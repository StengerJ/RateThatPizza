package com.pghpizza.api.rating;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record RatingResponse(
        UUID id,
        UUID creatorId,
        String creator,
        String restaurantName,
        String location,
        String sauce,
        String toppings,
        String crust,
        BigDecimal overallRating,
        BigDecimal affordabilityRating,
        String comments,
        Instant createdAt
) {
    public static RatingResponse from(RatingEntity rating) {
        return new RatingResponse(
                rating.getId(),
                rating.getCreator().getId(),
                rating.getCreator().getDisplayName(),
                rating.getRestaurantName(),
                rating.getLocation(),
                rating.getSauce(),
                rating.getToppings(),
                rating.getCrust(),
                rating.getOverallRating(),
                rating.getAffordabilityRating(),
                rating.getComments(),
                rating.getCreatedAt());
    }
}
