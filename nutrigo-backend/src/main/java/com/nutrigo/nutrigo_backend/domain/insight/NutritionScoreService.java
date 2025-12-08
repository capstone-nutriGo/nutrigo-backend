package com.nutrigo.nutrigo_backend.domain.insight;

import com.nutrigo.nutrigo_backend.domain.insight.dto.NutrientProfile;
import com.nutrigo.nutrigo_backend.domain.user.User;
import com.nutrigo.nutrigo_backend.global.common.enums.Gender;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class NutritionScoreService {

    private static final float MAX_SCORE = 100f;
    private static final float WARNING_THRESHOLD = 60f;
    private static final float GOOD_THRESHOLD = 80f;

    private final Map<Gender, List<ReferenceIntake>> genderReferences = new EnumMap<>(Gender.class);

    public NutritionScoreService() {
        genderReferences.put(Gender.male, List.of(
                new ReferenceIntake(19, 29, 2600, 65, 360, 2300),
                new ReferenceIntake(30, 49, 2500, 65, 350, 2300),
                new ReferenceIntake(50, 64, 2300, 60, 320, 2000),
                new ReferenceIntake(65, 120, 2100, 60, 300, 2000)
        ));
        genderReferences.put(Gender.female, List.of(
                new ReferenceIntake(19, 29, 2100, 55, 300, 2000),
                new ReferenceIntake(30, 49, 2000, 55, 290, 2000),
                new ReferenceIntake(50, 64, 1900, 50, 270, 1900),
                new ReferenceIntake(65, 120, 1800, 50, 250, 1900)
        ));
    }

    public Float calculateMealScore(User user, NutrientProfile nutrientProfile) {
        ReferenceIntake intake = resolveReference(user);
        if (intake == null || nutrientProfile == null) {
            return null;
        }

        float mealScoreSum = 0f;
        int metrics = 0;

        if (nutrientProfile.kcal() != null) {
            mealScoreSum += scoreAgainstTarget(nutrientProfile.kcal(), intake.dailyKcal() / 3f);
            metrics++;
        }
        if (nutrientProfile.proteinG() != null) {
            mealScoreSum += scoreAgainstTarget(nutrientProfile.proteinG(), intake.dailyProtein() / 3f);
            metrics++;
        }
        if (nutrientProfile.carbG() != null) {
            mealScoreSum += scoreAgainstTarget(nutrientProfile.carbG(), intake.dailyCarb() / 3f);
            metrics++;
        }
        if (nutrientProfile.sodiumMg() != null) {
            mealScoreSum += scoreAgainstTarget(nutrientProfile.sodiumMg(), intake.dailySodium() / 3f);
            metrics++;
        }

        if (metrics == 0) {
            return null;
        }

        return mealScoreSum / metrics;
    }

    public String resolveDayColor(Float dayScore) {
        if (dayScore == null) {
            return null;
        }
        if (dayScore >= GOOD_THRESHOLD) {
            return "green";
        }
        if (dayScore >= WARNING_THRESHOLD) {
            return "yellow";
        }
        return "red";
    }

    private float scoreAgainstTarget(float actual, float target) {
        if (target <= 0) {
            return 0f;
        }
        float ratio = actual / target;
        float deviation = Math.abs(1f - ratio);
        float score = MAX_SCORE - deviation * MAX_SCORE;
        return Math.max(0f, Math.min(MAX_SCORE, score));
    }

    private ReferenceIntake resolveReference(User user) {
        Gender gender = user.getGender();
        LocalDate birthday = user.getBirthday();
        if (gender == null || birthday == null) {
            return null;
        }

        int age = Period.between(birthday, LocalDate.now()).getYears();
        List<ReferenceIntake> references = genderReferences.get(gender);
        if (references == null || references.isEmpty()) {
            return null;
        }

        Optional<ReferenceIntake> match = references.stream()
                .filter(ref -> age >= ref.minAge() && age <= ref.maxAge())
                .min(Comparator.comparingInt(ReferenceIntake::minAge));

        return match.orElse(references.get(references.size() - 1));
    }

    private record ReferenceIntake(int minAge, int maxAge, float dailyKcal, float dailyProtein, float dailyCarb, float dailySodium) {
    }
}