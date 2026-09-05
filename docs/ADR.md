# Architecture Decision Record — Movie Library REST API

**Status:** Accepted

This document explains the reasoning behind four key technical decisions in
this project: how ratings are fetched from OMDb, how that fetch is kept from
blocking the user, why a synchronous HTTP client was chosen despite an
async workload, and how authentication works.

---

## 1. Safe URL Construction for the OMDb Integration

**Context**
The OMDb API is queried with a movie title as a parameter. Titles can
contain spaces, punctuation, and other characters that aren't safe to drop
directly into a URL string.

**Decision**
Requests are built using Spring's `UriBuilder` rather than manual string
concatenation. `UriBuilder` handles percent-encoding automatically (e.g.
turning spaces into `%20`) and manages query parameter syntax (`?`, `&`)
correctly regardless of how many parameters are present.

**Consequences**
Movie titles with special characters can't produce malformed requests or
crash the integration. The trade-off is negligible — `UriBuilder` is a
standard Spring API with no added dependency cost.

---

## 2. Asynchronous Rating Enrichment

**Context**
Fetching a rating from OMDb is a network call to a third-party service.
Making the user's "save movie" request wait on that call ties response time
to an external service's latency and availability — a slow or down OMDb
would slow down or break movie creation entirely, even though the rating is
a secondary enhancement, not core data.

**Decision**
The OMDb fetch is decoupled from the save request:
1. The movie is saved in the primary transaction on the main request thread.
2. `TransactionSynchronizationManager.afterCommit()` ensures OMDb is only
   queried *after* the movie is safely committed to the database — avoiding
   a race where the enrichment step runs against data that might still be
   rolled back.
3. `@Async` offloads the actual OMDb call to a background thread pool, so
   the main thread returns a response to the user immediately.

**Consequences**
Movie creation stays fast and independent of OMDb's response time or
uptime. The trade-off is eventual consistency — the rating appears on the
movie record shortly after creation, not instantly — which is an acceptable
delay for a non-critical enrichment field.

---

## 3. RestClient over WebClient

**Context**
Spring offers two HTTP clients for calling OMDb: the reactive, non-blocking
`WebClient`, and the newer synchronous, blocking `RestClient`.

**Decision**
`RestClient` was chosen.

**Consequences**
`WebClient`'s main advantage — an efficient non-blocking event loop capable
of handling large volumes of concurrent I/O with few threads — only pays
off when the *entire* call stack is reactive. This application's
persistence layer is Spring Data JPA over JDBC, which is inherently
blocking: it holds a thread while waiting on the database regardless of
what HTTP client is used elsewhere. Introducing `WebClient` here would add
the complexity of a reactive dependency without removing the actual
blocking point in the request path — a partially reactive stack gets none
of the benefit and all of the added complexity. Combined with the `@Async`
background thread pool from decision 2, `RestClient` gives the same
practical outcome (the main thread isn't blocked on OMDb) with a simpler,
more maintainable client.

---

## 4. Session-Based Authentication

**Context**
The API needs to authenticate users and enforce role-based access. The two
common approaches are stateless tokens (e.g. JWT) or traditional
server-side sessions.

**Decision**
Spring Security's standard session-based model was used: HTTP Basic
authentication verifies credentials, and on success the server creates a
stateful in-memory session and issues a `JSESSIONID` cookie.

**Consequences**
This is simpler to implement correctly and reason about than a token-based
scheme, with no need to manage token expiry, refresh, or signing keys — a
good fit for an application where client and server aren't independently
scaled or deployed. The trade-off is that this approach doesn't scale
horizontally without shared session storage (e.g. Redis), and isn't a
natural fit for a fully decoupled SPA or mobile client consuming the API
from a different origin. If the API needed to support either of those in
the future, a token-based scheme would be worth revisiting.
