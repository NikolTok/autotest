package practice_16.iteration1.api.helpers;

import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.models.DepositMoneyResponse;
import api.models.TransferMoneyResponse;
import api.requests.steps.AccountSteps;
import api.requests.steps.AdminSteps;
import api.requests.steps.DepositSteps;

import java.math.BigDecimal;

public class FraudTestHelper {

    public static final BigDecimal DEFAULT_DEPOSIT = new BigDecimal("5000.00");
    public static final BigDecimal DEFAULT_TRANSFER = new BigDecimal("100.00");

    public record TestContext(
            CreateUserRequest user1,
            CreateUserRequest user2,
            CreateAccountResponse senderAccount,
            CreateAccountResponse receiverAccount,
            DepositMoneyResponse depositResponse
    ) {}

    public static practice_16.iteration1.api.helpers.FraudTestHelper.TestContext prepare() {
        CreateUserRequest user1 = AdminSteps.createUser();
        CreateUserRequest user2 = AdminSteps.createUser();

        AccountSteps senderSteps = new AccountSteps(user1.getUsername(), user1.getPassword());
        CreateAccountResponse senderAccount = senderSteps.createAccount();

        DepositMoneyResponse deposit = DepositSteps.depositMoney(
                user1,
                senderAccount.getId(),
                DEFAULT_DEPOSIT
        );

        // ===== receiver =====
        AccountSteps receiverSteps = new AccountSteps(user2.getUsername(), user2.getPassword());
        CreateAccountResponse receiverAccount = receiverSteps.createAccount();

        return new practice_16.iteration1.api.helpers.FraudTestHelper.TestContext(user1, user2, senderAccount, receiverAccount, deposit);
    }

    public static TransferMoneyResponse transfer(practice_16.iteration1.api.helpers.FraudTestHelper.TestContext ctx, BigDecimal amount) {
        AccountSteps senderSteps = new AccountSteps(
                ctx.user1().getUsername(),
                ctx.user1().getPassword()
        );
        return senderSteps.transferWithFraudCheck(
                ctx.senderAccount().getId(),
                ctx.receiverAccount().getId(),
                amount
        );
    }
}
