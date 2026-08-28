package requests.steps;

import models.CreateUserRequest;
import models.DepositMoneyRequest;
import models.DepositMoneyResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.ValidatedCrudRequester;
import spec.RequestSpecs;
import spec.ResponseSpecs;

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