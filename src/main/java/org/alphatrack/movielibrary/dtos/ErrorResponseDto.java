package org.alphatrack.movielibrary.dtos;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ErrorResponseDto {

    private String message;
    private int statusCode;
    private String error;
    private String path;
    private LocalDateTime timeStamp;

}
