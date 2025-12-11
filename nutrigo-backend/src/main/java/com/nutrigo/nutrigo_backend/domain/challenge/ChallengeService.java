package com.nutrigo.nutrigo_backend.domain.challenge;

import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeCreateRequest;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeCreateResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeListResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeProgressResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeQuitResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.JoinChallengeResponse;
import com.nutrigo.nutrigo_backend.domain.challenge.dto.ChallengeProgressDetailResponse;
import com.nutrigo.nutrigo_backend.domain.insight.DailyIntakeSummary;
import com.nutrigo.nutrigo_backend.domain.insight.DailyIntakeSummaryRepository;
import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.global.error.AppExceptions.Challenge.ChallengeNotFoundException;
import com.nutrigo.nutrigo_backend.global.security.AuthenticatedUserProvider;
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
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final DailyIntakeSummaryRepository dailyIntakeSummaryRepository;

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
                enrollment.getStatus(),
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
                userChallenge.getStatus(),
                userChallenge.getStartedAt(),
                userChallenge.getEndedAt()
        ));
    }

    @Transactional
    public ChallengeQuitResponse quitChallenge(Long challengeId) {
        User user = getCurrentUser();
        UserChallenge enrollment = userChallengeRepository.findByUserAndChallengeId(user, challengeId)
                .orElseThrow(ChallengeNotFoundException::new);

        if ("ongoing".equalsIgnoreCase(enrollment.getStatus())) {
            LocalDate today = LocalDate.now();
            List<DailyIntakeSummary> summaries = loadDailyIntakes(enrollment, today);
            enrollment.setProgressRate((float) calculateProgressRate(enrollment, summaries, today));
            enrollment.setStatus("failed");
            enrollment.setFinishedAt(LocalDateTime.now());
            userChallengeRepository.save(enrollment);
        }

        return new ChallengeQuitResponse(true, new ChallengeQuitResponse.Data(
                enrollment.getChallenge().getId(),
                enrollment.getStatus(),
                enrollment.getFinishedAt()
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

    @Transactional(readOnly = true)
    public ChallengeProgressDetailResponse getChallengeProgress(Long challengeId) {
        User user = getCurrentUser();
        UserChallenge enrollment = userChallengeRepository.findByUserAndChallengeId(user, challengeId)
                .orElseThrow(ChallengeNotFoundException::new);

        LocalDate today = LocalDate.now();
        List<DailyIntakeSummary> summaries = loadDailyIntakes(enrollment, today);
        int progressRate = calculateProgressRate(enrollment, summaries, today);
        int remainingDays = 0;
        if ("ongoing".equalsIgnoreCase(enrollment.getStatus()) && enrollment.getEndedAt() != null) {
            remainingDays = (int) Math.max(0, ChronoUnit.DAYS.between(today, enrollment.getEndedAt()));
        }

        String status = enrollment.getStatus();
        if (status == null || status.isBlank()) {
            status = "inactive";
        }

        Integer totalDays = enrollment.getChallenge().getDurationDays();
        int completedDays = summaries.size();

        return new ChallengeProgressDetailResponse(true, new ChallengeProgressDetailResponse.Data(
                enrollment.getChallenge().getId(),
                enrollment.getChallenge().getTitle(),
                enrollment.getChallenge().getDescription(),
                enrollment.getChallenge().getType().name(),
                status.toLowerCase(),
                progressRate,
                remainingDays,
                totalDays,
                completedDays,
                enrollment.getStartedAt(),
                enrollment.getEndedAt(),
                enrollment.getFinishedAt(),
                summaries.stream().map(this::toDailyIntake).toList()
        ));
    }

    private ChallengeListResponse.ChallengeSummary toChallengeSummary(Challenge challenge, UserChallenge enrollment) {
        if (enrollment == null) {
            return new ChallengeListResponse.ChallengeSummary(
                    challenge.getId(),
                    challenge.getTitle(),
                    challenge.getDescription(),
                    challenge.getType().name(),
                    challenge.getDurationDays(),
                    normalizeChallengeStatus(challenge.getStatus()),
                    null,
                    null,
                    null
            );
        }

        boolean failed = "failed".equalsIgnoreCase(enrollment.getStatus());
        boolean done = "completed".equalsIgnoreCase(enrollment.getStatus());
        String status = failed ? "failed" : (done ? "completed" : "ongoing");
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
        LocalDate endDate = startDate.plusDays(challenge.getDurationDays() != null ? challenge.getDurationDays() : 0);
        UserChallenge enrollment = UserChallenge.builder()
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
        List<DailyIntakeSummary> summaries = loadDailyIntakes(enrollment, today);
        int progressRate = calculateProgressRate(enrollment, summaries, today);
        return new ChallengeProgressResponse.InProgress(
                enrollment.getChallenge().getId(),
                enrollment.getChallenge().getTitle(),
                enrollment.getChallenge().getType().name(),
                progressRate,
                remainingDays,
                summaries.stream()
                        .map(this::toDailyIntake)
                        .toList()
        );
    }

    private List<DailyIntakeSummary> loadDailyIntakes(UserChallenge enrollment, LocalDate today) {
        LocalDate startDate = enrollment.getStartedAt() != null ? enrollment.getStartedAt() : today;
        LocalDate endDate = enrollment.getEndedAt() != null ? enrollment.getEndedAt() : today;
        LocalDate upperBound = endDate.isBefore(today) ? endDate : today;
        return dailyIntakeSummaryRepository.findAllByUserAndDateBetween(
                enrollment.getUser(),
                startDate,
                upperBound
        );
    }

    private int calculateProgressRate(UserChallenge enrollment, List<DailyIntakeSummary> summaries, LocalDate today) {
        Integer durationDays = enrollment.getChallenge().getDurationDays();
        if (durationDays == null || durationDays <= 0) {
            return enrollment.getProgressRate() != null ? enrollment.getProgressRate().intValue() : 0;
        }
        LocalDate startDate = enrollment.getStartedAt() != null ? enrollment.getStartedAt() : today;
        long elapsedDays = ChronoUnit.DAYS.between(startDate, today) + 1;
        long targetDays = Math.min(elapsedDays, durationDays);
        if (targetDays <= 0) {
            return 0;
        }
        float completionRatio = (float) Math.min(summaries.size(), targetDays) / (float) durationDays;
        return Math.round(completionRatio * 100);
    }

    private ChallengeProgressResponse.DailyIntake toDailyIntake(DailyIntakeSummary summary) {
        return new ChallengeProgressResponse.DailyIntake(
                summary.getDate(),
                summary.getTotalKcal(),
                summary.getTotalSodiumMg(),
                summary.getTotalProteinG(),
                summary.getTotalCarbG(),
                summary.getTotalSnack(),
                summary.getTotalNight(),
                summary.getDayScore(),
                summary.getDayColor()
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

    private String normalizeChallengeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "inactive";
        }
        if ("ACTIVE".equalsIgnoreCase(status)) {
            return "active";
        }
        if ("INACTIVE".equalsIgnoreCase(status)) {
            return "inactive";
        }
        return status.toLowerCase();
    }

    private User getCurrentUser() {
        return authenticatedUserProvider.getCurrentUser();
    }
}