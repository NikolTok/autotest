package practice_16.iteration2.negative_test.deposit_test;

import generators.RandomData;
import models.AccountResponse;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
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
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class DepositMoney extends BaseTest {

    public static Stream<Arguments> depositNotCorrectDate() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.00"),"Deposit amount must be at least 0.01"),
                Arguments.of(new BigDecimal("-0.01"), "Deposit amount must be at least 0.01"),
                Arguments.of(new BigDecimal("5000.01"), "Deposit amount cannot exceed 5000")
        );
    }

    @MethodSource("depositNotCorrectDate")
    @ParameterizedTest
    public void userCannotDepositInvalidAmount(BigDecimal balance, String expectedMessage) {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        AccountResponse accountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(AccountResponse.class);

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId.getId())
                .balance(balance)
                .build();

        new DepositMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(expectedMessage))
                .post(depositRequest);
    }

    @Test
    public void userCannotDepositToNonExistingAccount() {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(999999)
                .balance(RandomData.getBalance())
                .build();

        new DepositMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsForbiddenWithText("Unauthorized access to account"))
                .post(depositRequest);
    }

    @Test
    public void userCannotDepositWithInvalidAccountId() {

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
                "balance": 100.00
            }
            """;

        given()
                .spec(RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()))
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCannotDepositWithInvalidBalance() {

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
                "id": 1,
            }
            """;

        given()
                .spec(RequestSpecs.authAsUser(
                        userRequest.getUsername(),
                        userRequest.getPassword()))
                .body(requestBody)
                .post("/api/v1/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanDepositNotAuthorization() {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        AccountResponse accountId = new CreateAccountRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated())
                .post()
                .extract()
                .as(AccountResponse.class);

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId.getId())
                .balance(RandomData.getBalance())
                .build();

        new DepositMoneyRequester(RequestSpecs.unAuthSpec(), ResponseSpecs.requestReturnUnauthorized())
                .post(depositRequest);
    }
}
