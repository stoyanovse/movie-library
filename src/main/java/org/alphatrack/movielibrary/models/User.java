package org.alphatrack.movielibrary.models;

import jakarta.persistence.*;
import lombok.*;
import org.alphatrack.movielibrary.models.enums.Role;

@Entity
@Table(name = "users")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column
    private String username;

    @Column
    private String firstName;

    @Column
    private String lastName;

    @Column
    private String password;

    @Column
    private Role role;

    @Column
    private String email;

    @Column
    private Boolean isBlocked;

    @Column
    private Boolean isEnabled;


}
