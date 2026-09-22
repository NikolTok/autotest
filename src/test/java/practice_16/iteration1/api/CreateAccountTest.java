package practice_16.iteration1.api;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.CreateAccountResponse;
import api.models.CreateUserRequest;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;
import org.junit.jupiter.api.Test;

public class CreateAccountTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminSteps.createUser();

        CreateAccountResponse createAccountResponse = new ValidatedCrudRequester<CreateAccountResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(createAccountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(createAccountResponse, accountDao).match();
    }
}
