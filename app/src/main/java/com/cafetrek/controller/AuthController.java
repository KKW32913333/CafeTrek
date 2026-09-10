package com.cafetrek.controller;

import com.cafetrek.domain.User;
import com.cafetrek.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("name", "");
        model.addAttribute("email", "");
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String password,
            Model model) {

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "そのメールアドレスはすでに登録されています。");
            model.addAttribute("name", name);
            model.addAttribute("email", email);
            return "register";
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        userRepository.save(user);

        return "redirect:/login?registered";
    }
}
