package org.alphatrack.movielibrary.services;


import jakarta.persistence.EntityNotFoundException;
import org.alphatrack.movielibrary.models.User;
import org.alphatrack.movielibrary.repositories.contracts.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    public void loadUserByUsername_Should_Throw_When_UserNotFound() {
        Assertions.assertThrows(EntityNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("unknownUser"));
    }

    @Test
    public void loadUserByUsername_Should_ReturnUserDetails_When_UserFound() {
        User user = new User();
        user.setUsername("testUser");

        Mockito.when(userRepository.findUserByUsername("testUser")).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("testUser");

        Assertions.assertEquals(user.getUsername(), result.getUsername());
    }
}