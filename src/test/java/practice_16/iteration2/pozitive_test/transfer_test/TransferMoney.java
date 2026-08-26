package practice_16.iteration2.pozitive_test.transfer_test;

import generators.RandomData;
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

        int accountId1 = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        int accountId2 = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId1)
                .balance(BigDecimal.valueOf(5000))
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
                .senderAccountId(accountId1)
                .receiverAccountId(accountId2)
                .amount(amount)
                .build();

        TransferMoneyResponse response = new TransferMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract()
                .as(TransferMoneyResponse.class);

        softly.assertThat(response.getSenderAccountId()).isEqualTo(accountId1);
        softly.assertThat(response.getReceiverAccountId()).isEqualTo(accountId2);
        softly.assertThat(response.getMessage()).isEqualTo("Transfer successful");
        softly.assertThat(response.getAmount()).isEqualByComparingTo(amount);

        List<TransactionResponse> senderTransactions = new GetAccountTransactionsRequester(
                        RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                        ResponseSpecs.requestReturnsOK())
                        .get(accountId1)
                        .extract()
                        .jsonPath()
                        .getList(".", TransactionResponse.class);

        List<TransactionResponse> receiverTransactions =
                new GetAccountTransactionsRequester(
                        RequestSpecs.authAsUser(
                                userRequest.getUsername(),
                                userRequest.getPassword()),
                        ResponseSpecs.requestReturnsOK())
                        .get(accountId2)
                        .extract()
                        .jsonPath()
                        .getList(".", TransactionResponse.class);

        TransactionResponse senderTransfer =
                senderTransactions.stream()
                        .filter(transaction -> transaction.getAmount().compareTo(amount) == 0)
                        .filter(transaction -> transaction.getRelatedAccountId() == accountId2)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Transfer transaction was not found for sender"));

        TransactionResponse receiverTransfer =
                receiverTransactions.stream()
                        .filter(transaction -> transaction.getAmount().compareTo(amount) == 0)
                        .filter(transaction -> transaction.getRelatedAccountId() == accountId1)
                        .findFirst()
                        .orElseThrow(() -> new AssertionError("Transfer transaction was not found for receiver"));

        // Проверяем транзакцию отправителя
        softly.assertThat(senderTransfer.getAmount()).isEqualByComparingTo(amount);
        softly.assertThat(senderTransfer.getRelatedAccountId()).isEqualTo(accountId2);

        // Проверяем транзакцию получателя
        softly.assertThat(receiverTransfer.getAmount()).isEqualByComparingTo(amount);
        softly.assertThat(receiverTransfer.getRelatedAccountId()).isEqualTo(accountId1);

    }
}
