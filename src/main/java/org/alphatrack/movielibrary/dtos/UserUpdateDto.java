package org.alphatrack.movielibrary.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {

    @NotBlank(message = "First name cannot be empty")
    @Size(message = "The length of First name should be between 4 and 32 characters"
            , min = 4, max = 32)
    private String firstName;

    @NotBlank(message = "Last name cannot be empty")
    @Size(message = "The length of Last name should be between 4 and 32 characters"
            , min = 4, max = 32)
    private String lastName;

}
