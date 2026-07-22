package org.alphatrack.movielibrary.repositories.contracts;

import org.alphatrack.movielibrary.dtos.filters.UserFilterOptions;
import org.alphatrack.movielibrary.models.User;

import java.util.List;

public interface UserRepositoryCustom {
    List<User> findAll(UserFilterOptions userFilterOptions);
}
