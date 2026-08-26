package practice_16.iteration2.negative_test.transfer_test;

import generators.RandomData;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.TransferMoneyRequest;
import models.UserRole;
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

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(expectedMessage))
                .post(transferRequest);
    }

    @Test
    public void userCanTransferWithNotCorrectReceiverAccountId() {

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

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId1)
                .receiverAccountId(99999)
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transferRequest);
    }

    @Test
    public void userCanTransferWithNotCorrectSenderAccountId() {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        int accountId2 = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post(null)
                .extract()
                .path("id");

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(99999)
                .receiverAccountId(accountId2)
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbiddenWithText("Unauthorized access to account"))
                .post(transferRequest);
    }

    @Test
    public void userCanTransferWithNotSenderAccountId() {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        String requestBody = """
                {
                "receiverAccountId": 1,
                "amount": 10.00
                }
                """;

        given()
                .spec(RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()))
                .body(requestBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCanTransferWithNotReceiverAccountId() {
        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        String requestBody = """
                {
                "senderAccountId": 1,
                "amount": 10.00
                }
                """;

        given()
                .spec(RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()))
                .body(requestBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCanTransferWithNotAmount() {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        String requestBody = """
                {
                "senderAccountId": 1,
                "receiverAccountId": 2,
                }
                """;

        given()
                .spec(RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()))
                .body(requestBody)
                .post("/api/v1/accounts/transfer")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanTransferSenderAccountIdOnReceiverAccountId() {

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

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId1)
                .balance(BigDecimal.valueOf(5000))
                .build();

        new DepositMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(accountId1)
                .receiverAccountId(accountId1)
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText("Invalid transfer: insufficient funds or invalid accounts"))
                .post(transferRequest);
    }

    @Test
    public void userCanTransferNotAuthorization() {

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
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(RequestSpecs.unAuthSpec(), ResponseSpecs.requestReturnUnauthorized())
                .post(transferRequest);
    }
}
