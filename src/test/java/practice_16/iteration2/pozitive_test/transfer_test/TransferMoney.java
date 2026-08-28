package practice_16.iteration2.pozitive_test.transfer_test;

import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.TransferMoneyRequest;
import models.TransferMoneyResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import requests.TransferMoneyRequester;
import requests.assertions.TransferAssertions;
import requests.steps.AccountSteps;
import requests.steps.AdminSteps;
import requests.steps.DepositSteps;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.math.BigDecimal;
import java.util.stream.Stream;

public class TransferMoney extends BaseTest {

    public static Stream<Arguments> transferWithCorrectAmount() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.01")),
                Arguments.of(new BigDecimal("9999.99")),
                Arguments.of(new BigDecimal("10000.00"))
        );
    }

    @MethodSource("transferWithCorrectAmount")
    @ParameterizedTest
    public void userCanTransferWithCorrectAmount(BigDecimal amount) {

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

        TransferMoneyResponse response = new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .post(transferRequest)
                .extract()
                .as(TransferMoneyResponse.class);

        ModelAssertions.assertThatModels(transferRequest, response).match();
        softly.assertThat(response.getMessage()).isEqualTo("Transfer successful");
        TransferAssertions.assertSuccessfulTransfer(user, senderAccountId, receiverAccountId, amount, response);
    }
}
