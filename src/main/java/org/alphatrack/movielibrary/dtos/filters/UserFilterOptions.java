package org.alphatrack.movielibrary.dtos.filters;

import lombok.*;

import java.util.Optional;
@Getter

public class UserFilterOptions {
    private final Optional<String> username;
    private final Optional<String> firstName;
    private final Optional<String> lastName;

    @Builder
    public UserFilterOptions(
            String username,
            String firstName,
            String lastName

    ) {
        this.username = Optional.ofNullable(username);
        this.firstName = Optional.ofNullable(firstName);
        this.lastName = Optional.ofNullable(lastName);

    }

}
