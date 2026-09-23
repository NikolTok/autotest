package api.requests.steps;

import api.models.CreateAccountResponse;
import api.models.TransactionResponse;
import api.models.TransferMoneyRequest;
import api.models.TransferMoneyResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;

import java.math.BigDecimal;
import java.util.List;

public class AccountSteps {

    private final String username;
    private final String password;

    public AccountSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public CreateAccountResponse createAccount() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);
    }

    public List<TransactionResponse> getTransactions(Long accountId) {
        return new ValidatedCrudRequester<TransactionResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK())
                .getAll(TransactionResponse[].class);
    }

    public TransferMoneyResponse transferWithFraudCheck(
            Long senderAccountId,
            Long receiverAccountId,
            BigDecimal amount
    ) {
        TransferMoneyRequest request = TransferMoneyRequest.builder()
                .senderAccountId(Math.toIntExact(senderAccountId))
                .receiverAccountId(Math.toIntExact(receiverAccountId))
                .amount(amount)
                .build();

        return new ValidatedCrudRequester<TransferMoneyResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.TRANSFER_WITH_FRAUD_CHECK,
                ResponseSpecs.requestReturnsOK())
                .post(request);
    }
}