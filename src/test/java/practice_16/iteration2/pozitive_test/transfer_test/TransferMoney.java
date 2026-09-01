package practice_16.iteration2.pozitive_test.transfer_test;

import generators.RandomData;
import io.restassured.common.mapper.TypeRef;
import models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import requests.*;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

public class TransferMoney extends BaseTest {

    public static Stream<Arguments> transferWithCorrectDate() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.01")),
                Arguments.of(new BigDecimal("9999.99")),
                Arguments.of(new BigDecimal("10000.00"))
        );
    }

    @MethodSource("transferWithCorrectDate")
    @ParameterizedTest
    public void userCanTransferWithCorrectDate(BigDecimal amount) {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        AccountResponse accountId1 = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(AccountResponse.class);

        AccountResponse accountId2 = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(AccountResponse.class);

        BigDecimal depositAmount = new BigDecimal("5000.00");

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId1.getId())
                .balance(depositAmount)
                .build();

        new DepositMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        new DepositMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId1.getId())
                .receiverAccountId(accountId2.getId())
                .amount(amount)
                .build();

        TransferMoneyResponse response = new TransferMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract()
                .as(TransferMoneyResponse.class);

        softly.assertThat(response.getSenderAccountId()).isEqualTo(accountId1.getId());
        softly.assertThat(response.getReceiverAccountId()).isEqualTo(accountId2.getId());
        softly.assertThat(response.getAmount()).isEqualByComparingTo(amount);
        softly.assertThat(response.getMessage()).isEqualTo(AlertMessage.TRANSFER_SUCCESS.getMessage());

        GetAccountTransactionsRequester transactionsRequester = new GetAccountTransactionsRequester(
                        RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                        ResponseSpecs.requestReturnsOK());

        List<TransactionResponse> senderTransactions =
                transactionsRequester
                        .get(accountId1.getId())
                        .extract()
                        .as(new TypeRef<List<TransactionResponse>>() {});

        List<TransactionResponse> receiverTransactions =
                transactionsRequester
                        .get(accountId2.getId())
                        .extract()
                        .as(new TypeRef<List<TransactionResponse>>() {});

        TransactionResponse senderTransfer = senderTransactions.stream()
                .filter(transaction -> transaction.getAmount().compareTo(amount) == 0)
                .filter(transaction -> transaction.getRelatedAccountId() == accountId2.getId())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transfer transaction was not found for sender account " + accountId1.getId()));

        TransactionResponse receiverTransfer = receiverTransactions.stream()
                .filter(transaction -> transaction.getAmount().compareTo(amount) == 0)
                .filter(transaction -> transaction.getRelatedAccountId() == accountId1.getId())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transfer transaction was not found for receiver account " + accountId2.getId()));

        softly.assertThat(senderTransfer.getAmount()).isEqualByComparingTo(amount);
        softly.assertThat(senderTransfer.getRelatedAccountId()).isEqualTo(accountId2.getId());
        softly.assertThat(receiverTransfer.getAmount()).isEqualByComparingTo(amount);
        softly.assertThat(receiverTransfer.getRelatedAccountId()).isEqualTo(accountId1.getId());
    }
}
