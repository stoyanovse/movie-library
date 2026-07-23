package org.alphatrack.movielibrary.services;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.alphatrack.movielibrary.dtos.UserRegisterDto;
import org.alphatrack.movielibrary.dtos.UserUpdateDto;
import org.alphatrack.movielibrary.dtos.filters.UserFilterOptions;
import org.alphatrack.movielibrary.models.User;
import org.alphatrack.movielibrary.models.enums.Role;
import org.alphatrack.movielibrary.repositories.contracts.UserRepository;
import org.alphatrack.movielibrary.services.contracts.UserService;
import org.alphatrack.movielibrary.utils.mappers.UserMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<User> getAll(UserFilterOptions userFilterOptions) {
        return userRepository.findAll(userFilterOptions);
    }

    @Override
    public List<User> search(UserFilterOptions userFilterOptions) {

        if (userFilterOptions.getUsername().isEmpty() &&
                userFilterOptions.getFirstName().isEmpty() &&
                userFilterOptions.getLastName().isEmpty()) {

            throw new IllegalArgumentException("You must provide at least one search parameter.");
        }

        return userRepository.findAll(userFilterOptions);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id %d not found", id)));
    }

    @Transactional
    @Override
    public User promoteToAdmin(Long id, User currentUser) {
        User user = userRepository.getUserById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User with id %d not found", id)));

        if (!currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException("Only admin can promote a user");
        }

        if (!user.getIsEnabled()) {
            throw new AccessDeniedException("You cannot promote deleted user");
        }

        user.setIsBlocked(false);
        user.setRole(Role.ADMIN);
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public User update(Long id, UserUpdateDto userUpdateDto, User currentUser) {
        boolean isOwner = currentUser.getId().equals(id);

        if (!isOwner) {
            throw new AccessDeniedException("Only the owner can update its profile");
        }

        if (userUpdateDto.getFirstName() != null) {
            currentUser.setFirstName(userUpdateDto.getFirstName());
        }

        if (userUpdateDto.getLastName() != null) {
            currentUser.setLastName(userUpdateDto.getLastName());
        }

        return userRepository.save(currentUser);
    }

    @Transactional
    @Override
    public User create(UserRegisterDto userRegisterDto) {
        if (userRepository.findUserByUsername(userRegisterDto.getUsername()).isPresent()) {
            throw new EntityExistsException(String.format("User with username %s exists", userRegisterDto.getUsername()));
        }
        if (userRepository.findUserByEmail(userRegisterDto.getEmail()).isPresent()) {
            throw new EntityExistsException(String.format("User with email %s exists", userRegisterDto.getEmail()));
        }

        User newUser = userMapper.dtoToUser(userRegisterDto);
        return userRepository.save(newUser);
    }

    @Transactional
    @Override
    public void delete(Long id, User currentUser) {
        User user = userRepository.getUserById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("User with id %d not found", id)
                ));

        boolean isOwner = currentUser.getId().equals(user.getId());
        boolean isAdmin = currentUser.getRole().equals(Role.ADMIN);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not authorized to delete this profile");
        }
        user.setIsEnabled(false);
        userRepository.save(user);
    }
}
