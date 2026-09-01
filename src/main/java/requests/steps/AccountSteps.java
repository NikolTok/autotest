package requests.steps;

import models.AccountResponse;
import models.CreateAccountResponse;
import models.CreateUserRequest;
import models.TransactionResponse;
import requests.GetAccountRequester;
import requests.GetAccountTransactionsRequester;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import spec.RequestSpecs;
import spec.ResponseSpecs;

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
