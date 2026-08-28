package practice_16.iteration2.negative_test.transfer_test;

import generators.RandomData;
import models.*;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import requests.AdminCreateUserRequester;
import requests.CreateAccountRequester;
import requests.DepositMoneyRequester;
import requests.TransferMoneyRequester;
import requests.steps.AccountSteps;
import requests.steps.AdminSteps;
import requests.steps.DepositSteps;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class TransferMoney extends BaseTest {

    public static Stream<Arguments> transferNotCorrectDate() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.00"), "Transfer amount must be at least 0.01"),
                Arguments.of(new BigDecimal("-0.01"), "Transfer amount must be at least 0.01"),
                Arguments.of(new BigDecimal("10000.01"), "Transfer amount cannot exceed 10000")
        );
    }

    @MethodSource("transferNotCorrectDate")
    @ParameterizedTest
    public void userCanTransferWithNotCorrectDate(BigDecimal amount, String expectedMessage) {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccountId = AccountSteps.createAccount(user);
        CreateAccountResponse receiverAccountId = AccountSteps.createAccount(user);
        DepositSteps.depositMoney(user, senderAccountId.getId(), new BigDecimal("5000.00"));
        DepositSteps.depositMoney(user, senderAccountId.getId(), new BigDecimal("5000.00"));

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccountId.getId()))
                .receiverAccountId(Math.toIntExact(receiverAccountId.getId()))
                .amount(amount)
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(expectedMessage))
                .post(transferRequest);
    }

    @Test
    public void userCanTransferWithNotCorrectReceiverAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccountId = AccountSteps.createAccount(user);


        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccountId.getId()))
                .receiverAccountId(99999)
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transferRequest);
    }

    @Test
    public void userCanTransferWithNotCorrectSenderAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse receiverAccountId = AccountSteps.createAccount(user);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(99999)
                .receiverAccountId(Math.toIntExact(receiverAccountId.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsForbiddenWithText("Unauthorized access to account"))
                .post(transferRequest);
    }

    @Test
    public void userCanTransferWithNotSenderAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse receiverAccountId = AccountSteps.createAccount(user);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .receiverAccountId(Math.toIntExact(receiverAccountId.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsForbiddenWithText("Unauthorized access to account"))
                .post(transferRequest);
    }

    @Test
    public void userCanTransferWithNotReceiverAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccountId = AccountSteps.createAccount(user);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccountId.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transferRequest);
    }

    @Test
    public void userCanTransferWithNotAmount() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccountId = AccountSteps.createAccount(user);
        CreateAccountResponse receiverAccountId = AccountSteps.createAccount(user);
        DepositSteps.depositMoney(user, senderAccountId.getId(), new BigDecimal("5000.00"));

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccountId.getId()))
                .receiverAccountId(Math.toIntExact(receiverAccountId.getId()))
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsInternalServerErrorWithText())
                .post(transferRequest);
    }

    @Test
    public void userCanTransferSenderAccountIdOnReceiverAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccountId = AccountSteps.createAccount(user);
        DepositSteps.depositMoney(user, senderAccountId.getId(), new BigDecimal("5000.00"));

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccountId.getId()))
                .receiverAccountId(Math.toIntExact(senderAccountId.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transferRequest);
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
    }
}
