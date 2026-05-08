package com.pghpizza.api.blog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogPostRepository extends JpaRepository<BlogPostEntity, UUID> {
    List<BlogPostEntity> findAllByOrderByCreatedAtDesc();

    List<BlogPostEntity> findAllByAuthor_IdOrderByCreatedAtDesc(UUID authorId);

    long countByAuthor_Id(UUID authorId);

    Optional<BlogPostEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
