package common.data;

public enum FraudStatus {
    SUCCESS("SUCCESS"),
    ERROR("ERROR");

    private final String value;

    FraudStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}