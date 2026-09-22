package practice_16.iteration2.negative_test.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.generators.RandomData;
import api.models.*;
import api.requests.steps.DataBaseSteps;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import api.requests.DepositMoneyRequester;
import api.requests.GetAccountTransactionsRequester;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class DepositMoney extends BaseTest {

    public static Stream<Arguments> depositNotCorrectDate() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.00"), "Deposit amount must be at least 0.01"),
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

        List<TransactionResponse> transactions =
                new GetAccountTransactionsRequester(
                        RequestSpecs.authAsUser(
                                user.getUsername(),
                                user.getPassword()),
                        ResponseSpecs.requestReturnsOK())
                        .get(Math.toIntExact(account.getId()))
                        .extract()
                        .as(new TypeRef<List<TransactionResponse>>() {
                        });

        softly.assertThat(transactions).isEmpty();

        AccountDao accountDao = DataBaseSteps.getAccountById(account.getId());
        softly.assertThat(accountDao).as("Счёт должен существовать в БД").isNotNull();
        softly.assertThat(accountDao.getBalance()).as("Баланс счёта в БД не должен измениться после неудачного депозита").isEqualTo(0.0);

        DaoAndModelAssertions.assertThat(CreateAccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(BigDecimal.ZERO)
                .build(), accountDao).match();
    }

    @Test
    public void userCannotDepositToNonExistingAccount() {

        CreateUserRequest user = AdminSteps.createUser();

        long nonExistingAccountId = 999_999L;

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(Math.toIntExact(nonExistingAccountId))
                .balance(RandomData.getBalance())
                .build();

        new DepositMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsForbiddenWithText(AlertMessage.FORBIDDEN_WITH_TEXT.getMessage()))
                .post(depositRequest);

        AccountDao accountDao = DataBaseSteps.getAccountById(nonExistingAccountId);
        softly.assertThat(accountDao).as("Счёт с ID %d не должен существовать в БД", nonExistingAccountId).isNull();
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
                .spec(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()))
                .contentType(ContentType.JSON)
                .body(requestBody)
                .post("/accounts/deposit")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        AccountDao accountDao = DataBaseSteps.getAccountById(1L);

        if (accountDao != null) {
            softly.assertThat(accountDao.getBalance()).as("Баланс счёта не должен измениться после невалидного запроса").isEqualTo(0.0);
        }
    }

    @Test
    public void userCanDepositNotAuthorization() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = AccountSteps.createAccount(user);

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(Math.toIntExact(account.getId()))
                .balance(RandomData.getBalance())
                .build();

        new DepositMoneyRequester(
                RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnUnauthorized())
                .post(depositRequest);

        AccountDao accountDao = DataBaseSteps.getAccountById(account.getId());
        softly.assertThat(accountDao).as("Счёт должен существовать в БД").isNotNull();
        softly.assertThat(accountDao.getBalance()).as("Баланс счёта НЕ должен измениться без авторизации").isEqualTo(0.0);

    }
}

