package com.pghpizza.api.blog;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pghpizza.api.security.CurrentUserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/blog-posts")
public class BlogPostController {
    private final BlogPostService blogPostService;
    private final CurrentUserService currentUserService;

    public BlogPostController(BlogPostService blogPostService, CurrentUserService currentUserService) {
        this.blogPostService = blogPostService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<BlogPostResponse> listPosts() {
        return blogPostService.listPosts();
    }

    @GetMapping("/{slug}")
    public BlogPostResponse getPost(@PathVariable String slug) {
        return blogPostService.getPost(slug);
    }

    @PostMapping
    public BlogPostResponse createPost(@Valid @RequestBody BlogPostRequest request) {
        return blogPostService.createPost(request, currentUserService.requireCurrentUser());
    }

    @PutMapping("/{id}")
    public BlogPostResponse updatePost(@PathVariable UUID id, @Valid @RequestBody BlogPostRequest request) {
        return blogPostService.updatePost(id, request, currentUserService.requireCurrentUser());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable UUID id) {
        blogPostService.deletePost(id, currentUserService.requireCurrentUser());
        return ResponseEntity.noContent().build();
    }
}
