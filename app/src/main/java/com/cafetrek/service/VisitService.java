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

    /** Returns a visit for editing, but only if it belongs to userId (never leaks another user's record). */
    public VisitResponse getForEdit(Long visitId, Long userId) {
        CafeVisit v = ownedVisit(visitId, userId);
        return toResponse(v);
    }

    @Transactional
    public VisitResponse update(Long visitId, VisitRequest req, Long userId) {
        CafeVisit v = ownedVisit(visitId, userId);

        Coffee coffee = null;
        if (req.getCoffeeName() != null && !req.getCoffeeName().isBlank()) {
            coffee = resolveCoffee(req.getCoffeeName(), req.getCoffeeCountry());
        }

        v.setCoffee(coffee);
        v.setVisitedAt(req.getVisitedAt());
        v.setRating(req.getRating());
        v.setPrice(req.getPrice());
        v.setAcidity(req.isAcidity());
        v.setBitterness(req.isBitterness());
        v.setSweetness(req.isSweetness());
        v.setBody(req.isBody());
        v.setFruity(req.isFruity());
        v.setNutty(req.isNutty());
        v.setAtmosphere(req.getAtmosphere());
        v.setComment(req.getComment());

        return toResponse(visitRepository.save(v));
    }

    @Transactional
    public void delete(Long visitId, Long userId) {
        CafeVisit v = ownedVisit(visitId, userId);
        photoRepository.findByVisitId(visitId).forEach(photoRepository::delete);
        visitRepository.delete(v);
    }

    /** Loads a visit and throws unless it belongs to userId — keeps one user from editing/deleting another's records via a guessed URL. */
    private CafeVisit ownedVisit(Long visitId, Long userId) {
        CafeVisit v = visitRepository.findById(visitId)
                .orElseThrow(() -> new EntityNotFoundException("visit not found: " + visitId));
        if (!v.getUser().getId().equals(userId)) {
            throw new EntityNotFoundException("visit not found: " + visitId);
        }
        return v;
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
