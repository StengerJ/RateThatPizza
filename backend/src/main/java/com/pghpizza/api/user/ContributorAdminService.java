package com.pghpizza.api.user;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pghpizza.api.blog.BlogPostRepository;
import com.pghpizza.api.common.NotFoundException;
import com.pghpizza.api.rating.RatingRepository;

@Service
public class ContributorAdminService {
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final BlogPostRepository blogPostRepository;

    public ContributorAdminService(
            UserRepository userRepository,
            RatingRepository ratingRepository,
            BlogPostRepository blogPostRepository) {
        this.userRepository = userRepository;
        this.ratingRepository = ratingRepository;
        this.blogPostRepository = blogPostRepository;
    }

    @Transactional(readOnly = true)
    public List<ContributorAdminResponse> listContributors() {
        return userRepository.findAllByRoleAndStatusOrderByDisplayNameAsc(
                        UserRole.CONTRIBUTOR,
                        UserStatus.ACTIVE)
                .stream()
                .map(contributor -> ContributorAdminResponse.from(
                        contributor,
                        ratingRepository.findAllByCreator_IdOrderByCreatedAtDesc(contributor.getId()),
                        blogPostRepository.findAllByAuthor_IdOrderByCreatedAtDesc(contributor.getId())))
                .toList();
    }

    @Transactional
    public void disableContributor(UUID id) {
        UserEntity contributor = userRepository.findById(id)
                .filter(user -> user.getRole() == UserRole.CONTRIBUTOR)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Contributor not found"));

        contributor.setStatus(UserStatus.DISABLED);
        userRepository.save(contributor);
    }
}
