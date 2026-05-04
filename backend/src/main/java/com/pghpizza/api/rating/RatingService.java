package com.pghpizza.api.rating;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pghpizza.api.common.ForbiddenActionException;
import com.pghpizza.api.common.NotFoundException;
import com.pghpizza.api.common.TextSanitizer;
import com.pghpizza.api.user.UserEntity;
import com.pghpizza.api.user.UserRole;
import com.pghpizza.api.user.UserStatus;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;

    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Transactional(readOnly = true)
    public List<RatingResponse> listRatings() {
        return ratingRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(RatingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public RatingResponse getRating(UUID id) {
        return ratingRepository.findById(id)
                .map(RatingResponse::from)
                .orElseThrow(() -> new NotFoundException("Rating not found"));
    }

    @Transactional
    public RatingResponse createRating(RatingRequest request, UserEntity user) {
        requirePublisher(user);
        RatingEntity rating = new RatingEntity();
        rating.setCreator(user);
        applyRequest(rating, request);
        return RatingResponse.from(ratingRepository.save(rating));
    }

    @Transactional
    public RatingResponse updateRating(UUID id, RatingRequest request, UserEntity user) {
        requirePublisher(user);
        RatingEntity rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating not found"));
        requireOwnerOrAdmin(rating, user);
        applyRequest(rating, request);
        return RatingResponse.from(ratingRepository.save(rating));
    }

    @Transactional
    public void deleteRating(UUID id, UserEntity user) {
        RatingEntity rating = ratingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Rating not found"));
        requireOwnerOrAdmin(rating, user);
        ratingRepository.delete(rating);
    }

    private void applyRequest(RatingEntity rating, RatingRequest request) {
        rating.setRestaurantName(TextSanitizer.trim(request.restaurantName()));
        rating.setSauce(TextSanitizer.trim(request.sauce()));
        rating.setToppings(TextSanitizer.trim(request.toppings()));
        rating.setCrust(TextSanitizer.trim(request.crust()));
        rating.setOverallRating(request.overallRating());
        rating.setComments(TextSanitizer.trim(request.comments()));
    }

    private void requirePublisher(UserEntity user) {
        boolean allowed = user.getStatus() == UserStatus.ACTIVE
                && (user.getRole() == UserRole.CONTRIBUTOR || user.getRole() == UserRole.ADMIN);
        if (!allowed) {
            throw new ForbiddenActionException("User cannot publish ratings");
        }
    }

    private void requireOwnerOrAdmin(RatingEntity rating, UserEntity user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ForbiddenActionException("User cannot modify this rating");
        }

        if (user.getRole() != UserRole.ADMIN && !rating.getCreator().getId().equals(user.getId())) {
            throw new ForbiddenActionException("User cannot modify this rating");
        }
    }
}
