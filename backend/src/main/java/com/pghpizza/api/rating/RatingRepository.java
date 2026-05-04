package com.pghpizza.api.rating;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RatingRepository extends JpaRepository<RatingEntity, UUID> {
    List<RatingEntity> findAllByOrderByCreatedAtDesc();

    List<RatingEntity> findAllByCreator_IdOrderByCreatedAtDesc(UUID creatorId);
}
