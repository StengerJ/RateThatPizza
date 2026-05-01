package com.pghpizza.api.blog;

import java.time.Instant;
import java.util.UUID;

public record BlogPostResponse(
        UUID id,
        String title,
        String slug,
        String body,
        String youtubeUrl,
        String youtubeVideoId,
        String author,
        Instant createdAt
) {
    public static BlogPostResponse from(BlogPostEntity post) {
        return new BlogPostResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getBody(),
                post.getYoutubeUrl(),
                post.getYoutubeVideoId(),
                post.getAuthor().getDisplayName(),
                post.getCreatedAt());
    }
}
