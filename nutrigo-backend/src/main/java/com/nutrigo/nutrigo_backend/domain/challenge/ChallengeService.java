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
import com.nutrigo.nutrigo_backend.global.common.enums.ChallengeType;
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
        
        // 챌린지 조건을 만족하는 날짜만 필터링
        List<DailyIntakeSummary> validSummaries = filterValidDays(enrollment.getChallenge(), summaries);
        
        int progressRate = calculateProgressRate(enrollment, validSummaries, today);
        
        String status = enrollment.getStatus();
        if (status == null || status.isBlank()) {
            status = "inactive";
        }

        Integer totalDays = enrollment.getChallenge().getDurationDays();
        int completedDays = validSummaries.size();
        
        // 남은 일수 = 총 일수 - 완료 일수
        int remainingDays = Math.max(0, totalDays - completedDays);

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
                validSummaries.stream().map(this::toDailyIntake).toList()
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
        List<DailyIntakeSummary> summaries = loadDailyIntakes(enrollment, today);
        
        // 챌린지 조건을 만족하는 날짜만 필터링
        List<DailyIntakeSummary> validSummaries = filterValidDays(enrollment.getChallenge(), summaries);
        
        int progressRate = calculateProgressRate(enrollment, validSummaries, today);
        
        Integer totalDays = enrollment.getChallenge().getDurationDays();
        int completedDays = validSummaries.size();
        
        // 남은 일수 = 총 일수 - 완료 일수
        int remainingDays = Math.max(0, totalDays != null ? totalDays - completedDays : 0);
        
        return new ChallengeProgressResponse.InProgress(
                enrollment.getChallenge().getId(),
                enrollment.getChallenge().getTitle(),
                enrollment.getChallenge().getType().name(),
                progressRate,
                remainingDays,
                validSummaries.stream()
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

    /**
     * 챌린지 조건을 만족하는 날짜만 필터링
     */
    private List<DailyIntakeSummary> filterValidDays(Challenge challenge, List<DailyIntakeSummary> summaries) {
        ChallengeType type = challenge.getType();
        String title = challenge.getTitle();
        
        return summaries.stream()
                .filter(summary -> {
                    if (type == ChallengeType.kcal) {
                        // 칼로리 챌린지: 제목에서 목표 칼로리 추출 (예: "7일 동안 1800kcal 이하 식단")
                        Integer targetKcal = extractKcalFromTitle(title);
                        if (targetKcal != null && summary.getTotalKcal() != null) {
                            // Float를 Integer와 비교
                            return summary.getTotalKcal().intValue() <= targetKcal;
                        }
                        // 목표 칼로리를 추출할 수 없거나 칼로리 데이터가 없으면 제외
                        return false;
                    } else if (type == ChallengeType.sodium) {
                        // 나트륨 챌린지: 제목에서 목표 나트륨 추출 (예: "7일 동안 나트륨 2000mg 이하")
                        Integer targetSodium = extractSodiumFromTitle(title);
                        if (targetSodium != null && summary.getTotalSodiumMg() != null) {
                            return summary.getTotalSodiumMg().intValue() <= targetSodium;
                        }
                        // 목표 나트륨을 추출할 수 없거나 나트륨 데이터가 없으면 제외
                        return false;
                    } else if (type == ChallengeType.protein) {
                        // 단백질 챌린지: 제목에서 목표 단백질 추출 (예: "일주일 동안 단백질 60g 이상 섭취")
                        Integer targetProtein = extractProteinFromTitle(title);
                        if (targetProtein != null && summary.getTotalProteinG() != null) {
                            return summary.getTotalProteinG().intValue() >= targetProtein;
                        }
                        // 목표 단백질을 추출할 수 없거나 단백질 데이터가 없으면 제외
                        return false;
                    } else if (type == ChallengeType.frequency) {
                        // 빈도 챌린지: 기록이 있으면 성공
                        return true;
                    } else if (type == ChallengeType.day_color) {
                        // 색깔 챌린지: 기록이 있으면 성공
                        return true;
                    }
                    // 알 수 없는 타입이면 제외
                    return false;
                })
                .toList();
    }

    /**
     * 제목에서 칼로리 목표 추출 (예: "7일 동안 1800kcal 이하 식단" -> 1800)
     */
    private Integer extractKcalFromTitle(String title) {
        if (title == null || title.isBlank()) return null;
        // "1800kcal", "2000kcal" 등의 패턴 찾기 (대소문자 구분 없이)
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)\\s*kcal", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 제목에서 나트륨 목표 추출 (예: "7일 동안 나트륨 2000mg 이하" -> 2000)
     */
    private Integer extractSodiumFromTitle(String title) {
        if (title == null) return null;
        // "나트륨 2000mg", "2000mg" 등의 패턴 찾기
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("나트륨\\s*(\\d+)mg|(\\d+)mg");
        java.util.regex.Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            try {
                String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 제목에서 단백질 목표 추출 (예: "일주일 동안 단백질 60g 이상 섭취" -> 60)
     */
    private Integer extractProteinFromTitle(String title) {
        if (title == null) return null;
        // "단백질 60g", "60g" 등의 패턴 찾기
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("단백질\\s*(\\d+)g|(\\d+)g");
        java.util.regex.Matcher matcher = pattern.matcher(title);
        if (matcher.find()) {
            try {
                String value = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
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