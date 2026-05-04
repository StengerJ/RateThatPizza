package com.pghpizza.api.user;

import java.util.List;
import java.util.UUID;

import com.pghpizza.api.blog.BlogPostEntity;
import com.pghpizza.api.blog.BlogPostResponse;
import com.pghpizza.api.rating.RatingEntity;
import com.pghpizza.api.rating.RatingResponse;

public record UserProfileResponse(
        UUID id,
        String displayName,
        String bio,
        String profilePictureUrl,
        List<RatingResponse> ratings,
        List<BlogPostResponse> blogPosts
) {
    public static UserProfileResponse from(
            UserEntity user,
            List<RatingEntity> ratings,
            List<BlogPostEntity> blogPosts) {
        return new UserProfileResponse(
                user.getId(),
                user.getDisplayName(),
                user.getProfileBio(),
                user.getProfilePictureUrl(),
                ratings.stream().map(RatingResponse::from).toList(),
                blogPosts.stream().map(BlogPostResponse::from).toList());
    }
}
