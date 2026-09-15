package practice_16.iteration2.negative_test.ui;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.TransactionResponse;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import common.data.TestData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoney extends BaseUiTest{

    public static Stream<Arguments> transferNotCorrectDate() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.00"), "Transfer amount must be at least 0.01"),
                Arguments.of(new BigDecimal("-0.01"), "Transfer amount must be at least 0.01"),
                Arguments.of(new BigDecimal("10000.01"), "Transfer amount cannot exceed 10000"));
    }

    @MethodSource("transferNotCorrectDate")
    @ParameterizedTest
    public void userCannotTransferWithInvalidAmount(BigDecimal amount, String expectedMessage) {

        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user.getUsername(), user.getPassword());

        UserDashboard dashboard = new UserDashboard().open();
        dashboard.createNewAccount().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage());
        dashboard.createNewAccount().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage());

        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        assertThat(accounts).as("Должно быть создано 2 аккаунта").hasSize(2);

        CreateAccountResponse senderAccount = accounts.get(0);
        CreateAccountResponse recipientAccount = accounts.get(1);

        String senderAccountNumber = senderAccount.getAccountNumber();
        String recipientAccountNumber = recipientAccount.getAccountNumber();
        int senderAccountId = Math.toIntExact(senderAccount.getId());

        BigDecimal depositAmount = TestData.MAX_DEPOSIT.getAmount();
        String recipientName = TestData.RECIPIENT_NAME.getString();

        dashboard
                .openDepositMoney()
                .selectAccount(senderAccountNumber)
                .enterDepositAmount(depositAmount)
                .deposit();

        dashboard.checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULY.format(depositAmount, senderAccountNumber));

        dashboard.openTransferMoney()
                .selectSenderAccount(senderAccountNumber)
                .enterRecipientName(recipientName)
                .selectRecipientAccount(recipientAccountNumber)
                .enterTransferAmount(amount)
                .confirmDetails()
                .sendTransfer();

        dashboard.checkAlertMessageAndAccept(expectedMessage);

        List<TransactionResponse> senderTransactions = new UserSteps(user.getUsername(), user.getPassword()).getAccountTransactions(senderAccountId);

        assertThat(senderTransactions).as("Не должно быть транзакции TRANSFER_OUT после неудачной попытки на сумму %s", amount).noneMatch(t -> t.getType().equals("TRANSFER_OUT"));

        List<CreateAccountResponse> updatedAccounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        CreateAccountResponse updatedSender = updatedAccounts.stream()
                .filter(a -> a.getAccountNumber().equals(senderAccountNumber))
                .findFirst()
                .orElseThrow();

        assertThat(updatedSender.getBalance()).as("Баланс отправителя не должен измениться после неудачного перевода").isEqualByComparingTo(depositAmount);
    }

    @Test
    public void userCannotTransferWithEmptyAmount() {

        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user.getUsername(), user.getPassword());

        UserDashboard dashboard = new UserDashboard().open();
        dashboard.createNewAccount().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage());
        dashboard.createNewAccount().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage());

        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        assertThat(accounts).as("Должно быть создано 2 аккаунта").hasSize(2);

        CreateAccountResponse senderAccount = accounts.get(0);
        CreateAccountResponse recipientAccount = accounts.get(1);

        String senderAccountNumber = senderAccount.getAccountNumber();
        String recipientAccountNumber = recipientAccount.getAccountNumber();
        int senderAccountId = Math.toIntExact(senderAccount.getId());

        BigDecimal depositAmount = TestData.MAX_DEPOSIT.getAmount();
        String recipientName = TestData.RECIPIENT_NAME.getString();

        dashboard.openDepositMoney()
                .selectAccount(senderAccountNumber)
                .enterDepositAmount(depositAmount)
                .deposit();

        dashboard.checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULY.format(depositAmount, senderAccountNumber));

        dashboard.openTransferMoney()
                .selectSenderAccount(senderAccountNumber)
                .enterRecipientName(recipientName)
                .selectRecipientAccount(recipientAccountNumber)
                .confirmDetails()
                .sendTransfer();

        dashboard.checkAlertMessageAndAccept(BankAlert.FILL_ALL_FIELDS.getMessage());

        List<TransactionResponse> senderTransactions = new UserSteps(user.getUsername(), user.getPassword()).getAccountTransactions(senderAccountId);

        assertThat(senderTransactions).as("Не должно быть транзакции TRANSFER_OUT после пустого перевода").noneMatch(t -> t.getType().equals("TRANSFER_OUT"));

        List<CreateAccountResponse> updatedAccounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        CreateAccountResponse updatedSender = updatedAccounts.stream()
                .filter(a -> a.getAccountNumber().equals(senderAccountNumber))
                .findFirst()
                .orElseThrow();

        assertThat(updatedSender.getBalance()).as("Баланс отправителя не должен измениться после пустого перевода").isEqualByComparingTo(depositAmount);
    }
}