package org.alphatrack.movielibrary.dtos.filters;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;
@Getter
public class UserFilterOptions {
    private final Optional<String> username;
    private final Optional<String> firstName;
    private final Optional<String> lastName;
    private final Optional<String> email;
    private final Optional<String> sortBy;
    private final Optional<String> sortOrder;

    @Builder
    public UserFilterOptions(
            String username,
            String firstName,
            String lastName,
            String email,
            String sortBy,
            String sortOrder
    ) {
        this.username = Optional.ofNullable(username);
        this.firstName = Optional.ofNullable(firstName);
        this.lastName = Optional.ofNullable(lastName);
        this.email = Optional.ofNullable(email);
        this.sortBy = Optional.ofNullable(sortBy);
        this.sortOrder = Optional.ofNullable(sortOrder);
    }

}
