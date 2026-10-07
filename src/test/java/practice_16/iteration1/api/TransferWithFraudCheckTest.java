package practice_16.iteration1.api;

import api.models.*;
import api.models.comparison.ModelAssertions;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import api.requests.steps.DepositSteps;
import common.annotations.FraudCheckMock;
import common.data.FraudDecision;
import common.data.FraudTestData;
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
            riskScore = FraudTestData.LOW_RISK_SCORE,
            reason = FraudTestData.LOW_RISK_REASON,
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void testTransferWithFraudCheck() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                FraudTestData.TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();

        TransferMoneyResponse expectedResponse = TransferMoneyResponse.builder()
                .status(FraudDecision.APPROVED.getValue())
                .message(FraudTestData.APPROVED_MESSAGE)
                .amount(FraudTestData.TRANSFER_AMOUNT)
                .senderAccountId(Math.toIntExact(account1.getId()))
                .receiverAccountId(Math.toIntExact(account2.getId()))
                .fraudRiskScore(FraudTestData.LOW_RISK_SCORE)
                .fraudReason(FraudTestData.LOW_RISK_REASON)
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
            riskScore = FraudTestData.HIGH_RISK_SCORE,
            reason = FraudTestData.HIGH_RISK_REASON,
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void transferBlockedByFraudCheck() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                FraudTestData.TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Статус").isEqualTo(FraudDecision.BLOCKED.getValue());
        softly.assertThat(transferResponse.getMessage()).as("Сообщение").contains(FraudTestData.BLOCKED_MESSAGE_PART);
        softly.assertThat(transferResponse.getFraudRiskScore()).as("Risk score").isEqualTo(FraudTestData.HIGH_RISK_SCORE);
        softly.assertThat(transferResponse.getFraudReason()).as("Reason").isEqualTo(FraudTestData.HIGH_RISK_REASON);
        softly.assertThat(transferResponse.isRequiresManualReview()).as("Manual review").isFalse();
        softly.assertThat(accountSteps1().getTransactions(account1.getId())).as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals(TransactionType.TRANSFER_OUT.getValue()));
    }

    @Test
    @DisplayName("REVIEW_REQUIRED → ручная проверка")
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "REVIEW_REQUIRED",
            riskScore = FraudTestData.SUSPICIOUS_RISK_SCORE,
            reason = FraudTestData.SUSPICIOUS_REASON,
            requiresManualReview = true,
            additionalVerificationRequired = false
    )
    public void transferRequiresManualReview() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                FraudTestData.TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Статус").isEqualTo(FraudDecision.REVIEW_REQUIRED.getValue());
        softly.assertThat(transferResponse.isRequiresManualReview()).as("Manual review").isTrue();
        softly.assertThat(transferResponse.getFraudReason()).as("Reason").isEqualTo(FraudTestData.SUSPICIOUS_REASON);
        softly.assertThat(accountSteps1().getTransactions(account1.getId())).as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals(TransactionType.TRANSFER_OUT.getValue()));
    }

    @Test
    @DisplayName("VERIFICATION_REQUIRED → доп. верификация")
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "VERIFICATION_REQUIRED",
            riskScore = FraudTestData.VERIFICATION_RISK_SCORE,
            reason = FraudTestData.VERIFICATION_REASON,
            requiresManualReview = false,
            additionalVerificationRequired = true
    )
    public void transferRequiresVerification() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                FraudTestData.TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Статус").isEqualTo(FraudDecision.VERIFICATION_REQUIRED.getValue());
        softly.assertThat(transferResponse.isRequiresVerification()).as("Verification").isTrue();
        softly.assertThat(transferResponse.getFraudReason()).as("Reason").isEqualTo(FraudTestData.VERIFICATION_REASON);
        softly.assertThat(accountSteps1().getTransactions(account1.getId())).as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals(TransactionType.TRANSFER_OUT.getValue()));
    }

    @Test
    @DisplayName("Timeout сервиса → REVIEW_REQUIRED")
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = FraudTestData.LOW_RISK_SCORE,
            reason = FraudTestData.LOW_RISK_REASON,
            delayMs = FraudTestData.TIMEOUT_DELAY_MS,
            port = FraudTestData.PORT_TIMEOUT
    )
    public void transferFallbackOnTimeout() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                FraudTestData.TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Fallback в REVIEW_REQUIRED при timeout").isEqualTo(FraudDecision.REVIEW_REQUIRED.getValue());
        softly.assertThat(transferResponse.isRequiresManualReview()).as("Manual review").isTrue();
        softly.assertThat(accountSteps1().getTransactions(account1.getId())).as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals(TransactionType.TRANSFER_OUT.getValue()));
    }

    @Test
    @DisplayName("500 от fraud-сервиса → REVIEW_REQUIRED")
    @FraudCheckMock(
            status = "ERROR",
            decision = "UNKNOWN",
            httpStatus = 500,
            port = FraudTestData.PORT_SERVER_ERROR
    )
    public void transferFallbackOnServerError() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                FraudTestData.TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Fallback в REVIEW_REQUIRED при 500").isEqualTo(FraudDecision.REVIEW_REQUIRED.getValue());
        softly.assertThat(transferResponse.isRequiresManualReview()).as("Manual review").isTrue();
        softly.assertThat(accountSteps1().getTransactions(account1.getId())).as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals(TransactionType.TRANSFER_OUT.getValue()));
    }

    @Test
    @DisplayName("Connection error → REVIEW_REQUIRED")
    @FraudCheckMock(
            connectionError = true,
            port = FraudTestData.PORT_CONNECTION_ERROR
    )
    public void transferFallbackOnConnectionError() {

        prepareUsersAndAccounts();

        transferResponse = accountSteps1().transferWithFraudCheck(
                account1.getId(),
                account2.getId(),
                FraudTestData.TRANSFER_AMOUNT
        );

        softly.assertThat(transferResponse).as("Ответ не должен быть null").isNotNull();
        softly.assertThat(transferResponse.getStatus()).as("Fallback в REVIEW_REQUIRED при connection error").isEqualTo(FraudDecision.REVIEW_REQUIRED.getValue());
        softly.assertThat(transferResponse.isRequiresManualReview()).as("Manual review").isTrue();
        softly.assertThat(accountSteps1().getTransactions(account1.getId())).as("У отправителя не должно быть TRANSFER_OUT")
                .noneMatch(t -> t.getType().equals(TransactionType.TRANSFER_OUT.getValue()));
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