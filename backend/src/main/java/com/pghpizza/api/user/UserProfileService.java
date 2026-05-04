package com.pghpizza.api.user;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pghpizza.api.blog.BlogPostRepository;
import com.pghpizza.api.common.NotFoundException;
import com.pghpizza.api.common.TextSanitizer;
import com.pghpizza.api.rating.RatingRepository;

@Service
public class UserProfileService {
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final BlogPostRepository blogPostRepository;

    public UserProfileService(
            UserRepository userRepository,
            RatingRepository ratingRepository,
            BlogPostRepository blogPostRepository) {
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
        this.blogPostRepository = blogPostRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        return UserProfileResponse.from(
                user,
                ratingRepository.findAllByCreator_IdOrderByCreatedAtDesc(user.getId()),
                blogPostRepository.findAllByAuthor_IdOrderByCreatedAtDesc(user.getId()));
    }

    @Transactional
    public UserProfileResponse updateProfile(UserProfileUpdateRequest request, UserEntity currentUser) {
        UserEntity user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Profile not found"));

        String bio = TextSanitizer.trim(request.bio());

        user.setDisplayName(TextSanitizer.trim(request.displayName()));
        user.setProfileBio(bio == null ? "" : bio);
        user.setProfilePictureUrl(TextSanitizer.emptyToNull(request.profilePictureUrl()));

        UserEntity savedUser = userRepository.save(user);
        return getProfile(savedUser.getId());
    }
}
