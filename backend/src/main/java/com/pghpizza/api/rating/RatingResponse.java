package com.pghpizza.api.rating;

import java.time.Instant;
import java.util.UUID;

public record RatingResponse(
        UUID id,
        String restaurantName,
        String sauce,
        String toppings,
        String crust,
        int overallRating,
        String comments,
        Instant createdAt
) {
    public static RatingResponse from(RatingEntity rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getRestaurantName(),
                rating.getSauce(),
                rating.getToppings(),
                rating.getCrust(),
                rating.getOverallRating(),
                rating.getComments(),
                rating.getCreatedAt());
    }
}
