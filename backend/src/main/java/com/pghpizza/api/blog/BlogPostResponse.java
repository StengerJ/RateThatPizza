package com.pghpizza.api.blog;

import java.time.Instant;
import java.util.UUID;

public record BlogPostResponse(
        UUID id,
        UUID authorId,
        String title,
        String slug,
        String location,
        String body,
        String youtubeUrl,
        String youtubeVideoId,
        String author,
        Instant createdAt
) {
    public static BlogPostResponse from(BlogPostEntity post) {
        return new BlogPostResponse(
                post.getId(),
                post.getAuthor().getId(),
                post.getTitle(),
                post.getSlug(),
                post.getLocation(),
                post.getBody(),
                post.getYoutubeUrl(),
                post.getYoutubeVideoId(),
                post.getAuthor().getDisplayName(),
                post.getCreatedAt());
    }
}
