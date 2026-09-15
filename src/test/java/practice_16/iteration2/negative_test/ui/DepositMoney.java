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

public class DepositMoney extends BaseUiTest{

    public static Stream<Arguments> depositNotCorrectDate() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.00"),"❌ Please enter a valid amount."),
                Arguments.of(new BigDecimal("-0.01"), "❌ Please enter a valid amount."),
                Arguments.of(new BigDecimal("5000.01"), "❌ Please deposit less or equal to 5000$.")
        );
    }

    @MethodSource("depositNotCorrectDate")
    @ParameterizedTest
    public void userCannotDepositInvalidAmount(BigDecimal balance, String expectedMessage) {

        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user.getUsername(), user.getPassword());

        UserDashboard dashboard = new UserDashboard().open();
        dashboard.createNewAccount().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage());

        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        assertThat(accounts).hasSize(1);

        CreateAccountResponse account = accounts.getFirst();
        String accountNumber = account.getAccountNumber();
        int accountId = Math.toIntExact(account.getId());

        dashboard.openDepositMoney().selectAccount(accountNumber).enterDepositAmount(balance).deposit();

        dashboard.checkAlertMessageAndAccept(expectedMessage);

        List<TransactionResponse> transactions = new UserSteps(user.getUsername(), user.getPassword()).getAccountTransactions(accountId);

        assertThat(transactions).as("Транзакций не должно быть после неудачного депозита на сумму %s", balance).isEmpty();

        List<CreateAccountResponse> updatedAccounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        assertThat(updatedAccounts.getFirst().getBalance()).isZero();
    }

    @Test
    public void userCannotDepositInvalidWords() {

        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user.getUsername(), user.getPassword());

        UserDashboard dashboard = new UserDashboard().open();
        dashboard.createNewAccount().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage());

        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        assertThat(accounts).hasSize(1);

        CreateAccountResponse account = accounts.getFirst();
        String accountNumber = account.getAccountNumber();
        int accountId = Math.toIntExact(account.getId());

        dashboard.openDepositMoney().selectAccount(accountNumber).enterDepositAmount(TestData.TEST_WORD.getString());

        String actualValue = dashboard.getAmountInputValue();

        assertThat(actualValue).isEmpty();

        List<TransactionResponse> transactions = new UserSteps(user.getUsername(), user.getPassword()).getAccountTransactions(accountId);

        assertThat(transactions).isEmpty();

        List<CreateAccountResponse> updatedAccounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        assertThat(updatedAccounts.getFirst().getBalance()).isZero();
    }

    @Test
    public void userCannotDepositEmpty() {

        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user.getUsername(), user.getPassword());

        UserDashboard dashboard = new UserDashboard().open();
        dashboard.createNewAccount().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage());

        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        assertThat(accounts).hasSize(1);

        CreateAccountResponse account = accounts.getFirst();
        String accountNumber = account.getAccountNumber();
        int accountId = Math.toIntExact(account.getId());

        dashboard.openDepositMoney().selectAccount(accountNumber).deposit();

        dashboard.checkAlertMessageAndAccept(BankAlert.DEPOSIT_INVALID.getMessage());

        List<TransactionResponse> transactions = new UserSteps(user.getUsername(), user.getPassword()).getAccountTransactions(accountId);

        assertThat(transactions).isEmpty();

        List<CreateAccountResponse> updatedAccounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        assertThat(updatedAccounts.getFirst().getBalance()).isZero();
    }
}
