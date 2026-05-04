package com.pghpizza.api.rating;

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
@RequestMapping("/api/ratings")
public class RatingController {
    private final RatingService ratingService;
    private final CurrentUserService currentUserService;

    public RatingController(RatingService ratingService, CurrentUserService currentUserService) {
        this.ratingService = ratingService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<RatingResponse> listRatings() {
        return ratingService.listRatings();
    }

    @GetMapping("/{id}")
    public RatingResponse getRating(@PathVariable UUID id) {
        return ratingService.getRating(id);
    }

    @PostMapping
    public RatingResponse createRating(@Valid @RequestBody RatingRequest request) {
        return ratingService.createRating(request, currentUserService.requireCurrentUser());
    }

    @PutMapping("/{id}")
    public RatingResponse updateRating(@PathVariable UUID id, @Valid @RequestBody RatingRequest request) {
        return ratingService.updateRating(id, request, currentUserService.requireCurrentUser());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRating(@PathVariable UUID id) {
        ratingService.deleteRating(id, currentUserService.requireCurrentUser());
        return ResponseEntity.noContent().build();
    }
}
