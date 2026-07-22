package org.alphatrack.movielibrary.dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserResponseDto {
    private Long id;

    private String username;

    private String email;
}
