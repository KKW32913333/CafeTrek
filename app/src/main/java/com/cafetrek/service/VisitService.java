package com.cafetrek.service;

import com.cafetrek.domain.*;
import com.cafetrek.dto.VisitRequest;
import com.cafetrek.dto.VisitResponse;
import com.cafetrek.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitService {

    private final CafeVisitRepository visitRepository;
    private final UserRepository userRepository;
    private final CafeRepository cafeRepository;
    private final CoffeeRepository coffeeRepository;
    private final PhotoRepository photoRepository;

    @Transactional
    public VisitResponse create(VisitRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("user not found: " + req.getUserId()));
        Cafe cafe = cafeRepository.findById(req.getCafeId())
                .orElseThrow(() -> new EntityNotFoundException("cafe not found: " + req.getCafeId()));

        Coffee coffee = null;
        if (req.getCoffeeName() != null && !req.getCoffeeName().isBlank()) {
            coffee = resolveCoffee(req.getCoffeeName(), req.getCoffeeCountry());
        }

        CafeVisit visit = new CafeVisit();
        visit.setUser(user);
        visit.setCafe(cafe);
        visit.setCoffee(coffee);
        visit.setVisitedAt(req.getVisitedAt());
        visit.setRating(req.getRating());
        visit.setPrice(req.getPrice());
        visit.setAcidity(req.isAcidity());
        visit.setBitterness(req.isBitterness());
        visit.setSweetness(req.isSweetness());
        visit.setBody(req.isBody());
        visit.setFruity(req.isFruity());
        visit.setNutty(req.isNutty());
        visit.setAtmosphere(req.getAtmosphere());
        visit.setComment(req.getComment());

        CafeVisit saved = visitRepository.save(visit);

        if (req.getPhotoUrls() != null) {
            for (String url : req.getPhotoUrls()) {
                Photo p = new Photo();
                p.setUser(user);
                p.setCafe(cafe);
                p.setVisit(saved);
                p.setImageUrl(url);
                photoRepository.save(p);
            }
        }

        return toResponse(saved);
    }

    public List<VisitResponse> history(Long userId, Long cafeId) {
        List<CafeVisit> visits = (cafeId == null)
                ? visitRepository.findByUserIdOrderByVisitedAtDesc(userId)
                : visitRepository.findByUserIdAndCafeIdOrderByVisitedAtDesc(userId, cafeId);
        return visits.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private Coffee resolveCoffee(String name, String country) {
        // MVP: match by name; create a new master record if unseen.
        List<Coffee> all = coffeeRepository.findAll();
        Optional<Coffee> existing = all.stream()
                .filter(c -> c.getName() != null && c.getName().equals(name))
                .findFirst();
        if (existing.isPresent()) return existing.get();

        Coffee c = new Coffee();
        c.setName(name);
        c.setCountry(country);
        return coffeeRepository.save(c);
    }

    private VisitResponse toResponse(CafeVisit v) {
        VisitResponse r = VisitResponse.from(v);
        r.setPhotoUrls(photoRepository.findByVisitId(v.getId()).stream()
                .map(Photo::getImageUrl).collect(Collectors.toList()));
        return r;
    }
}
