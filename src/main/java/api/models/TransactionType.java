package api.models;

public enum TransactionType {
    DEPOSIT("DEPOSIT"),
    TRANSFER_OUT("TRANSFER_OUT"),
    TRANSFER_OUT2("Нет транзакции списания (TRANSFER_OUT)"),
    TRANSFER_IN("TRANSFER_IN");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}