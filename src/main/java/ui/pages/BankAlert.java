package ui.pages;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Locale;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    DEPOSIT_SUCCESSFULY("✅ Successfully deposited $%.2f to account %s!"),
    DEPOSIT_INVALID("❌ Please enter a valid amount."),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number:"),
    TRANSFER_SUCCESSFULY("✅ Successfully transferred $%.2f to account %s!"),
    TRANSFER_INVALID_MIN("Error: Transfer amount must be at least 0.01"),
    TRANSFER_EXCEEDS_LIMIT("Error: Transfer amount cannot exceed 10000"),
    INVALID_AMOUNT("❌ Please enter a valid amount."),
    AMOUNT_EXCEEDS_LIMIT("❌ Please deposit less or equal to 5000$."),
    FILL_ALL_FIELDS("❌ Please fill all fields and confirm"),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }


    public String format(Object... args) {
        return String.format(Locale.US, message, args);
    }
}
