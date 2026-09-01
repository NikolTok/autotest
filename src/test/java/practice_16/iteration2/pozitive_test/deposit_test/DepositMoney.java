package practice_16.iteration2.pozitive_test.deposit_test;

import generators.RandomData;
import models.*;
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

        int account = accountId.getId();

        DepositMoneyRequest depositRequest = DepositMoneyRequest.builder()
                .id(accountId.getId())
                .balance(balance)
                .build();

        DepositMoneyResponse response = new DepositMoneyRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(depositRequest)
                .extract()
                .as(DepositMoneyResponse.class);

        softly.assertThat(response.getId()).isEqualTo(accountId.getId());
        softly.assertThat(response.getAccountNumber()).isEqualTo("ACC" + account);
        softly.assertThat(response.getBalance()).isEqualByComparingTo(balance);
        softly.assertThat(response.getTransactions()).hasSize(1);
        softly.assertThat(response.getTransactions().get(0).getAmount()).isEqualByComparingTo(balance);
        softly.assertThat(response.getTransactions().get(0).getType()).isEqualTo("DEPOSIT");
        softly.assertThat(response.getTransactions().get(0).getRelatedAccountId()).isEqualTo(account);
    }
}
