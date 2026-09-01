package models;

public enum AlertMessage {
    TRANSFER_SUCCESS("Transfer successful"),
    FORBIDDEN_WITH_TEXT("Unauthorized access to account"),
    BAD_REQUEST_WITH_TEXT("Invalid transfer: insufficient funds or invalid accounts"),
    PROFILE_UPDATED_SUCCESSFULLY("Profile updated successfully");

    private final String message;

    AlertMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
