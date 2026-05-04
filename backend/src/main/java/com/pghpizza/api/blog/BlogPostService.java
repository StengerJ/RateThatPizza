package com.pghpizza.api.blog;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pghpizza.api.common.ConflictException;
import com.pghpizza.api.common.ForbiddenActionException;
import com.pghpizza.api.common.NotFoundException;
import com.pghpizza.api.common.TextSanitizer;
import com.pghpizza.api.user.UserEntity;
import com.pghpizza.api.user.UserRole;
import com.pghpizza.api.user.UserStatus;

@Service
public class BlogPostService {
    private final BlogPostRepository blogPostRepository;

    public BlogPostService(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    @Transactional(readOnly = true)
    public List<BlogPostResponse> listPosts() {
        return blogPostRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(BlogPostResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BlogPostResponse getPost(String slug) {
        return blogPostRepository.findBySlug(slug)
                .map(BlogPostResponse::from)
                .orElseThrow(() -> new NotFoundException("Blog post not found"));
    }

    @Transactional
    public BlogPostResponse createPost(BlogPostRequest request, UserEntity user) {
        requirePublisher(user);
        String slug = TextSanitizer.trim(request.slug());
        if (blogPostRepository.existsBySlug(slug)) {
            throw new ConflictException("Blog post slug already exists");
        }

        BlogPostEntity post = new BlogPostEntity();
        post.setAuthor(user);
        applyRequest(post, request);
        return BlogPostResponse.from(blogPostRepository.save(post));
    }

    @Transactional
    public BlogPostResponse updatePost(UUID id, BlogPostRequest request, UserEntity user) {
        requirePublisher(user);
        BlogPostEntity post = blogPostRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog post not found"));
        requireOwnerOrAdmin(post, user);

        String newSlug = TextSanitizer.trim(request.slug());
        blogPostRepository.findBySlug(newSlug)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException("Blog post slug already exists");
                });

        applyRequest(post, request);
        return BlogPostResponse.from(blogPostRepository.save(post));
    }

    @Transactional
    public void deletePost(UUID id, UserEntity user) {
        BlogPostEntity post = blogPostRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Blog post not found"));
        requireOwnerOrAdmin(post, user);
        blogPostRepository.delete(post);
    }

    private void applyRequest(BlogPostEntity post, BlogPostRequest request) {
        post.setTitle(TextSanitizer.trim(request.title()));
        post.setSlug(TextSanitizer.trim(request.slug()));
        post.setLocation(TextSanitizer.trim(request.location()));
        post.setBody(TextSanitizer.trim(request.body()));
        post.setYoutubeUrl(TextSanitizer.emptyToNull(request.youtubeUrl()));
        post.setYoutubeVideoId(TextSanitizer.emptyToNull(request.youtubeVideoId()));
    }

    private void requirePublisher(UserEntity user) {
        boolean allowed = user.getStatus() == UserStatus.ACTIVE
                && (user.getRole() == UserRole.CONTRIBUTOR || user.getRole() == UserRole.ADMIN);
        if (!allowed) {
            throw new ForbiddenActionException("User cannot publish blog posts");
        }
    }

    private void requireOwnerOrAdmin(BlogPostEntity post, UserEntity user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenActionException("User cannot modify this blog post");
        }

        if (user.getRole() != UserRole.ADMIN && !post.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenActionException("User cannot modify this blog post");
        }
    }
}
