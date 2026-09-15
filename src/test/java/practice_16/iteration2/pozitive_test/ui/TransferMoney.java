package practice_16.iteration2.pozitive_test.ui;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.TransactionResponse;
import api.models.TransactionType;
import api.requests.steps.AdminSteps;
import api.requests.steps.UserSteps;
import common.data.TestData;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoney extends BaseUiTest{

    @Test
    public void userCanDepositWithCorrectDate() {

        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user.getUsername(), user.getPassword());

        UserDashboard dashboard = new UserDashboard().open();

        dashboard.createNewAccount().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage());
        dashboard.createNewAccount().checkAlertMessageAndAccept(BankAlert.NEW_ACCOUNT_CREATED.getMessage());

        List<CreateAccountResponse> accounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        assertThat(accounts).as(String.valueOf(BankAlert.DEPOSIT_TWO)).hasSize(2);

        CreateAccountResponse senderAccount = accounts.get(0);
        CreateAccountResponse recipientAccount = accounts.get(1);

        String senderAccountNumber = senderAccount.getAccountNumber();
        String recipientAccountNumber = recipientAccount.getAccountNumber();
        int senderAccountId = Math.toIntExact(senderAccount.getId());
        int recipientAccountId = Math.toIntExact(recipientAccount.getId());

        BigDecimal depositAmount = TestData.MAX_DEPOSIT.getAmount();

        dashboard.openDepositMoney().selectAccount(senderAccountNumber).enterDepositAmount(depositAmount).deposit();

        dashboard.checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULY.format(depositAmount, senderAccountNumber));

        BigDecimal transferAmount = TestData.MIN_TRANSFER.getAmount();
        String recipientName = TestData.RECIPIENT_NAME.getString();

        dashboard
                .openTransferMoney()
                .selectSenderAccount(senderAccountNumber)
                .enterRecipientName(recipientName)
                .selectRecipientAccount(recipientAccountNumber)
                .enterTransferAmount(transferAmount)
                .confirmDetails()
                .sendTransfer();

        dashboard.checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULY.format(transferAmount, recipientAccountNumber));

        List<TransactionResponse> senderTransactions = new UserSteps(user.getUsername(), user.getPassword()).getAccountTransactions(senderAccountId);

        List<TransactionResponse> recipientTransactions = new UserSteps(user.getUsername(), user.getPassword()).getAccountTransactions(recipientAccountId);

        assertThat(senderTransactions).as("На счете-отправителе должно быть 2 транзакции: DEPOSIT + TRANSFER_OUT").hasSize(2);

        TransactionResponse transferOut = senderTransactions.stream()
                .filter(t -> t.getType().equals(TransactionType.TRANSFER_OUT.getValue()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(TransactionType.TRANSFER_OUT2));

        assertThat(transferOut.getAmount()).as("Сумма списания должна соответствовать переводу").isEqualByComparingTo(transferAmount);
        assertThat(transferOut.getRelatedAccountId()).as("ID связанного счета должен быть ID получателя").isEqualTo(recipientAccountId);
        assertThat(recipientTransactions).as("На счете-получателе должна быть 1 транзакция: TRANSFER_IN").hasSize(1);

        TransactionResponse transferIn = recipientTransactions.getFirst();
        assertThat(transferIn.getType()).as("Тип транзакции должен быть TRANSFER_IN").isEqualTo(TransactionType.TRANSFER_IN.getValue());
        assertThat(transferIn.getAmount()).as("Сумма зачисления должна соответствовать переводу").isEqualByComparingTo(transferAmount);

        List<CreateAccountResponse> updatedAccounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        CreateAccountResponse updatedSender = updatedAccounts.stream()
                .filter(a -> a.getAccountNumber().equals(senderAccountNumber))
                .findFirst()
                .orElseThrow();

        CreateAccountResponse updatedRecipient = updatedAccounts.stream()
                .filter(a -> a.getAccountNumber().equals(recipientAccountNumber))
                .findFirst()
                .orElseThrow();

        assertThat(updatedSender.getBalance()).as("Баланс отправителя должен быть: 5000.00 - 1.00 = 4999.00").isEqualByComparingTo(depositAmount.subtract(transferAmount));
        assertThat(updatedRecipient.getBalance()).as("Баланс получателя должен быть равен сумме перевода").isEqualByComparingTo(transferAmount);
    }

    @Test
    public void userCanDepositWithCorrectDateTransferAgain() {

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
        int recipientAccountId = Math.toIntExact(recipientAccount.getId());

        BigDecimal depositAmount = TestData.MAX_DEPOSIT.getAmount();
        BigDecimal firstTransferAmount = TestData.THOUSAND_TRANSFER.getAmount();
        BigDecimal repeatTransferAmount = TestData.MIN_TRANSFER.getAmount();

        String recipientName = TestData.RECIPIENT_NAME.getString();

        dashboard.openDepositMoney().selectAccount(senderAccountNumber).enterDepositAmount(depositAmount).deposit();

        dashboard.checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESSFULY.format(depositAmount, senderAccountNumber));

        dashboard.openTransferMoney()
                .selectSenderAccount(senderAccountNumber)
                .enterRecipientName(recipientName)
                .selectRecipientAccount(recipientAccountNumber)
                .enterTransferAmount(firstTransferAmount)
                .confirmDetails()
                .sendTransfer();

        dashboard.checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESSFULY.format(firstTransferAmount, recipientAccountNumber));

        List<TransactionResponse> senderTransactionsBefore = new UserSteps(user.getUsername(), user.getPassword()).getAccountTransactions(senderAccountId);

        assertThat(senderTransactionsBefore).as("Перед Transfer Again должны быть транзакции").isNotEmpty();

        dashboard.openTransferMoney()
                .clickTransferAgain()
                .searchTransaction(user.getUsername())
                .clickRepeat()
                .selectAccountInModal(senderAccountNumber)
                .enterAmountInModal(repeatTransferAmount)
                .confirmDetails()
                .sendTransferInModal();

        dashboard.checkAlertMessageAndAccept(BankAlert.TRANSFER_AGAIN_SUCCESSFULY.format(repeatTransferAmount));

        List<TransactionResponse> senderTransactions = new UserSteps(user.getUsername(), user.getPassword()).getAccountTransactions(senderAccountId);
        List<TransactionResponse> recipientTransactions = new UserSteps(user.getUsername(), user.getPassword()).getAccountTransactions(recipientAccountId);

        assertThat(senderTransactions).as("На счёте-отправителе должно быть 3 транзакции").hasSize(3);

        assertThat(senderTransactions).extracting(TransactionResponse::getType).containsExactlyInAnyOrder(
                TransactionType.DEPOSIT.getValue(),
                TransactionType.TRANSFER_OUT.getValue(),
                TransactionType.TRANSFER_OUT.getValue());

        List<TransactionResponse> transfersOut = senderTransactions.stream()
                .filter(t -> t.getType().equals(TransactionType.TRANSFER_OUT.getValue()))
                .toList();

        assertThat(transfersOut).extracting(TransactionResponse::getAmount).containsExactlyInAnyOrder(firstTransferAmount, repeatTransferAmount);

        transfersOut.forEach(t -> assertThat(t.getRelatedAccountId()).as("ID связанного счёта = ID получателя").isEqualTo(recipientAccountId));

        assertThat(recipientTransactions).as("На счёте-получателе должно быть 2 транзакции").hasSize(2);
        assertThat(recipientTransactions).extracting(TransactionResponse::getType).containsExactlyInAnyOrder(
                TransactionType.TRANSFER_IN.getValue(),
                TransactionType.TRANSFER_IN.getValue());

        List<CreateAccountResponse> updatedAccounts = new UserSteps(user.getUsername(), user.getPassword()).getAllAccounts();

        CreateAccountResponse updatedSender = updatedAccounts.stream()
                .filter(a -> a.getAccountNumber().equals(senderAccountNumber))
                .findFirst()
                .orElseThrow();

        CreateAccountResponse updatedRecipient = updatedAccounts.stream()
                .filter(a -> a.getAccountNumber().equals(recipientAccountNumber))
                .findFirst()
                .orElseThrow();

        BigDecimal expectedSenderBalance = depositAmount.subtract(firstTransferAmount).subtract(repeatTransferAmount);
        BigDecimal expectedRecipientBalance = firstTransferAmount.add(repeatTransferAmount);

        assertThat(updatedSender.getBalance()).as("Баланс отправителя после двух переводов").isEqualByComparingTo(expectedSenderBalance);

        assertThat(updatedRecipient.getBalance()).as("Баланс получателя после двух переводов").isEqualByComparingTo(expectedRecipientBalance);
    }
}