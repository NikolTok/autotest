package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

import static io.restassured.RestAssured.given;

public class GetAccountTransactionsRequester extends Request {

    public GetAccountTransactionsRequester(
            RequestSpecification requestSpecification,
            ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        throw new UnsupportedOperationException("GET request does not support POST");
    }

    public ValidatableResponse get(int accountId) {
        return given()
                .spec(requestSpecification)
                .pathParam("accountId", accountId)
                .get("/accounts/{accountId}/transactions")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse put(BaseModel model) {
        return null;
    }
}
