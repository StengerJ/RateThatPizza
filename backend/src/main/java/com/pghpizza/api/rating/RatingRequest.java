package com.pghpizza.api.rating;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RatingRequest(
        @NotBlank @Size(max = 160) String restaurantName,
        @NotBlank @Size(max = 120) String sauce,
        @NotBlank @Size(max = 160) String toppings,
        @NotBlank @Size(max = 120) String crust,
        @NotNull @DecimalMin("1.0") @DecimalMax("10.0") @Digits(integer = 2, fraction = 2) BigDecimal overallRating,
        @NotBlank @Size(max = 5000) String comments
) {
}
