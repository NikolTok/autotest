package api.requests.steps;

import api.models.CreateUserRequest;
import api.models.DepositMoneyRequest;
import api.models.DepositMoneyResponse;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.ValidatedCrudRequester;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;

import java.math.BigDecimal;

public class DepositSteps {
    public static DepositMoneyResponse depositMoney(CreateUserRequest user, long accountId, BigDecimal amount) {

        DepositMoneyRequest request = DepositMoneyRequest.builder()
                .id(Math.toIntExact(accountId))
                .balance(amount)
                .build();

        return new ValidatedCrudRequester<DepositMoneyResponse>(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                Endpoint.DEPOSIT,
                ResponseSpecs.requestReturnsOK())
                .post(request);
    }
}