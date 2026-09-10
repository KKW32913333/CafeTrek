package com.cafetrek.service;

import com.cafetrek.domain.User;
import com.cafetrek.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/** Resolves the logged-in Spring Security principal (email) to our User entity. */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepository userRepository;

    public User get(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("logged-in user not found"));
    }
}
