package com.pghpizza.api.rating;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RatingResponse(
        UUID id,
        UUID creatorId,
        String creator,
        String restaurantName,
        String sauce,
        String toppings,
        String crust,
        BigDecimal overallRating,
        String comments,
        Instant createdAt
) {
    public static RatingResponse from(RatingEntity rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getCreator().getId(),
                rating.getCreator().getDisplayName(),
                rating.getRestaurantName(),
                rating.getSauce(),
                rating.getToppings(),
                rating.getCrust(),
                rating.getOverallRating(),
                rating.getComments(),
                rating.getCreatedAt());
    }
}
