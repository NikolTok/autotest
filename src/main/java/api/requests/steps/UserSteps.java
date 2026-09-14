package api.requests.steps;

import api.models.CreateAccountResponse;
import api.models.TransactionResponse;
import api.requests.GetAccountTransactionsRequester;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;
import io.restassured.common.mapper.TypeRef;

import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public  List<CreateAccountResponse> getAllAccounts() {
        return new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(username, password),
                Endpoint.CUSTOMER_ACCOUNTS,
                ResponseSpecs.requestReturnsOK()).getAll(CreateAccountResponse[].class);
    }

    public List<TransactionResponse> getAccountTransactions(int accountId) {
        return new GetAccountTransactionsRequester(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOK())
                .get(accountId)
                .extract()
                .as(new TypeRef<List<TransactionResponse>>() {});
    }
}
