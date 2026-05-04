package com.pghpizza.api.user;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.pghpizza.api.blog.BlogPostEntity;
import com.pghpizza.api.rating.RatingEntity;

public record ContributorAdminResponse(
        UUID id,
        String email,
        String displayName,
        UserStatus status,
        Instant createdAt,
        int ratingCount,
        int blogPostCount,
        List<ContributorRatingResponse> ratings,
        List<ContributorBlogPostResponse> blogPosts
) {
    public static ContributorAdminResponse from(
            UserEntity user,
            List<RatingEntity> ratings,
            List<BlogPostEntity> blogPosts) {
        return new ContributorAdminResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getStatus(),
                user.getCreatedAt(),
                ratings.size(),
                blogPosts.size(),
                ratings.stream().map(ContributorRatingResponse::from).toList(),
                blogPosts.stream().map(ContributorBlogPostResponse::from).toList());
    }

    public record ContributorRatingResponse(
            UUID id,
            String restaurantName,
            String location,
            BigDecimal overallRating,
            BigDecimal affordabilityRating,
            Instant createdAt
    ) {
        public static ContributorRatingResponse from(RatingEntity rating) {
            return new ContributorRatingResponse(
                    rating.getId(),
                    rating.getRestaurantName(),
                    rating.getLocation(),
                    rating.getOverallRating(),
                    rating.getAffordabilityRating(),
                    rating.getCreatedAt());
        }
    }

    public record ContributorBlogPostResponse(
            UUID id,
            String title,
            String slug,
            String location,
            Instant createdAt
    ) {
        public static ContributorBlogPostResponse from(BlogPostEntity post) {
            return new ContributorBlogPostResponse(
                    post.getId(),
                    post.getTitle(),
                    post.getSlug(),
                    post.getLocation(),
                    post.getCreatedAt());
        }
    }
}
