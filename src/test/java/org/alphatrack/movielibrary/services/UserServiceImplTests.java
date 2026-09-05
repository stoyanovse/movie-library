package org.alphatrack.movielibrary.services;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.alphatrack.movielibrary.dtos.UserRegisterDto;
import org.alphatrack.movielibrary.dtos.UserUpdateDto;
import org.alphatrack.movielibrary.dtos.filters.UserFilterOptions;
import org.alphatrack.movielibrary.models.User;
import org.alphatrack.movielibrary.models.enums.Role;
import org.alphatrack.movielibrary.repositories.contracts.UserRepository;
import org.alphatrack.movielibrary.utils.mappers.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;


import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User mockUser;
    private UserFilterOptions userFilterOptions;

    @BeforeEach
    public void init() {
        mockUser = Mockito.mock(User.class);
        userFilterOptions = Mockito.mock(UserFilterOptions.class);
    }

    @Test
    public void getAll_Should_returnListOfUsers() {
        Mockito.when(mockUser.getLastName())
                .thenReturn("Ivanov");
        Mockito.when(userRepository.findAll(userFilterOptions))
                .thenReturn(List.of(mockUser));
        List<User> userList = userService.getAll(userFilterOptions);

        Assertions.assertEquals("Ivanov", userList.get(0).getLastName());
    }

    @Test
    public void search_Should_Return_Throw_WhenInvalidInput() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.search(userFilterOptions));
    }

    @Test
    public void search_Should_Return_ListWhenInputIsValid() {
        Mockito.when(userFilterOptions.getFirstName())
                .thenReturn(Optional.of("Ivan"));
        Mockito.when(mockUser.getFirstName()).thenReturn("Ivan");
        Mockito.when(userRepository.findAll(userFilterOptions))
                .thenReturn(List.of(mockUser));

        List<User> userList = userService.search(userFilterOptions);

        Assertions.assertEquals("Ivan", userList.get(0).getFirstName());
    }

    @Test
    public void getById_Should_Throw_WhenUserNotFound() {
        Mockito.when(mockUser.getId())
                .thenReturn(1L);
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.getById(mockUser.getId()));
    }

    @Test
    public void getById_Should_ReturnUserWhenFound() {
        Mockito.when(mockUser.getId())
                .thenReturn(1L);
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        User result = userService.getById(1L);

        Assertions.assertEquals(result.getId(), mockUser.getId());

        Mockito.verify(userRepository,Mockito.times(1)).findById(mockUser.getId());
    }

    @Test
    public void promoteToAdmin_Should_Throw_WhenUserNotFound() {
        User user = new User();
        user.setRole(Role.ADMIN);
        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.promoteToAdmin(1L, user));
    }

    @Test
    public void promoteToAdmin_Should_Throw_WhenUserIsNotAdmin() {
        User user = new User();
        user.setRole(Role.USER);
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        Assertions.assertThrows(AccessDeniedException.class, () -> userService.promoteToAdmin(1L, user));
    }

    @Test
    public void promoteToAdmin_Should_Throw_WhenUserIsDeleted() {
        User user = new User();
        user.setRole(Role.ADMIN);
        Mockito.when(mockUser.getIsEnabled())
                .thenReturn(false);
        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(mockUser));

        Assertions.assertThrows(AccessDeniedException.class, () -> userService.promoteToAdmin(1L, user));
    }

    @Test
    public void promoteToAdmin_Should_PromoteAndCallRepository() {
        User user = new User();
        user.setRole(Role.ADMIN);
        User userToPromote = new User();
        userToPromote.setIsEnabled(true);
        userToPromote.setIsBlocked(true);
        userToPromote.setRole(Role.USER);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(userToPromote));

        userService.promoteToAdmin(1L, user);

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.times(1)).save(argumentCaptor.capture());

        User result = argumentCaptor.getValue();

        Assertions.assertFalse(result.getIsBlocked());
        Assertions.assertEquals(Role.ADMIN, result.getRole());
    }

    @Test
    public void update_Should_Throw_When_UserNotOwner() {
        UserUpdateDto userUpdateDto = new UserUpdateDto();
        userUpdateDto.setFirstName("Ivan");

        Mockito.when(mockUser.getId())
                .thenReturn(2L);

        Assertions.assertThrows(AccessDeniedException.class, () -> userService.update(1L, userUpdateDto, mockUser));
    }

    @Test
    public void update_Should_CallRepository() {
        UserUpdateDto userUpdateDto = new UserUpdateDto("Ivan", "George");
        User user = new User(1L, "Marto", "Martin", "Martinchev","userpass123",Role.USER, "marticha@gmail.com" ,false, true);

        userService.update(1L, userUpdateDto, user);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.times(1)).save(userArgumentCaptor.capture());

        User result = userArgumentCaptor.getValue();

        Assertions.assertEquals("Ivan", result.getFirstName());
        Assertions.assertEquals("George", result.getLastName());
    }

    @Test
    public void create_Should_Throw_When_UsernameExists() {
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setUsername("vankicha");

        Mockito.when(userRepository.findUserByUsername("vankicha"))
                .thenReturn(Optional.of(mockUser));

        Assertions.assertThrows(EntityExistsException.class, () -> userService.create(userRegisterDto));
    }


    @Test
    public void create_Should_Throw_When_EmailExists() {
        UserRegisterDto userRegisterDto = new UserRegisterDto();
        userRegisterDto.setEmail("vankicha@gmail.com");

        Mockito.when(userRepository.findUserByEmail("vankicha@gmail.com"))
                .thenReturn(Optional.of(mockUser));

        Assertions.assertThrows(EntityExistsException.class, () -> userService.create(userRegisterDto));
    }

    @Test
    public void create_Should_MapAndCallRepository() {
        UserRegisterDto userRegisterDto = new UserRegisterDto(
                "Ivan", "Martinchev" , "vankicha" , "example@gmail.com", "userpass123");

        User dummyMappedUser = new User();
        dummyMappedUser.setFirstName("Ivan");
        dummyMappedUser.setLastName("Martinchev");
        dummyMappedUser.setEmail("example@gmail.com");
        dummyMappedUser.setUsername("vankicha");

        Mockito.when(userMapper.dtoToUser(userRegisterDto))
                .thenReturn(dummyMappedUser);

        userService.create(userRegisterDto);

        ArgumentCaptor<User> argumentCaptor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.times(1)).save(argumentCaptor.capture());

        User result = argumentCaptor.getValue();

        Assertions.assertEquals("Ivan", result.getFirstName());
        Assertions.assertEquals("Martinchev", result.getLastName());
        Assertions.assertEquals("example@gmail.com", result.getEmail());
        Assertions.assertEquals("vankicha", result.getUsername());
    }

    @Test
    public void delete_Should_Throw_When_UserNotFound() {
        User user = new User();
        user.setRole(Role.ADMIN);

        Assertions.assertThrows(EntityNotFoundException.class, () -> userService.delete(1L, user));
    }

    @Test
    public void delete_Should_Throw_When_UserIsNotAdminAndNotOwner() {
        User targetUser = new User(1L, "target", "Target", "User", "pass123", Role.USER, "target@gmail.com", false, true);
        User currentUser = new User(2L, "current", "Current", "User", "pass123", Role.USER, "current@gmail.com", false, true);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(targetUser));

        Assertions.assertThrows(AccessDeniedException.class, () -> userService.delete(1L, currentUser));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void delete_Should_CallRepository_When_UserIsOwner() {
        User targetUser = new User(1L, "owner", "Owner", "User", "pass123", Role.USER, "owner@gmail.com", false, true);
        User currentUser = new User(1L, "owner", "Owner", "User", "pass123", Role.USER, "owner@gmail.com", false, true);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(targetUser));

        userService.delete(1L, currentUser);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.times(1)).save(captor.capture());

        User result = captor.getValue();
        Assertions.assertFalse(result.getIsEnabled());
    }

    @Test
    public void delete_Should_CallRepository_When_UserIsAdmin() {
        User targetUser = new User(1L, "target", "Target", "User", "pass123", Role.USER, "target@gmail.com", false, true);
        User currentUser = new User(2L, "admin", "Admin", "User", "pass123", Role.ADMIN, "admin@gmail.com", false, true);

        Mockito.when(userRepository.findById(1L))
                .thenReturn(Optional.of(targetUser));

        userService.delete(1L, currentUser);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        Mockito.verify(userRepository, Mockito.times(1)).save(captor.capture());

        User result = captor.getValue();
        Assertions.assertFalse(result.getIsEnabled());
    }

}