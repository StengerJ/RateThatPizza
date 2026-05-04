package com.pghpizza.api.blog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BlogPostRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 180) @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$") String slug,
        @NotBlank @Size(max = 180) String location,
        @NotBlank @Size(max = 20000) String body,
        @Size(max = 500) String youtubeUrl,
        @Size(max = 20) @Pattern(regexp = "^[A-Za-z0-9_-]{11}$|^$") String youtubeVideoId
) {
}
