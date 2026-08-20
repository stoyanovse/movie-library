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
    public static final String PROVIDE_SEARCH_PARAMETER = "You must provide at least one search parameter.";
    public static final String USER_NOT_FOUND = "User with id %d not found";
    public static final String ONLY_ADMIN_CAN_PROMOTE_A_USER = "Only admin can promote a user";
    public static final String CANNOT_PROMOTE_DELETED_USER = "You cannot promote deleted user";
    public static final String OWNER_CAN_UPDATE_ITS_PROFILE = "Only the owner can update its profile";
    public static final String USER_WITH_USERNAME_EXISTS = "User with username %s exists";
    public static final String USER_WITH_EMAIL_EXISTS = "User with email %s exists";
    public static final String NOT_AUTHORIZED_TO_DELETE_THIS_PROFILE = "You are not authorized to delete this profile";
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

        boolean noUsername = userFilterOptions.getUsername().orElse("").isBlank();
        boolean noFirstName = userFilterOptions.getFirstName().orElse("").isBlank();
        boolean noLastName = userFilterOptions.getLastName().orElse("").isBlank();

        if (noUsername && noFirstName && noLastName) {
            throw new IllegalArgumentException(PROVIDE_SEARCH_PARAMETER);
        }

        return userRepository.findAll(userFilterOptions);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format(USER_NOT_FOUND, id)));
    }

    @Transactional
    @Override
    public User promoteToAdmin(Long id, User currentUser) {
        User user = getById(id);

        if (!currentUser.getRole().equals(Role.ADMIN)) {
            throw new AccessDeniedException(ONLY_ADMIN_CAN_PROMOTE_A_USER);
        }

        if (!user.getIsEnabled()) {
            throw new AccessDeniedException(CANNOT_PROMOTE_DELETED_USER);
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
            throw new AccessDeniedException(OWNER_CAN_UPDATE_ITS_PROFILE);
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
            throw new EntityExistsException(String.format(USER_WITH_USERNAME_EXISTS, userRegisterDto.getUsername()));
        }
        if (userRepository.findUserByEmail(userRegisterDto.getEmail()).isPresent()) {
            throw new EntityExistsException(String.format(USER_WITH_EMAIL_EXISTS, userRegisterDto.getEmail()));
        }

        User newUser = userMapper.dtoToUser(userRegisterDto);
        return userRepository.save(newUser);
    }

    @Transactional
    @Override
    public void delete(Long id, User currentUser) {
        User user = getById(id);

        boolean isOwner = currentUser.getId().equals(user.getId());
        boolean isAdmin = currentUser.getRole().equals(Role.ADMIN);

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException(NOT_AUTHORIZED_TO_DELETE_THIS_PROFILE);
        }

        user.setIsEnabled(false);
        userRepository.save(user);
    }
}
