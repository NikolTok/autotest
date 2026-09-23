package practice_16.iteration1.api;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.DepositMoneyResponse;
import api.models.TransferMoneyResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import api.requests.steps.DepositSteps;
import common.annotations.FraudCheckMock;
import common.extensions.TimingExtension;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import practice_16.iteration2.BaseTest;

import java.math.BigDecimal;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
public class TransferWithFraudCheckTest extends BaseTest {

    private CreateUserRequest user1;
    private CreateUserRequest user2;
    private CreateAccountResponse account1;
    private CreateAccountResponse account2;
    private DepositMoneyResponse depositResponse;
    private TransferMoneyResponse transferResponse;

    private static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("5000.00");
    private static final BigDecimal TRANSFER_AMOUNT = new BigDecimal("100.00");

    @BeforeEach
    public void setupTest() {
        this.softly = new SoftAssertions();
    }

    @AfterEach
    public void afterTest() {
        softly.assertAll();
    }

    @Test
    @DisplayName("APPROVED → перевод обрабатывается сразу")
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void testTransferWithFraudCheck() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();

        TransferMoneyResponse expectedResponse = TransferMoneyResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(TRANSFER_AMOUNT)
                .senderAccountId(Math.toIntExact(account1.getId()))
                .receiverAccountId(Math.toIntExact(account2.getId()))
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @DisplayName("BLOCKED → перевод блокируется")
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "BLOCKED",
            riskScore = 0.95,
            reason = "High risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void transferBlockedByFraudCheck() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Статус").isEqualTo("BLOCKED");
        softly.assertThat(transferResponse.getMessage()).as("Сообщение").contains("blocked");
        softly.assertThat(transferResponse.getFraudRiskScore()).as("Risk score").isEqualTo(0.95);
        softly.assertThat(transferResponse.getFraudReason()).as("Reason").isEqualTo("High risk transaction");
        softly.assertThat(transferResponse.isRequiresManualReview()).as("Manual review").isFalse();
        softly.assertThat(accountSteps1().getTransactions(account1.getId()))
                .as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals("TRANSFER_OUT"));
    }

    @Test
    @DisplayName("REVIEW_REQUIRED → ручная проверка")
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "REVIEW_REQUIRED",
            riskScore = 0.6,
            reason = "Suspicious activity",
            requiresManualReview = true,
            additionalVerificationRequired = false
    )
    public void transferRequiresManualReview() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Статус").isEqualTo("REVIEW_REQUIRED");
        softly.assertThat(transferResponse.isRequiresManualReview()).as("Manual review").isTrue();
        softly.assertThat(transferResponse.getFraudReason()).as("Reason").isEqualTo("Suspicious activity");
        softly.assertThat(accountSteps1().getTransactions(account1.getId()))
                .as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals("TRANSFER_OUT"));
    }

    @Test
    @DisplayName("VERIFICATION_REQUIRED → доп. верификация")
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "VERIFICATION_REQUIRED",
            riskScore = 0.75,
            reason = "Additional verification needed",
            requiresManualReview = false,
            additionalVerificationRequired = true
    )
    public void transferRequiresVerification() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Статус").isEqualTo("VERIFICATION_REQUIRED");
        softly.assertThat(transferResponse.isRequiresVerification()).as("Verification").isTrue();
        softly.assertThat(transferResponse.getFraudReason()).as("Reason").isEqualTo("Additional verification needed");
        softly.assertThat(accountSteps1().getTransactions(account1.getId()))
                .as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals("TRANSFER_OUT"));
    }

    @Test
    @DisplayName("Timeout сервиса → REVIEW_REQUIRED")
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            delayMs = 10_000,   // ← 10 секунд задержки
            port = 8081
    )
    public void transferFallbackOnTimeout() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Fallback в REVIEW_REQUIRED при timeout").isEqualTo("REVIEW_REQUIRED");
        softly.assertThat(transferResponse.isRequiresManualReview()).as("Manual review").isTrue();
        softly.assertThat(accountSteps1().getTransactions(account1.getId()))
                .as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals("TRANSFER_OUT"));
    }

    @Test
    @DisplayName("500 от fraud-сервиса → REVIEW_REQUIRED")
    @FraudCheckMock(
            status = "ERROR",
            decision = "UNKNOWN",
            httpStatus = 500,
            port = 8082
    )
    public void transferFallbackOnServerError() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Fallback в REVIEW_REQUIRED при 500").isEqualTo("REVIEW_REQUIRED");
        softly.assertThat(transferResponse.isRequiresManualReview()).as("Manual review").isTrue();
        softly.assertThat(accountSteps1().getTransactions(account1.getId()))
                .as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals("TRANSFER_OUT"));
    }

    @Test
    @DisplayName("Connection error → REVIEW_REQUIRED")
    @FraudCheckMock(
            connectionError = true,
            port = 8083
    )
    public void transferFallbackOnConnectionError() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Fallback в REVIEW_REQUIRED при connection error").isEqualTo("REVIEW_REQUIRED");
        softly.assertThat(transferResponse.isRequiresManualReview()).as("Manual review").isTrue();
        softly.assertThat(accountSteps1().getTransactions(account1.getId()))
                .as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals("TRANSFER_OUT"));
    }

    private void prepareUsersAndAccounts() {
        user1 = AdminSteps.createUser();
        account1 = accountSteps1().createAccount();
        depositResponse = DepositSteps.depositMoney(user1, account1.getId(), DEPOSIT_AMOUNT);

        user2 = AdminSteps.createUser();
        account2 = accountSteps2().createAccount();
    }

    private AccountSteps accountSteps1() {
        return new AccountSteps(user1.getUsername(), user1.getPassword());
    }

    private AccountSteps accountSteps2() {
        return new AccountSteps(user2.getUsername(), user2.getPassword());
    }
}