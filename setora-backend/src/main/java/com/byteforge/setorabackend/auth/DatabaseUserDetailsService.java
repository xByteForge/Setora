package com.byteforge.setorabackend.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DatabaseUserDetailsService implements UserDetailsService { // this implements spring's security interface for loading user credentials
    private final UserRepository userRepository;                        // spring needs an object of type UserDetails for security, and this converts our format into that

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        if (email.isBlank())
            throw new UsernameNotFoundException("Invalid credentials");

        String normalizedEmail = email.trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail).orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (user.getRole() == null)
            throw new UsernameNotFoundException("Invalid account");

        // guest must not log in, we can change this by deleting here
        if ("ghost@gmail.com".equalsIgnoreCase(user.getEmail()) || user.isDeleted())
            throw new UsernameNotFoundException("Invalid credentials");

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}
