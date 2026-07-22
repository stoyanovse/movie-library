package org.alphatrack.movielibrary.services;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.alphatrack.movielibrary.dtos.UserRegisterDto;
import org.alphatrack.movielibrary.dtos.UserUpdateDto;
import org.alphatrack.movielibrary.dtos.filters.UserFilterOptions;
import org.alphatrack.movielibrary.models.User;
import org.alphatrack.movielibrary.models.enums.Role;
import org.alphatrack.movielibrary.repositories.contracts.UserRepository;
import org.alphatrack.movielibrary.services.contracts.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<User> getAll(UserFilterOptions userFilterOptions) {
        return userRepository.findAll(userFilterOptions);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id %d not found", id)));
    }

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

    @Override
    public User create(UserRegisterDto userRegisterDto) {
        if (userRepository.findUserByUsername(userRegisterDto.getUsername()).isPresent()) {
            throw new EntityExistsException(String.format("User with username %s exists", userRegisterDto.getUsername());
        }
        if (userRepository.findUserByEmail(userRegisterDto.getEmail()).isPresent()) {
            throw new EntityExistsException(String.format("User with email %s exists", userRegisterDto.getEmail());
        }

        User newUser = User.builder()
                .username(userRegisterDto.getUsername())
                .firstName(userRegisterDto.getFirstName())
                .lastName(userRegisterDto.getLastName())
                .password(passwordEncoder.encode(userRegisterDto.getPassword()))
                .email(userRegisterDto.getEmail())
                .role(Role.USER)
                .isBlocked(false)
                .build();

        return userRepository.save(newUser);
    }

    @Override
    public void delete(Long id, User currentUser) {
        User user = userRepository.getUserById(id);

        boolean isOwner = currentUser.getId().equals(user.getId());
        boolean isAdmin = currentUser.getRole().equals(Role.ADMIN);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("You are not authorized to delete this profile");
        }
        user.setIsEnabled(false);
        userRepository.save(user);
    }
}
