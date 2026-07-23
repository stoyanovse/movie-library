package org.alphatrack.movielibrary.controllers.rest;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.alphatrack.movielibrary.dtos.UserRegisterDto;
import org.alphatrack.movielibrary.dtos.UserResponseDto;
import org.alphatrack.movielibrary.dtos.UserUpdateDto;
import org.alphatrack.movielibrary.dtos.filters.UserFilterOptions;
import org.alphatrack.movielibrary.security.CustomUserDetails;
import org.alphatrack.movielibrary.services.contracts.UserService;
import org.alphatrack.movielibrary.utils.mappers.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController

@RequestMapping("/api/users")
public class UserRestController {

    private final UserService userService;
    private final UserMapper userMapper;

    public UserRestController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }
    @Operation(summary = "show all users.ADMIN only")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAll(@ModelAttribute UserFilterOptions userFilterOptions) {
        List<UserResponseDto> list = userMapper.usersListToResponseDtoList(userService.getAll(userFilterOptions));
        return new  ResponseEntity<>(list,HttpStatus.OK);
    }

    @Operation(summary = "search for a specific user based on a criteria : username, first name, last name, or specific order")
    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDto>> searchUser(@ModelAttribute UserFilterOptions userFilterOptions) {
        List<UserResponseDto> list = userMapper.usersListToResponseDtoList(userService.search(userFilterOptions));
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Operation(summary = "search for a specific user based on id")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
        UserResponseDto userResponseDto = userMapper.userToDto(userService.getById(id));
        return new ResponseEntity<>(userResponseDto, HttpStatus.OK);
    }

    @Operation(summary = "create a new user")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        UserResponseDto userResponseDto = userMapper.userToDto(userService.create(userRegisterDto));
        return new ResponseEntity<>(userResponseDto,HttpStatus.CREATED);
    }

    @Operation(summary = "promote specific user to ADMIN based on ID. ADMIN only")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponseDto> promoteToAdmin(@PathVariable Long id,
                                                          @AuthenticationPrincipal CustomUserDetails currentUser) {
        UserResponseDto userResponseDto = userMapper.userToDto(userService.promoteToAdmin(id,currentUser.getUser()));
        return new ResponseEntity<>(userResponseDto,HttpStatus.OK);
    }

    @Operation(summary = "updates a user based on provided id.Owner only")
    @PreAuthorize("#currentUser.id == #id")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> update(@PathVariable Long id,
                                                  @AuthenticationPrincipal CustomUserDetails currentUser,
                                                  @Valid @RequestBody UserUpdateDto userUpdateDto) {
        UserResponseDto userResponseDto = userMapper.userToDto(userService.update(id, userUpdateDto, currentUser.getUser()));
        return new ResponseEntity<>(userResponseDto,HttpStatus.OK);
    }

    @Operation(summary = "delete a user. ADMIN or Owner only")
    @PreAuthorize("hasRole('ADMIN') or #currentUser.id == #id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails currentUser) {
        userService.delete(id, currentUser.getUser());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



}
