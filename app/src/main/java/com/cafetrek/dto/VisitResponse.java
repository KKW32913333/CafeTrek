package com.cafetrek.dto;

import com.cafetrek.domain.CafeVisit;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class VisitResponse {
    private Long id;
    private Long cafeId;
    private String cafeName;
    private String coffeeName;
    private String coffeeCountry;
    private LocalDate visitedAt;
    private Integer rating;
    private Integer price;
    private List<String> tasteTags;
    private String atmosphere;
    private String comment;
    private List<String> photoUrls;

    public static VisitResponse from(CafeVisit v) {
        VisitResponse r = new VisitResponse();
        r.setId(v.getId());
        r.setCafeId(v.getCafe().getId());
        r.setCafeName(v.getCafe().getName());
        if (v.getCoffee() != null) {
            r.setCoffeeName(v.getCoffee().getName());
            r.setCoffeeCountry(v.getCoffee().getCountry());
        }
        r.setVisitedAt(v.getVisitedAt());
        r.setRating(v.getRating());
        r.setPrice(v.getPrice());
        r.setAtmosphere(v.getAtmosphere());
        r.setComment(v.getComment());

        java.util.ArrayList<String> tags = new java.util.ArrayList<>();
        if (v.isAcidity()) tags.add("酸味");
        if (v.isBitterness()) tags.add("苦味");
        if (v.isSweetness()) tags.add("甘み");
        if (v.isBody()) tags.add("コク");
        if (v.isFruity()) tags.add("フルーティー");
        if (v.isNutty()) tags.add("ナッツ系");
        r.setTasteTags(tags);
        return r;
    }
}
