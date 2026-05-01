package com.pghpizza.api.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RatingRequest(
        @NotBlank @Size(max = 160) String restaurantName,
        @NotBlank @Size(max = 120) String sauce,
        @NotBlank @Size(max = 160) String toppings,
        @NotBlank @Size(max = 120) String crust,
        @NotNull @Min(1) @Max(10) Integer overallRating,
        @NotBlank @Size(max = 5000) String comments
) {
}
