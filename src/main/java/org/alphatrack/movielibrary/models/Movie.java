package org.alphatrack.movielibrary.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movies")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String director;

    @Column(nullable = false)
    private Integer releaseYear;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false,
    foreignKey = @ForeignKey(name = "fk_movie_user_id"))
    private User addedBy;

    @Column
    private Double rating;

}
