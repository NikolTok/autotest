package practice_16.iteration2.negative_test.transfer_test;

import generators.RandomData;
import models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import requests.TransferMoneyRequester;
import requests.steps.AccountSteps;
import requests.steps.AdminSteps;
import requests.steps.DepositSteps;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static requests.steps.AccountSteps.getAccount;
import static requests.steps.AccountSteps.getTransactions;

public class TransferMoney extends BaseTest {

    public static Stream<Arguments> transferNotCorrectDate() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.00"), "Transfer amount must be at least 0.01", new BigDecimal("5000.00")),
                Arguments.of(new BigDecimal("-0.01"), "Transfer amount must be at least 0.01", new BigDecimal("5000.00")),
                Arguments.of(new BigDecimal("10000.01"), "Transfer amount cannot exceed 10000", new BigDecimal("5000.00")));
    }

    @MethodSource("transferNotCorrectDate")
    @ParameterizedTest
    public void userCanTransferWithNotCorrectDate(BigDecimal amount, String expectedMessage, BigDecimal maxDepositAmount) {

        CreateUserRequest user = AdminSteps.createUser();

        CreateAccountResponse senderAccount = AccountSteps.createAccount(user);
        CreateAccountResponse receiverAccount = AccountSteps.createAccount(user);

        DepositSteps.depositMoney(user, senderAccount.getId(), maxDepositAmount);
        DepositSteps.depositMoney(user, senderAccount.getId(), maxDepositAmount);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccount.getId()))
                .receiverAccountId(Math.toIntExact(receiverAccount.getId()))
                .amount(amount)
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(expectedMessage))
                .post(transferRequest);

        List<TransactionResponse> senderTransactions = getTransactions(user, senderAccount.getId());

        List<TransactionResponse> receiverTransactions = getTransactions(user, receiverAccount.getId());

        softly.assertThat(senderTransactions)
                .noneMatch(transaction ->
                        transaction.getType().equals("TRANSFER")
                                && transaction.getRelatedAccountId()
                                == receiverAccount.getId());

        softly.assertThat(receiverTransactions)
                .noneMatch(transaction ->
                        transaction.getType().equals("TRANSFER")
                                && transaction.getRelatedAccountId()
                                == senderAccount.getId());
    }


    @Test
    public void userCanTransferWithNotCorrectReceiverAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccount = AccountSteps.createAccount(user);

        BigDecimal depositAmount = new BigDecimal("5000.00");
        DepositSteps.depositMoney(user, senderAccount.getId(), depositAmount);

        List<TransactionResponse> senderTransactionsBefore = AccountSteps.getTransactions(user, senderAccount.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccount.getId()))
                .receiverAccountId(99999)
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(AlertMessage.BAD_REQUEST_WITH_TEXT.getMessage()))
                .post(transferRequest);

        List<TransactionResponse> senderTransactionsAfter = AccountSteps.getTransactions(user, senderAccount.getId());

        softly.assertThat(senderTransactionsAfter).as("Sender transactions count should not change").hasSize(senderTransactionsBefore.size());
        softly.assertThat(senderTransactionsAfter).as("No TRANSFER transactions should be created").noneMatch(transaction ->
                        transaction.getType().equals("TRANSFER"));
        softly.assertThat(senderTransactionsAfter).as("Only DEPOSIT transactions should exist")
                .allMatch(transaction -> transaction.getType().equals("DEPOSIT"));
    }

    @Test
    public void userCanTransferWithNotCorrectSenderAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse receiverAccount = AccountSteps.createAccount(user);

        List<TransactionResponse> receiverTransactionsBefore = AccountSteps.getTransactions(user, receiverAccount.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(99999)
                .receiverAccountId(Math.toIntExact(receiverAccount.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsForbiddenWithText(AlertMessage.FORBIDDEN_WITH_TEXT.getMessage()))
                .post(transferRequest);

        List<TransactionResponse> receiverTransactionsAfter = AccountSteps.getTransactions(user, receiverAccount.getId());

        softly.assertThat(receiverTransactionsAfter).as("Receiver transactions count should not change")
                .hasSize(receiverTransactionsBefore.size());

        softly.assertThat(receiverTransactionsAfter).as("No TRANSFER transactions should be created")
                .noneMatch(tx -> tx.getType().equals("TRANSFER"));

        softly.assertThat(receiverTransactionsAfter).as("Only DEPOSIT transactions should exist")
                .allMatch(tx -> tx.getType().equals("DEPOSIT"));
    }

    @Test
    public void userCanTransferWithNotSenderAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse receiverAccount = AccountSteps.createAccount(user);

        List<TransactionResponse> receiverTransactionsBefore = AccountSteps.getTransactions(user, receiverAccount.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .receiverAccountId(Math.toIntExact(receiverAccount.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsForbiddenWithText(AlertMessage.FORBIDDEN_WITH_TEXT.getMessage()))
                .post(transferRequest);

        List<TransactionResponse> receiverTransactionsAfter =
                AccountSteps.getTransactions(user, receiverAccount.getId());

        softly.assertThat(receiverTransactionsAfter)
                .as("Receiver transactions count should not change")
                .hasSize(receiverTransactionsBefore.size());

        softly.assertThat(receiverTransactionsAfter)
                .as("No TRANSFER transactions should be created")
                .noneMatch(tx -> tx.getType().equals("TRANSFER"));

        softly.assertThat(receiverTransactionsAfter)
                .as("Only DEPOSIT transactions should exist")
                .allMatch(tx -> tx.getType().equals("DEPOSIT"));
    }

    @Test
    public void userCanTransferWithNotReceiverAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccount = AccountSteps.createAccount(user);

        List<TransactionResponse> senderTransactionsBefore = AccountSteps.getTransactions(user, senderAccount.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccount.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(AlertMessage.BAD_REQUEST_WITH_TEXT.getMessage()))
                .post(transferRequest);

        List<TransactionResponse> senderTransactionsAfter =
                AccountSteps.getTransactions(user, senderAccount.getId());

        softly.assertThat(senderTransactionsAfter).as("Sender transactions count should not change")
                .hasSize(senderTransactionsBefore.size());

        softly.assertThat(senderTransactionsAfter).as("No TRANSFER transactions should be created")
                .noneMatch(tx -> tx.getType().equals("TRANSFER"));

        softly.assertThat(senderTransactionsAfter).as("Only DEPOSIT transactions should exist")
                .allMatch(tx -> tx.getType().equals("DEPOSIT"));
    }

    @Test
    public void userCanTransferWithNotAmount() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccount = AccountSteps.createAccount(user);
        CreateAccountResponse receiverAccount = AccountSteps.createAccount(user);

        DepositSteps.depositMoney(user, senderAccount.getId(), new BigDecimal("5000.00"));

        List<TransactionResponse> senderTransactionsBefore = AccountSteps.getTransactions(user, senderAccount.getId());
        List<TransactionResponse> receiverTransactionsBefore = AccountSteps.getTransactions(user, receiverAccount.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccount.getId()))
                .receiverAccountId(Math.toIntExact(receiverAccount.getId()))
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsInternalServerErrorWithText())
                .post(transferRequest);

        List<TransactionResponse> senderTransactionsAfter =
                AccountSteps.getTransactions(user, senderAccount.getId());
        List<TransactionResponse> receiverTransactionsAfter =
                AccountSteps.getTransactions(user, receiverAccount.getId());

        softly.assertThat(senderTransactionsAfter).as("Sender transactions count should not change")
                .hasSize(senderTransactionsBefore.size());

        softly.assertThat(senderTransactionsAfter).as("No TRANSFER transactions should be created for sender")
                .noneMatch(tx -> tx.getType().equals("TRANSFER"));

        softly.assertThat(receiverTransactionsAfter).as("Receiver transactions count should not change")
                .hasSize(receiverTransactionsBefore.size());

        softly.assertThat(receiverTransactionsAfter).as("No TRANSFER transactions should be created for receiver")
                .noneMatch(tx -> tx.getType().equals("TRANSFER"));
    }

    @Test
    public void userCanTransferSenderAccountIdOnReceiverAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccountId = AccountSteps.createAccount(user);

        DepositSteps.depositMoney(user, senderAccountId.getId(), new BigDecimal("5000.00"));

        List<TransactionResponse> transactionsBefore = AccountSteps.getTransactions(user, senderAccountId.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccountId.getId()))
                .receiverAccountId(Math.toIntExact(senderAccountId.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(AlertMessage.BAD_REQUEST_WITH_TEXT.getMessage()))
                .post(transferRequest);

        List<TransactionResponse> transactionsAfter =
                AccountSteps.getTransactions(user, senderAccountId.getId());

        softly.assertThat(transactionsAfter).as("Transactions count should not change")
                .hasSize(transactionsBefore.size());

        softly.assertThat(transactionsAfter).as("No TRANSFER transactions should be created")
                .noneMatch(tx -> tx.getType().equals("TRANSFER"));

        softly.assertThat(transactionsAfter).as("Only DEPOSIT transactions should exist")
                .allMatch(tx -> tx.getType().equals("DEPOSIT"));
    }

    @Test
    public void userCanTransferNotAuthorization() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccountId = AccountSteps.createAccount(user);
        CreateAccountResponse receiverAccountId = AccountSteps.createAccount(user);
        DepositSteps.depositMoney(user, senderAccountId.getId(), new BigDecimal("5000.00"));

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccountId.getId()))
                .receiverAccountId(Math.toIntExact(senderAccountId.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(RequestSpecs.unAuthSpec(), ResponseSpecs.requestReturnUnauthorized())
                .post(transferRequest);

        List<TransactionResponse> senderTransactions = AccountSteps.getTransactions(user, senderAccountId.getId());
        List<TransactionResponse> receiverTransactions = AccountSteps.getTransactions(user, receiverAccountId.getId());

        softly.assertThat(senderTransactions).as("Only DEPOSIT transactions should exist for sender")
                .allMatch(tx -> tx.getType().equals("DEPOSIT"));

        softly.assertThat(senderTransactions).as("No TRANSFER transactions for sender")
                .noneMatch(tx -> tx.getType().equals("TRANSFER"));

        softly.assertThat(receiverTransactions).as("Receiver should have no transactions")
                .isEmpty();
    }
}
