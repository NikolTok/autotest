package models;

public enum AlertMessage {
    TRANSFER_SUCCESS("Transfer successful"),
    PROFILE_UPDATED_SUCCESSFULLY("Profile updated successfully");

    private final String message;

    AlertMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
