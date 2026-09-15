package practice_16.iteration2.pozitive_test.ui;

import api.generators.RandomData;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.TransactionResponse;
import api.models.TransactionType;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import org.junit.jupiter.api.Test;
import practice_16.iteration1.ui.BaseUiTest;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoney extends BaseUiTest {

    @Test
    public void userCanDepositWithCorrectData() {

        CreateUserRequest user = AdminSteps.createUser();

        authAsUser(user.getUsername(), user.getPassword());

        UserDashboard dashboard = new UserDashboard().open();

        dashboard.createNewAccount().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage());

        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        assertThat(accounts).hasSize(1);

        CreateAccountResponse account = accounts.getFirst();

        assertThat(account.getBalance()).isZero();

        String accountNumber = account.getAccountNumber();
        int accountId = Math.toIntExact(account.getId());

        BigDecimal depositAmount = RandomData.getAmount();

        dashboard.openDepositMoney().selectAccount(accountNumber).enterDepositAmount(depositAmount).deposit();

        dashboard.checkAlertMessageAndAccept(String.format(Locale.US, BankAlert.DEPOSIT_SUCCESSFULY.getMessage(), depositAmount, accountNumber));

        List<TransactionResponse> transactions = new UserSteps(user.getUsername(), user.getPassword()).getAccountTransactions(accountId);

        assertThat(transactions).hasSize(1);

        TransactionResponse transaction = transactions.getFirst();

        assertThat(transaction.getAmount()).isEqualByComparingTo(depositAmount);
        assertThat(transaction.getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(transaction.getRelatedAccountId()).isEqualTo(accountId);
    }
}
