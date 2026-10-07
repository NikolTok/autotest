package common.data;

public enum FraudDecision {
    APPROVED("APPROVED"),
    BLOCKED("BLOCKED"),
    REVIEW_REQUIRED("REVIEW_REQUIRED"),
    VERIFICATION_REQUIRED("VERIFICATION_REQUIRED"),
    UNKNOWN("UNKNOWN");

    private final String value;

    FraudDecision(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}