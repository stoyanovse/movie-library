package org.alphatrack.movielibrary.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OmdbResponseDto {

    @JsonProperty("imdbRating")
    private String imdbRating;

    @JsonProperty("response")
    private String response;

    @JsonProperty("Error")
    private String error;

}
