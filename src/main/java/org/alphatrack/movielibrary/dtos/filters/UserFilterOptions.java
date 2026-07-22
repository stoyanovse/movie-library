package org.alphatrack.movielibrary.dtos.filters;

import lombok.Builder;
import lombok.Getter;

import java.util.Optional;
@Getter
public class UserFilterOptions {
    private Optional<String> username;
    private Optional<String> firstName;
    private Optional<String> lastName;
    private Optional<String> email;
    private Optional<String> sortBy;
    private Optional<String> sortOrder;

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
