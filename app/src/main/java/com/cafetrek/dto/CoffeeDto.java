package com.cafetrek.dto;

import com.cafetrek.domain.Coffee;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CoffeeDto {
    private Long id;
    private String name;
    private String country;
    private String region;
    private String roastLevel;
    private String processingMethod;

    public static CoffeeDto from(Coffee c) {
        CoffeeDto d = new CoffeeDto();
        d.setId(c.getId());
        d.setName(c.getName());
        d.setCountry(c.getCountry());
        d.setRegion(c.getRegion());
        d.setRoastLevel(c.getRoastLevel());
        d.setProcessingMethod(c.getProcessingMethod());
        return d;
    }
}
