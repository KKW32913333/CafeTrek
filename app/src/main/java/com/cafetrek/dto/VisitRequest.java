package com.cafetrek.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/** Body for POST /api/visits — matches every field on the 記録 screen. */
@Getter
@Setter
public class VisitRequest {

    @NotNull
    private Long userId;

    @NotNull
    private Long cafeId;

    /** Free-text coffee name, e.g. "エチオピア イルガチェフェ". Resolved/created against the Coffee master table by name. */
    private String coffeeName;
    private String coffeeCountry;

    @NotNull
    private LocalDate visitedAt;

    @Min(1) @Max(5)
    private Integer rating;

    private Integer price;

    private boolean acidity;
    private boolean bitterness;
    private boolean sweetness;
    private boolean body;
    private boolean fruity;
    private boolean nutty;

    private String atmosphere;

    @Size(max = 1000)
    private String comment;

    /** Photo URLs already uploaded to object storage by the client. */
    private List<String> photoUrls;
}
