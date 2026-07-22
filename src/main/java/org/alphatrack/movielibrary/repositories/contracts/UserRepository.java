package org.alphatrack.movielibrary.repositories.contracts;

import org.alphatrack.movielibrary.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>,UserRepositoryCustom {
}

