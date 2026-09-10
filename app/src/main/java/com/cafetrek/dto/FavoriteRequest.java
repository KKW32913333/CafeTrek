package com.cafetrek.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long cafeId;
}
