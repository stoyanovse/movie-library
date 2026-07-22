package org.alphatrack.movielibrary.repositories.contracts;

import org.alphatrack.movielibrary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>,UserRepositoryCustom {
    Optional<User> findUserByUsername(String username);

    Optional<Object> findUserByEmail(String email);

   Optional<User> getUserById(Long id);

}

