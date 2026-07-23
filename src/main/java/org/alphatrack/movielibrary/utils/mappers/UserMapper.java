package org.alphatrack.movielibrary.utils.mappers;

import org.alphatrack.movielibrary.dtos.UserRegisterDto;
import org.alphatrack.movielibrary.dtos.UserResponseDto;
import org.alphatrack.movielibrary.models.User;
import org.alphatrack.movielibrary.models.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserMapper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDto userToDto(User currentUser) {
        return UserResponseDto.builder()
                .id(currentUser.getId())
                .username(currentUser.getUsername())
                .email(currentUser.getEmail())
                .build();
    }

    public User dtoToUser(UserRegisterDto userRegisterDto) {
        return User.builder()
                .username(userRegisterDto.getUsername())
                .firstName(userRegisterDto.getFirstName())
                .lastName(userRegisterDto.getLastName())
                .email(userRegisterDto.getEmail())
                .password(passwordEncoder.encode(userRegisterDto.getPassword()))
                .isBlocked(false)
                .isEnabled(true)
                .role(Role.USER)
                .build();
    }

    public List<UserResponseDto> usersListToResponseDtoList (List<User> users){
        return users.stream()
                .map(this::userToDto)
                .toList();
    }
}
