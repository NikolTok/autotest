package practice_16.iteration2.negative_test.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.generators.RandomData;
import api.models.*;
import api.requests.steps.DataBaseSteps;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import api.requests.TransferMoneyRequester;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import api.requests.steps.DepositSteps;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static api.requests.steps.AccountSteps.getTransactions;

public class TransferMoney extends BaseTest {

    private static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("5000.00");
    private static final long NON_EXISTING_ACCOUNT_ID = 99999L;

    public static Stream<Arguments> transferNotCorrectDate() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.00"), "Transfer amount must be at least 0.01", new BigDecimal("5000.00")),
                Arguments.of(new BigDecimal("-0.01"), "Transfer amount must be at least 0.01", new BigDecimal("5000.00")),
                Arguments.of(new BigDecimal("10000.01"), "Transfer amount cannot exceed 10000", new BigDecimal("5000.00")));
    }

    @MethodSource("transferNotCorrectDate")
    @ParameterizedTest
    public void userCanTransferWithNotCorrectDate(BigDecimal amount, String expectedMessage, BigDecimal maxDepositAmount) {

        CreateUserRequest user = AdminSteps.createUser();

        CreateAccountResponse senderAccount = AccountSteps.createAccount(user);
        CreateAccountResponse receiverAccount = AccountSteps.createAccount(user);

        DepositSteps.depositMoney(user, senderAccount.getId(), maxDepositAmount);
        DepositSteps.depositMoney(user, senderAccount.getId(), maxDepositAmount);

        BigDecimal totalDeposited = maxDepositAmount.add(maxDepositAmount);

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccount.getId()))
                .receiverAccountId(Math.toIntExact(receiverAccount.getId()))
                .amount(amount)
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(expectedMessage))
                .post(transferRequest);

        List<TransactionResponse> senderTransactions = getTransactions(user, senderAccount.getId());

        List<TransactionResponse> receiverTransactions = getTransactions(user, receiverAccount.getId());

        softly.assertThat(senderTransactions).noneMatch(transaction -> transaction.getType().equals("TRANSFER") && transaction.getRelatedAccountId()
                == receiverAccount.getId());

        softly.assertThat(receiverTransactions).noneMatch(transaction -> transaction.getType().equals("TRANSFER") && transaction.getRelatedAccountId()
                == senderAccount.getId());

        AccountDao senderDao = DataBaseSteps.getAccountById(senderAccount.getId());

        softly.assertThat(senderDao).as("Счёт-отправитель должен существовать в БД").isNotNull();
        softly.assertThat(BigDecimal.valueOf(senderDao.getBalance())).as("Баланс отправителя в БД не должен измениться после неудачного перевода")
                .isEqualByComparingTo(totalDeposited);

        AccountDao receiverDao = DataBaseSteps.getAccountById(receiverAccount.getId());

        softly.assertThat(receiverDao).as("Счёт-получатель должен существовать в БД").isNotNull();

        softly.assertThat(BigDecimal.valueOf(receiverDao.getBalance())).as("Баланс получателя в БД не должен измениться после неудачного перевода")
                .isEqualByComparingTo(BigDecimal.ZERO);

        DaoAndModelAssertions.assertThat(CreateAccountResponse.builder()
                .id(senderAccount.getId())
                .accountNumber(senderAccount.getAccountNumber())
                .balance(totalDeposited)
                .build(), senderDao).match();

        DaoAndModelAssertions.assertThat(CreateAccountResponse.builder()
                .id(receiverAccount.getId())
                .accountNumber(receiverAccount.getAccountNumber())
                .balance(BigDecimal.ZERO)
                .build(), receiverDao).match();
    }


    @Test
    public void userCanTransferWithNotCorrectReceiverAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccount = AccountSteps.createAccount(user);

        BigDecimal depositAmount = new BigDecimal("5000.00");
        DepositSteps.depositMoney(user, senderAccount.getId(), depositAmount);

        List<TransactionResponse> senderTransactionsBefore = AccountSteps.getTransactions(user, senderAccount.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccount.getId()))
                .receiverAccountId((int) NON_EXISTING_ACCOUNT_ID)
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(AlertMessage.BAD_REQUEST_WITH_TEXT.getMessage()))
                .post(transferRequest);

        List<TransactionResponse> senderTransactionsAfter = AccountSteps.getTransactions(user, senderAccount.getId());

        softly.assertThat(senderTransactionsAfter).as("Sender transactions count should not change").hasSize(senderTransactionsBefore.size());
        softly.assertThat(senderTransactionsAfter).as("No TRANSFER transactions should be created").noneMatch(transaction ->
                transaction.getType().equals("TRANSFER"));
        softly.assertThat(senderTransactionsAfter).as("Only DEPOSIT transactions should exist")
                .allMatch(transaction -> transaction.getType().equals("DEPOSIT"));

        AccountDao senderDao = DataBaseSteps.getAccountById(senderAccount.getId());

        softly.assertThat(senderDao).as("Счёт отправителя должен существовать в БД").isNotNull();
        softly.assertThat(BigDecimal.valueOf(senderDao.getBalance())).as("Баланс отправителя в БД не должен измениться").isEqualByComparingTo(depositAmount);

        AccountDao receiverDao = DataBaseSteps.getAccountById(NON_EXISTING_ACCOUNT_ID);

        softly.assertThat(receiverDao).as("Счёт получателя с ID %d не должен существовать в БД", NON_EXISTING_ACCOUNT_ID).isNull();

        DaoAndModelAssertions.assertThat(CreateAccountResponse.builder()
                                .id(senderAccount.getId())
                                .accountNumber(senderAccount.getAccountNumber())
                                .balance(depositAmount)
                                .build(), senderDao).match();
    }

    @Test
    public void userCanTransferWithNotCorrectSenderAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse receiverAccount = AccountSteps.createAccount(user);

        List<TransactionResponse> receiverTransactionsBefore = AccountSteps.getTransactions(user, receiverAccount.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId((int) NON_EXISTING_ACCOUNT_ID)
                .receiverAccountId(Math.toIntExact(receiverAccount.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsForbiddenWithText(AlertMessage.FORBIDDEN_WITH_TEXT.getMessage()))
                .post(transferRequest);

        List<TransactionResponse> receiverTransactionsAfter = AccountSteps.getTransactions(user, receiverAccount.getId());

        softly.assertThat(receiverTransactionsAfter).as("Receiver transactions count should not change").hasSize(receiverTransactionsBefore.size());
        softly.assertThat(receiverTransactionsAfter).as("No TRANSFER transactions should be created").noneMatch(tx -> tx.getType().equals("TRANSFER"));
        softly.assertThat(receiverTransactionsAfter).as("Only DEPOSIT transactions should exist").allMatch(tx -> tx.getType().equals("DEPOSIT"));

        AccountDao receiverDao = DataBaseSteps.getAccountById(receiverAccount.getId());

        softly.assertThat(receiverDao).as("Счёт получателя должен существовать в БД").isNotNull();
        softly.assertThat(BigDecimal.valueOf(receiverDao.getBalance())).as("Баланс получателя в БД не должен измениться").isEqualByComparingTo(BigDecimal.ZERO);

        AccountDao senderDao = DataBaseSteps.getAccountById(NON_EXISTING_ACCOUNT_ID);

        softly.assertThat(senderDao).as("Счёт отправителя с ID %d не должен существовать в БД", NON_EXISTING_ACCOUNT_ID).isNull();
    }

    @Test
    public void userCanTransferWithNotSenderAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse receiverAccount = AccountSteps.createAccount(user);

        List<TransactionResponse> receiverTransactionsBefore = AccountSteps.getTransactions(user, receiverAccount.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .receiverAccountId(Math.toIntExact(receiverAccount.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsForbiddenWithText(AlertMessage.FORBIDDEN_WITH_TEXT.getMessage()))
                .post(transferRequest);

        List<TransactionResponse> receiverTransactionsAfter =
                AccountSteps.getTransactions(user, receiverAccount.getId());

        softly.assertThat(receiverTransactionsAfter).as("Receiver transactions count should not change").hasSize(receiverTransactionsBefore.size());
        softly.assertThat(receiverTransactionsAfter).as("No TRANSFER transactions should be created").noneMatch(tx -> tx.getType().equals("TRANSFER"));
        softly.assertThat(receiverTransactionsAfter).as("Only DEPOSIT transactions should exist").allMatch(tx -> tx.getType().equals("DEPOSIT"));

        AccountDao receiverDao = DataBaseSteps.getAccountById(receiverAccount.getId());

        softly.assertThat(BigDecimal.valueOf(receiverDao.getBalance())).as("Баланс получателя в БД не должен измениться").isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void userCanTransferWithNotReceiverAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccount = AccountSteps.createAccount(user);

        DepositSteps.depositMoney(user, senderAccount.getId(), DEPOSIT_AMOUNT);

        List<TransactionResponse> senderTransactionsBefore = AccountSteps.getTransactions(user, senderAccount.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccount.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(
                        AlertMessage.BAD_REQUEST_WITH_TEXT.getMessage()))
                .post(transferRequest);

        List<TransactionResponse> senderTransactionsAfter = AccountSteps.getTransactions(user, senderAccount.getId());

        softly.assertThat(senderTransactionsAfter).as("Количество транзакций отправителя не должно измениться").hasSize(senderTransactionsBefore.size());
        softly.assertThat(senderTransactionsAfter).as("Не должно быть TRANSFER_OUT").noneMatch(t -> t.getType().equals("TRANSFER_OUT"));

        AccountDao senderDao = DataBaseSteps.getAccountById(senderAccount.getId());

        softly.assertThat(BigDecimal.valueOf(senderDao.getBalance())).as("Баланс отправителя в БД не должен измениться").isEqualByComparingTo(DEPOSIT_AMOUNT);

    }

    @Test
    public void userCanTransferWithNotAmount() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccount = AccountSteps.createAccount(user);
        CreateAccountResponse receiverAccount = AccountSteps.createAccount(user);

        DepositSteps.depositMoney(user, senderAccount.getId(), new BigDecimal("5000.00"));

        List<TransactionResponse> senderTransactionsBefore = AccountSteps.getTransactions(user, senderAccount.getId());
        List<TransactionResponse> receiverTransactionsBefore = AccountSteps.getTransactions(user, receiverAccount.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccount.getId()))
                .receiverAccountId(Math.toIntExact(receiverAccount.getId()))
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsInternalServerErrorWithText())
                .post(transferRequest);

        List<TransactionResponse> senderTransactionsAfter =
                AccountSteps.getTransactions(user, senderAccount.getId());
        List<TransactionResponse> receiverTransactionsAfter =
                AccountSteps.getTransactions(user, receiverAccount.getId());

        softly.assertThat(senderTransactionsAfter).as("Sender transactions count should not change").hasSize(senderTransactionsBefore.size());
        softly.assertThat(senderTransactionsAfter).as("No TRANSFER transactions should be created for sender").noneMatch(tx -> tx.getType().equals("TRANSFER"));
        softly.assertThat(receiverTransactionsAfter).as("Receiver transactions count should not change").hasSize(receiverTransactionsBefore.size());
        softly.assertThat(receiverTransactionsAfter).as("No TRANSFER transactions should be created for receiver").noneMatch(tx -> tx.getType().equals("TRANSFER"));

        AccountDao senderDao = DataBaseSteps.getAccountById(senderAccount.getId());
        AccountDao receiverDao = DataBaseSteps.getAccountById(receiverAccount.getId());

        softly.assertThat(BigDecimal.valueOf(senderDao.getBalance())).as("Баланс отправителя в БД не должен измениться").isEqualByComparingTo(DEPOSIT_AMOUNT);
        softly.assertThat(BigDecimal.valueOf(receiverDao.getBalance())).as("Баланс получателя в БД не должен измениться").isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    public void userCanTransferSenderAccountIdOnReceiverAccountId() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccountId = AccountSteps.createAccount(user);

        DepositSteps.depositMoney(user, senderAccountId.getId(), new BigDecimal("5000.00"));

        List<TransactionResponse> transactionsBefore = AccountSteps.getTransactions(user, senderAccountId.getId());

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccountId.getId()))
                .receiverAccountId(Math.toIntExact(senderAccountId.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(AlertMessage.BAD_REQUEST_WITH_TEXT.getMessage()))
                .post(transferRequest);

        List<TransactionResponse> transactionsAfter = AccountSteps.getTransactions(user, senderAccountId.getId());

        softly.assertThat(transactionsAfter).as("Transactions count should not change").hasSize(transactionsBefore.size());
        softly.assertThat(transactionsAfter).as("No TRANSFER transactions should be created").noneMatch(tx -> tx.getType().equals("TRANSFER"));
        softly.assertThat(transactionsAfter).as("Only DEPOSIT transactions should exist").allMatch(tx -> tx.getType().equals("DEPOSIT"));

        AccountDao accountDao = DataBaseSteps.getAccountById(senderAccountId.getId());

        softly.assertThat(BigDecimal.valueOf(accountDao.getBalance())).as("Баланс счёта в БД не должен измениться").isEqualByComparingTo(DEPOSIT_AMOUNT);
    }

    @Test
    public void userCanTransferNotAuthorization() {

        CreateUserRequest user = AdminSteps.createUser();
        CreateAccountResponse senderAccountId = AccountSteps.createAccount(user);
        CreateAccountResponse receiverAccountId = AccountSteps.createAccount(user);
        DepositSteps.depositMoney(user, senderAccountId.getId(), new BigDecimal("5000.00"));

        TransferMoneyRequest transferRequest = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccountId.getId()))
                .receiverAccountId(Math.toIntExact(senderAccountId.getId()))
                .amount(RandomData.getAmount())
                .build();

        new TransferMoneyRequester(RequestSpecs.unAuthSpec(), ResponseSpecs.requestReturnUnauthorized())
                .post(transferRequest);

        List<TransactionResponse> senderTransactions = AccountSteps.getTransactions(user, senderAccountId.getId());
        List<TransactionResponse> receiverTransactions = AccountSteps.getTransactions(user, receiverAccountId.getId());

        softly.assertThat(senderTransactions).as("Only DEPOSIT transactions should exist for sender").allMatch(tx -> tx.getType().equals("DEPOSIT"));
        softly.assertThat(senderTransactions).as("No TRANSFER transactions for sender").noneMatch(tx -> tx.getType().equals("TRANSFER"));
        softly.assertThat(receiverTransactions).as("Receiver should have no transactions").isEmpty();

        AccountDao senderDao = DataBaseSteps.getAccountById(senderAccountId.getId());
        AccountDao receiverDao = DataBaseSteps.getAccountById(receiverAccountId.getId());

        softly.assertThat(BigDecimal.valueOf(senderDao.getBalance())).as("Баланс отправителя в БД не должен измениться").isEqualByComparingTo(DEPOSIT_AMOUNT);
        softly.assertThat(BigDecimal.valueOf(receiverDao.getBalance())).as("Баланс получателя в БД не должен измениться").isEqualByComparingTo(BigDecimal.ZERO);
    }
}
