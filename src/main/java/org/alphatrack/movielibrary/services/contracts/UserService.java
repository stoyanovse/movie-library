package org.alphatrack.movielibrary.services.contracts;

import org.alphatrack.movielibrary.dtos.UserRegisterDto;
import org.alphatrack.movielibrary.dtos.UserUpdateDto;
import org.alphatrack.movielibrary.dtos.filters.UserFilterOptions;
import org.alphatrack.movielibrary.models.User;

import java.util.List;

public interface UserService {

    List<User> getAll(UserFilterOptions userFilterOptions);

    User getById(Long id);
    User update(Long id, UserUpdateDto userUpdateDto, User currentUser);
    User create(UserRegisterDto userRegisterDto);
    void delete(Long id, User currentUser);



}
