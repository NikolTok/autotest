package practice_16.iteration2.pozitive_test.deposit_test;

import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.DepositMoneyResponse;
import models.comparison.ModelAssertions;
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

public class DepositMoney extends BaseTest {

    public static Stream<Arguments> depositWithCorrectDate() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.01")),
                Arguments.of(new BigDecimal("4999.99")),
                Arguments.of(new BigDecimal("5000.00"))
        );
    }

    @MethodSource("depositWithCorrectDate")
    @ParameterizedTest
    public void userCanDepositWithCorrectDate(BigDecimal balance) {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse account = AccountSteps.createAccount(user);

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(Math.toIntExact(account.getId()))
                .balance(balance)
                .build();

        DepositMoneyResponse response = new DepositMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(DepositMoneyResponse.class);

        ModelAssertions.assertThatModels(depositRequest, response).match();
        softly.assertThat(response.getAccountNumber()).isEqualTo("ACC" + account.getId());
        softly.assertThat(response.getTransactions()).hasSize(1);
        softly.assertThat(response.getTransactions().get(0).getAmount()).isEqualByComparingTo(balance);
        softly.assertThat(response.getTransactions().get(0).getType()).isEqualTo("DEPOSIT");
        softly.assertThat(response.getTransactions().get(0).getRelatedAccountId()).isEqualTo(account.getId());
    }
}
