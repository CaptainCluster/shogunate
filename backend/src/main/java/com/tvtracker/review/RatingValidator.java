package com.tvtracker.review;

import com.tvtracker.common.exception.ValidationException;
import java.math.BigDecimal;

public final class RatingValidator {

    private static final BigDecimal MIN = new BigDecimal("1.0");
    private static final BigDecimal MAX = new BigDecimal("5.0");
    private static final BigDecimal STEP = new BigDecimal("0.5");

    private RatingValidator() {}

    public static void validate(BigDecimal rating) {
        if (rating == null) {
            throw new ValidationException("Rating is required");
        }

        if (rating.compareTo(MIN) < 0 || rating.compareTo(MAX) > 0) {
            throw new ValidationException("Rating must be between 1.0 and 5.0");
        }

        if (rating.remainder(STEP).compareTo(BigDecimal.ZERO) != 0) {
            throw new ValidationException("Rating must be in 0.5 increments");
        }
    }
}
