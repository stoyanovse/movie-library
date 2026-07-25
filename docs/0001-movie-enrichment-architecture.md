# Movie Library Architecture & Technical Decisions 

## 1. External Rating API Integration 
The application integrates with the **OMDb API** to enrich local movie records
with external IMDb ratings.
* To ensure robust and safe HTTP requests, the `UriBuilder` is used to
  construct the endpoints.
* This prevents application crashes by automatically handling URL encoding
  (e.g., converting spaces in movie titles to `%20`) and safely managing
  query parameter syntax (`?` and `&`).
* The raw JSON response is deserialized into an `OmdbResponseDto` using
  Spring's internal JSON parser, allowing the application to safely
  extract, parse, and save the rating field.

## 2. Authentication & Authorization 
Security is implemented using **Spring Security** with a standard
**session-based authentication** architecture.
* The application uses Basic Authentication to verify user credentials
  during login.
* Upon successful authentication, the server creates a stateful session
  in memory and issues a `JSESSIONID` cookie to the client's browser.
* This traditional stateful approach was chosen because it is highly
  secure and straightforward for applications where the frontend and
  backend are tightly coupled, avoiding the overhead of managing
  stateless tokens (like JWTs).

## 3. Asynchronous Data Enrichment 
To ensure a fast user experience, the OMDb API fetch is processed
asynchronously.
* When a user saves a movie, the primary database transaction is handled
  by the main worker thread.
* Using `TransactionSynchronizationManager.afterCommit()`, the application
  waits until the new movie is safely committed to the database.
* The `@Async` annotation then intercepts the OMDb fetch request,
  offloading it to a separate **Background Thread Pool**. This allows the
  main thread to immediately return a success response to the user
  without waiting for the external network call to finish.

## 4. Architectural Decisions & Trade-offs 
A key architectural decision was choosing between Spring's `RestClient`
and `WebClient` for the API integration.
* **`RestClient`** was selected. It is a synchronous, blocking client
  with a modern fluent API.
* **Trade-off context:** While `WebClient` offers a highly efficient,
  non-blocking Event Loop capable of handling massive traffic with
  minimal threads, it requires a fully reactive stack to be effective.
* Because the application relies on **Spring Data JPA (JDBC)** for
  database operations—which is inherently synchronous and blocks threads
  while waiting for the database—using `WebClient` would add unnecessary
  complexity without providing a true non-blocking pipeline. Therefore,
  `RestClient` combined with `@Async` background threads provides the
  perfect balance of performance and maintainability for this stack.