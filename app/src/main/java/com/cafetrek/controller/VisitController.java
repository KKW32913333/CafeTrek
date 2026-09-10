package com.cafetrek.controller;

import com.cafetrek.dto.VisitRequest;
import com.cafetrek.dto.VisitResponse;
import com.cafetrek.service.VisitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    /** The 記録 screen's "保存する" button posts here. */
    @PostMapping
    public VisitResponse create(@Valid @RequestBody VisitRequest req) {
        return visitService.create(req);
    }

    /** GET /api/visits?userId=&cafeId= — 訪問履歴, and the coffee log shown on カフェ詳細. */
    @GetMapping
    public List<VisitResponse> history(@RequestParam Long userId, @RequestParam(required = false) Long cafeId) {
        return visitService.history(userId, cafeId);
    }
}
