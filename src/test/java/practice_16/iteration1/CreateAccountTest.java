package practice_16.iteration1;

import models.CreateUserRequest;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import spec.RequestSpecs;
import spec.ResponseSpecs;

public class CreateAccountTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest user = AdminSteps.createUser();

        new CrudRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.ACCOUNTS,
                ResponseSpecs.entityWasCreated())
                .post(null);
    }
}
