package com.cafetrek.controller;

import com.cafetrek.domain.User;
import com.cafetrek.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * MVP has no login (spec section 20: ユーザー登録 is Phase 3). The frontend
     * always uses userId=1, the demo user seeded in data.sql.
     */
    @GetMapping("/{id}")
    public User get(@PathVariable Long id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("user not found: " + id));
    }
}
