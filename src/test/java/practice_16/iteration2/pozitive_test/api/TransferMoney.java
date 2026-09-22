package practice_16.iteration2.pozitive_test.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.TransferMoneyRequest;
import api.models.TransferMoneyResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.DataBaseSteps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import api.requests.TransferMoneyRequester;
import api.requests.assertions.TransferAssertions;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import api.requests.steps.DepositSteps;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;

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

        BigDecimal depositAmount = new BigDecimal("5000.00");
        DepositSteps.depositMoney(user, senderAccountId.getId(), depositAmount);
        DepositSteps.depositMoney(user, senderAccountId.getId(), depositAmount);

        BigDecimal totalDeposited = depositAmount.add(depositAmount);

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

        AccountDao senderDao = DataBaseSteps.getAccountById(senderAccountId.getId());

        softly.assertThat(senderDao).as("Счёт-отправитель должен существовать в БД").isNotNull();

        BigDecimal expectedSenderBalance = totalDeposited.subtract(amount);

        softly.assertThat(senderDao.getBalance()).as("Баланс счёта-отправителя в БД после перевода").isEqualByComparingTo(expectedSenderBalance.doubleValue());

        AccountDao receiverDao = DataBaseSteps.getAccountById(receiverAccountId.getId());

        softly.assertThat(receiverDao).as("Счёт-получатель должен существовать в БД").isNotNull();
        softly.assertThat(receiverDao.getBalance()).as("Баланс счёта-получателя в БД после перевода").isEqualByComparingTo(amount.doubleValue());

        DaoAndModelAssertions.assertThat(CreateAccountResponse.builder()
                                .id(senderAccountId.getId())
                                .accountNumber(senderAccountId.getAccountNumber())
                                .balance(expectedSenderBalance)
                                .build(), senderDao).match();

        DaoAndModelAssertions.assertThat(CreateAccountResponse.builder()
                                .id(receiverAccountId.getId())
                                .accountNumber(receiverAccountId.getAccountNumber())
                                .balance(amount)
                                .build(), receiverDao).match();
    }
}
