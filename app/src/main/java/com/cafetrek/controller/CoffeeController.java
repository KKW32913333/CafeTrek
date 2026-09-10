package com.cafetrek.controller;

import com.cafetrek.dto.CoffeeDto;
import com.cafetrek.repository.CoffeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/coffees")
@RequiredArgsConstructor
public class CoffeeController {

    private final CoffeeRepository coffeeRepository;

    /** Master list, used to power autocomplete on the 記録 screen's coffee-name field. */
    @GetMapping
    public List<CoffeeDto> list() {
        return coffeeRepository.findAll().stream().map(CoffeeDto::from).collect(Collectors.toList());
    }
}
