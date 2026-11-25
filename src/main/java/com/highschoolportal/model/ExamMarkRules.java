package com.highschoolportal.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/** Subject marks are totals across the configured examination series. */
public final class ExamMarkRules {
    private static final BigDecimal MAX_DATABASE_MARK = new BigDecimal("999999.99");
    private ExamMarkRules() {}

    public record Result(BigDecimal total, BigDecimal finalMark, String status) {}

    public static BigDecimal validateMark(BigDecimal mark) {
        if (mark == null || mark.signum() < 0 || mark.compareTo(MAX_DATABASE_MARK) > 0)
            throw new IllegalArgumentException("Enter a non-negative mark within the supported range.");
        try { return mark.setScale(2, RoundingMode.UNNECESSARY); }
        catch (ArithmeticException ex) { throw new IllegalArgumentException("Marks can have at most two decimal places.", ex); }
    }

    public static boolean validConfiguration(int series, BigDecimal pass, BigDecimal max) {
        return series > 0 && pass != null && max != null && pass.signum() > 0
                && pass.compareTo(max) <= 0 && max.compareTo(MAX_DATABASE_MARK) <= 0;
    }

    public static Result calculate(Map<Integer, BigDecimal> marks, int series, BigDecimal pass, BigDecimal max) {
        if (!validConfiguration(series, pass, max))
            throw new IllegalArgumentException("The subject examination rules have not been configured correctly.");
        BigDecimal total = BigDecimal.ZERO.setScale(2);
        for (var entry : marks.entrySet()) {
            if (entry.getKey() == null || entry.getKey() < 1 || entry.getKey() > series)
                throw new IllegalArgumentException("An exam series is outside the configured range.");
            total = total.add(validateMark(entry.getValue()));
        }
        if (total.compareTo(max) > 0)
            throw new IllegalArgumentException("The combined marks cannot exceed the subject maximum of " + max.toPlainString() + ".");
        if (marks.size() != series) return new Result(total, null, "Incomplete");
        return new Result(total, total, total.compareTo(pass) >= 0 ? "Passed" : "Failed");
    }
}
