package com.nutrigo.nutrigo_backend.global.error;

import org.springframework.http.HttpStatus;

public final class AppExceptions {

    private AppExceptions() {
    }

    public static final class Auth {
        private Auth() {
        }

        public static class UserNotFoundException extends AppException {
            public UserNotFoundException() {
                super("AUTH_001", "User not found", HttpStatus.NOT_FOUND);
            }
        }

        public static class InvalidCredentialsException extends AppException {
            public InvalidCredentialsException() {
                super("AUTH_002", "Invalid credentials", HttpStatus.UNAUTHORIZED);
            }
        }

        public static class DuplicateEmailException extends AppException {
            public DuplicateEmailException() {
                super("AUTH_003", "Email already registered", HttpStatus.BAD_REQUEST);
            }
        }
    }

    public static final class User {
        private User() {
        }

        public static class UserNotFoundException extends AppException {
            public UserNotFoundException() {
                super("USER_001", "User not found", HttpStatus.NOT_FOUND);
            }
        }
    }

    public static final class Challenge {
        private Challenge() {
        }

        public static class ChallengeNotFoundException extends AppException {
            public ChallengeNotFoundException() {
                super("CHALLENGE_001", "Challenge not found", HttpStatus.NOT_FOUND);
            }
        }
    }

    public static final class Insight {
        private Insight() {
        }

        public static class AnalysisSessionNotFoundException extends AppException {
            public AnalysisSessionNotFoundException(Long analysisSessionId) {
                super("INSIGHT_001", "Analysis session not found: " + analysisSessionId, HttpStatus.NOT_FOUND);
            }
        }

        public static class InvalidReportRangeException extends AppException {
            public InvalidReportRangeException(String range) {
                super("INSIGHT_002", "Invalid report range: " + range, HttpStatus.BAD_REQUEST);
            }
        }
    }
}