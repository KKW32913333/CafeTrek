package com.cafetrek.controller;

import com.cafetrek.domain.User;
import com.cafetrek.dto.CafeResponse;
import com.cafetrek.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Server-rendered pages (Thymeleaf), one per screen from the spec:
 * ホーム / 地図 / カフェ詳細 / 記録 / マイページ. Mirrors what the old
 * Expo screens did, but rendered on the server like Linkle.
 */
@Controller
@RequiredArgsConstructor
public class PageController {

    private final CafeService cafeService;
    private final FavoriteService favoriteService;
    private final VisitService visitService;
    private final StatsService statsService;
    private final CurrentUserService currentUserService;

    @GetMapping("/")
    public String home(Model model, Authentication auth) {
        User user = currentUserService.get(auth);
        List<CafeResponse> cafes = cafeService.search(null, null, null, user.getId());
        model.addAttribute("user", user);
        model.addAttribute("cafes", cafes);
        return "home";
    }

    @GetMapping("/map")
    public String map(Model model, Authentication auth) {
        User user = currentUserService.get(auth);
        model.addAttribute("cafes", cafeService.search(null, null, null, user.getId()));
        return "map";
    }

    @GetMapping("/cafes/{id}")
    public String cafeDetail(@PathVariable Long id, Model model, Authentication auth) {
        User user = currentUserService.get(auth);
        model.addAttribute("cafe", cafeService.getById(id, user.getId()));
        model.addAttribute("visits", visitService.history(user.getId(), id));
        return "cafe-detail";
    }

    @PostMapping("/cafes/{id}/favorite")
    public String toggleFavorite(@PathVariable Long id, @RequestParam String redirectTo, Authentication auth) {
        User user = currentUserService.get(auth);
        CafeResponse cafe = cafeService.getById(id, user.getId());
        if (cafe.isFavorite()) favoriteService.remove(user.getId(), id);
        else favoriteService.add(user.getId(), id);
        return "redirect:" + redirectTo;
    }

    @GetMapping("/record/{cafeId}")
    public String recordForm(@PathVariable Long cafeId, Model model, Authentication auth) {
        User user = currentUserService.get(auth);
        model.addAttribute("cafe", cafeService.getById(cafeId, user.getId()));
        return "record";
    }

    @PostMapping("/record/{cafeId}")
    public String recordSave(
            @PathVariable Long cafeId,
            @RequestParam(required = false) String coffeeName,
            @RequestParam String visitedAt,
            @RequestParam(defaultValue = "5") Integer rating,
            @RequestParam(required = false) Integer price,
            @RequestParam(name = "taste", required = false) List<String> taste,
            @RequestParam(required = false) String atmosphere,
            @RequestParam(required = false) String comment,
            Authentication auth) {

        User user = currentUserService.get(auth);
        taste = taste == null ? List.of() : taste;

        com.cafetrek.dto.VisitRequest req = new com.cafetrek.dto.VisitRequest();
        req.setUserId(user.getId());
        req.setCafeId(cafeId);
        req.setCoffeeName(coffeeName);
        req.setVisitedAt(java.time.LocalDate.parse(visitedAt));
        req.setRating(rating);
        req.setPrice(price);
        req.setAcidity(taste.contains("acidity"));
        req.setBitterness(taste.contains("bitterness"));
        req.setSweetness(taste.contains("sweetness"));
        req.setBody(taste.contains("body"));
        req.setFruity(taste.contains("fruity"));
        req.setNutty(taste.contains("nutty"));
        req.setAtmosphere(atmosphere);
        req.setComment(comment);

        visitService.create(req);
        return "redirect:/mypage?saved";
    }

    @GetMapping("/mypage")
    public String myPage(Model model, Authentication auth) {
        User user = currentUserService.get(auth);
        model.addAttribute("user", user);
        model.addAttribute("stats", statsService.get(user.getId()));
        return "mypage";
    }
}
