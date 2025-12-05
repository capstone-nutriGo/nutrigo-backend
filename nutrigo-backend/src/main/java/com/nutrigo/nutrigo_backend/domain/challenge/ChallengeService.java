package com.nutrigo.nutrigo_backend.domain.challenge;

import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeCreateRequest;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeCreateResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeListResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeProgressResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.JoinChallengeResponse;
import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.domain.user.UserRepository;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions.Challenge.ChallengeNotFoundException;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions.User.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserChallengeRepository userChallengeRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChallengeCreateResponse createCustomChallenge(ChallengeCreateRequest request) {
        User user = getCurrentUser();
        LocalDateTime now = LocalDateTime.now();

        Challenge challenge = Challenge.builder()
                .code(generateCustomCode(user))
                .title(request.title())
                .description(request.description())
                .type(request.type())
                .durationDays(request.durationDays())
                .createdAt(now)
                .status("ACTIVE")
                .createdBy(user)
                .build();

        Challenge saved = challengeRepository.save(challenge);
        UserChallenge enrollment = createEnrollment(user, saved);

        return new ChallengeCreateResponse(true, new ChallengeCreateResponse.Data(
                saved.getId(),
                saved.getTitle(),
                saved.getDescription(),
                saved.getType().name(),
                saved.getDurationDays(),
                "in-progress",
                enrollment.getStartedAt(),
                enrollment.getEndedAt()
        ));
    }

    @Transactional(readOnly = true)
    public ChallengeListResponse getChallenges(String statusFilter) {
        User user = getCurrentUser();
        Map<Long, UserChallenge> userChallenges = userChallengeRepository.findByUser(user)
                .stream()
                .collect(Collectors.toMap(uc -> uc.getChallenge().getId(), uc -> uc));

        List<ChallengeListResponse.ChallengeSummary> summaries = challengeRepository.findAll()
                .stream()
                .map(challenge -> toChallengeSummary(challenge, userChallenges.get(challenge.getId())))
                .filter(summary -> statusFilter == null || statusFilter.isBlank() || summary.status().equalsIgnoreCase(statusFilter))
                .sorted(Comparator.comparing(ChallengeListResponse.ChallengeSummary::challengeId))
                .toList();

        return new ChallengeListResponse(true, new ChallengeListResponse.Data(summaries));
    }

    @Transactional
    public JoinChallengeResponse joinChallenge(Long challengeId) {
        User user = getCurrentUser();
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(ChallengeNotFoundException::new);

        UserChallenge userChallenge = userChallengeRepository.findByUserAndChallengeId(user, challengeId)
                .orElseGet(() -> createEnrollment(user, challenge));

        return new JoinChallengeResponse(true, new JoinChallengeResponse.Data(
                challenge.getId(),
                "on_going",
                userChallenge.getStartedAt(),
                userChallenge.getEndedAt()
        ));
    }

    @Transactional(readOnly = true)
    public ChallengeProgressResponse getProgress() {
        User user = getCurrentUser();
        List<UserChallenge> enrollments = userChallengeRepository.findByUser(user);

        List<ChallengeProgressResponse.InProgress> inProgress = enrollments.stream()
                .filter(uc -> "ongoing".equalsIgnoreCase(uc.getStatus()))
                .map(this::toInProgress)
                .toList();

        List<ChallengeProgressResponse.Completed> done = enrollments.stream()
                .filter(uc -> "completed".equalsIgnoreCase(uc.getStatus()))
                .map(this::toCompleted)
                .toList();

        return new ChallengeProgressResponse(true, new ChallengeProgressResponse.Data(inProgress, done));
    }

    private ChallengeListResponse.ChallengeSummary toChallengeSummary(Challenge challenge, UserChallenge enrollment) {
        if (enrollment == null) {
            return new ChallengeListResponse.ChallengeSummary(
                    challenge.getId(),
                    challenge.getTitle(),
                    challenge.getDescription(),
                    challenge.getType().name(),
                    challenge.getDurationDays(),
                    "available",
                    null,
                    null,
                    null
            );
        }

        boolean done = "completed".equalsIgnoreCase(enrollment.getStatus());
        String status = done ? "done" : "in-progress";
        Double progressValue = enrollment.getProgressRate() != null ? enrollment.getProgressRate() / 100.0 : null;

        return new ChallengeListResponse.ChallengeSummary(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription(),
                challenge.getType().name(),
                challenge.getDurationDays(),
                status,
                progressValue,
                enrollment.getStartedAt(),
                enrollment.getEndedAt()
        );
    }

    private UserChallenge createEnrollment(User user, Challenge challenge) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate startDate = now.toLocalDate();
        LocalDate endDate = startDate.plusDays(challenge.getDurationDays() != null ? challenge.getDurationDays() : 0);        UserChallenge enrollment = UserChallenge.builder()
                .user(user)
                .challenge(challenge)
                .status("ongoing")
                .progressRate(0f)
                .startedAt(startDate)
                .endedAt(endDate)
                .build();
        return userChallengeRepository.save(enrollment);
    }

    private ChallengeProgressResponse.InProgress toInProgress(UserChallenge enrollment) {
        LocalDate today = LocalDate.now();
        int remainingDays = 0;
        if (enrollment.getEndedAt() != null) {
            remainingDays = (int) Math.max(0, ChronoUnit.DAYS.between(today, enrollment.getEndedAt()));
        }
        return new ChallengeProgressResponse.InProgress(
                enrollment.getChallenge().getId(),
                enrollment.getChallenge().getTitle(),
                enrollment.getChallenge().getType().name(),
                enrollment.getProgressRate() != null ? enrollment.getProgressRate().intValue() : 0,
                0,
                remainingDays
        );
    }

    private ChallengeProgressResponse.Completed toCompleted(UserChallenge enrollment) {
        return new ChallengeProgressResponse.Completed(
                enrollment.getChallenge().getId(),
                enrollment.getChallenge().getTitle(),
                enrollment.getChallenge().getType().name(),
                enrollment.getFinishedAt()
        );
    }

    private String generateCustomCode(User user) {
        String userKey = user.getId() != null ? user.getId().toString() : "user";
        return "CUSTOM-" + userKey + "-" + System.currentTimeMillis();
    }

    private User getCurrentUser() {
        return userRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(UserNotFoundException::new);
    }
}