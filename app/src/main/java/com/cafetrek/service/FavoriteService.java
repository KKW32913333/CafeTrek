package com.cafetrek.service;

import com.cafetrek.domain.Cafe;
import com.cafetrek.domain.Favorite;
import com.cafetrek.domain.User;
import com.cafetrek.dto.CafeResponse;
import com.cafetrek.repository.CafeRepository;
import com.cafetrek.repository.FavoriteRepository;
import com.cafetrek.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;

    public List<CafeResponse> list(Long userId) {
        return favoriteRepository.findByUserId(userId).stream()
                .map(f -> {
                    CafeResponse r = CafeResponse.from(f.getCafe());
                    r.setFavorite(true);
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void add(Long userId, Long cafeId) {
        if (favoriteRepository.findByUserIdAndCafeId(userId, cafeId).isPresent()) return; // already a favorite
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("user not found"));
        Cafe cafe = cafeRepository.findById(cafeId).orElseThrow(() -> new EntityNotFoundException("cafe not found"));
        Favorite f = new Favorite();
        f.setUser(user);
        f.setCafe(cafe);
        favoriteRepository.save(f);
    }

    @Transactional
    public void remove(Long userId, Long cafeId) {
        favoriteRepository.deleteByUserIdAndCafeId(userId, cafeId);
    }
}
