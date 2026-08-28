package practice_16.iteration2.negative_test.deposit_test;

import generators.RandomData;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import requests.DepositMoneyRequester;
import requests.steps.AccountSteps;
import requests.steps.AdminSteps;
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

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = AccountSteps.createAccount(user);

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(Math.toIntExact(account.getId()))
                .balance(balance)
                .build();

        new DepositMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(expectedMessage))
                .post(depositRequest);
    }

    @Test
    public void userCannotDepositToNonExistingAccount() {

        CreateUserRequest user = AdminSteps.createUser();

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(999999)
                .balance(RandomData.getBalance())
                .build();

        new DepositMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsForbiddenWithText("Unauthorized access to account"))
                .post(depositRequest);
    }

    @Test
    public void userCannotDepositWithInvalidAccountId() {

        CreateUserRequest user = AdminSteps.createUser();

        String requestBody = """
            {
                "balance": 100.00
            }
            """;

        given()
                .spec(RequestSpecs.authAsUser(
                        user.getUsername(),
                        user.getPassword()))
                .body(requestBody)
                .post("/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCannotDepositWithInvalidBalance() {

        CreateUserRequest user = AdminSteps.createUser();

        String requestBody = """
            {
                "id": 1,
            }
            """;

        given()
                .spec(RequestSpecs.authAsUser(
                        user.getUsername(),
                        user.getPassword()))
                .body(requestBody)
                .post("/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanDepositNotAuthorization() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = AccountSteps.createAccount(user);

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(Math.toIntExact(account.getId()))
                .balance(RandomData.getBalance())
                .build();

        new DepositMoneyRequester(RequestSpecs.unAuthSpec(), ResponseSpecs.requestReturnUnauthorized())
                .post(depositRequest);
    }
}
