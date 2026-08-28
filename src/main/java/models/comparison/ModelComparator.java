package models.comparison;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModelComparator {

    public static <A, B> ComparisonResult compareFields(
            A request,
            B response,
            Map<String, String> fieldMappings) {

        List<Mismatch> mismatches = new ArrayList<>();

        for (Map.Entry<String, String> entry : fieldMappings.entrySet()) {

            String requestField = entry.getKey();
            String responseField = entry.getValue();

            Object expected = getNestedFieldValue(request, requestField);
            Object actual = getNestedFieldValue(response, responseField);

            if (!areEqual(expected, actual)) {
                mismatches.add(
                        new Mismatch(
                                requestField + " -> " + responseField,
                                expected,
                                actual
                        )
                );
            }
        }

        return new ComparisonResult(mismatches);
    }

    private static boolean areEqual(Object expected, Object actual) {

        if (expected == null && actual == null) {
            return true;
        }

        if (expected == null || actual == null) {
            return false;
        }

        if (expected instanceof BigDecimal expectedDecimal
                && actual instanceof Number actualNumber) {

            return expectedDecimal.compareTo(
                    new BigDecimal(actualNumber.toString())
            ) == 0;
        }

        if (actual instanceof BigDecimal actualDecimal
                && expected instanceof Number expectedNumber) {

            return actualDecimal.compareTo(
                    new BigDecimal(expectedNumber.toString())
            ) == 0;
        }

        return Objects.equals(expected, actual);
    }

    private static Object getNestedFieldValue(
            Object object,
            String fieldPath) {

        if (object == null) {
            return null;
        }

        Object current = object;

        for (String fieldName : fieldPath.split("\\.")) {
            current = getFieldValue(current, fieldName);

            if (current == null) {
                return null;
            }
        }

        return current;
    }

    private static Object getFieldValue(
            Object object,
            String fieldName) {

        Class<?> clazz = object.getClass();

        while (clazz != null) {

            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);

                return field.get(object);

            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();

            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        "Cannot access field '"
                                + fieldName
                                + "' in class '"
                                + clazz.getSimpleName()
                                + "'",
                        e
                );
            }
        }

        throw new RuntimeException(
                "Field '"
                        + fieldName
                        + "' not found in class '"
                        + object.getClass().getSimpleName()
                        + "'"
        );
    }

    public static class ComparisonResult {

        private final List<Mismatch> mismatches;

        public ComparisonResult(List<Mismatch> mismatches) {
            this.mismatches = mismatches;
        }

        public boolean isSuccess() {
            return mismatches.isEmpty();
        }

        public List<Mismatch> getMismatches() {
            return mismatches;
        }

        @Override
        public String toString() {

            if (isSuccess()) {
                return "All fields match.";
            }

            StringBuilder sb = new StringBuilder("Mismatched fields:\n");

            for (Mismatch mismatch : mismatches) {
                sb.append("- ")
                        .append(mismatch.fieldName)
                        .append(": expected=")
                        .append(mismatch.expected)
                        .append(", actual=")
                        .append(mismatch.actual)
                        .append("\n");
            }

            return sb.toString();
        }
    }

    public static class Mismatch {

        public final String fieldName;
        public final Object expected;
        public final Object actual;

        public Mismatch(
                String fieldName,
                Object expected,
                Object actual) {

            this.fieldName = fieldName;
            this.expected = expected;
            this.actual = actual;
        }
    }
}