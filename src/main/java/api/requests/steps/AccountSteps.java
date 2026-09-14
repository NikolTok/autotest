package api.requests.steps;

import api.models.*;
import api.requests.GetAccountRequester;
import api.requests.GetAccountTransactionsRequester;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;

import java.util.List;

public class AccountSteps {

    public static CreateAccountResponse createAccount(CreateUserRequest user) {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);
    }

    public static AccountResponse getAccount(CreateUserRequest user, long accountId) {
        return new GetAccountRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(Math.toIntExact(accountId))
                .extract()
                .as(AccountResponse.class);
    }

    public static List<TransactionResponse> getTransactions(
            CreateUserRequest user,
            long accountId) {

        return new GetAccountTransactionsRequester(
                RequestSpecs.authAsUser(
                        user.getUsername(),
                        user.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(Math.toIntExact(accountId))
                .extract()
                .jsonPath()
                .getList(".", TransactionResponse.class);
    }
}
