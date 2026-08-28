package requests.assertions;

import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.TransactionResponse;
import models.TransferMoneyResponse;
import org.assertj.core.api.Assertions;
import requests.steps.AccountSteps;

import java.math.BigDecimal;
import java.util.List;

public class TransferAssertions {

    public static void assertSuccessfulTransfer(
            CreateUserRequest user,
            CreateAccountResponse sender,
            CreateAccountResponse receiver,
            BigDecimal amount,
            TransferMoneyResponse response) {

        Assertions.assertThat(response.getMessage()).isEqualTo("Transfer successful");

        List<TransactionResponse> senderTransactions = AccountSteps.getTransactions(user, sender.getId());

        List<TransactionResponse> receiverTransactions = AccountSteps.getTransactions(user, receiver.getId());

        TransactionResponse senderTransfer = findTransfer(senderTransactions, amount, receiver.getId());

        TransactionResponse receiverTransfer = findTransfer(receiverTransactions, amount, sender.getId());

        Assertions.assertThat(senderTransfer.getAmount()).isEqualByComparingTo(amount);
        Assertions.assertThat(senderTransfer.getType()).isEqualTo("TRANSFER_OUT");
        Assertions.assertThat(senderTransfer.getRelatedAccountId()).isEqualTo(receiver.getId());
        Assertions.assertThat(receiverTransfer.getAmount()).isEqualByComparingTo(amount);
        Assertions.assertThat(receiverTransfer.getType()).isEqualTo("TRANSFER_IN");
        Assertions.assertThat(receiverTransfer.getRelatedAccountId()).isEqualTo(sender.getId());
    }

    private static TransactionResponse findTransfer(List<TransactionResponse> transactions, BigDecimal amount, long relatedAccountId) {

        return transactions.stream()
                .filter(transaction -> transaction.getAmount().compareTo(amount) == 0)
                .filter(transaction -> transaction.getRelatedAccountId() == relatedAccountId)
                .findFirst()
                .orElseThrow(() -> new AssertionError("Transfer transaction was not found"));
    }
}