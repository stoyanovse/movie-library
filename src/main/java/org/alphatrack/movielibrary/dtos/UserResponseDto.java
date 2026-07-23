package org.alphatrack.movielibrary.dtos;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private Long id;

    private String username;

    private String email;
}
