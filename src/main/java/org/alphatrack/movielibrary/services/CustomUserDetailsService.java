package org.alphatrack.movielibrary.services;

import jakarta.persistence.EntityNotFoundException;
import org.alphatrack.movielibrary.models.User;
import org.alphatrack.movielibrary.repositories.contracts.UserRepository;
import org.alphatrack.movielibrary.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with username %s not found", username)));

        Set<GrantedAuthority> authorities = Set.of(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new CustomUserDetails(user);
    }
}
