package requests;

import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.BaseModel;

import static io.restassured.RestAssured.given;

public class GetAccountRequester extends Request {

    public GetAccountRequester(
            RequestSpecification requestSpecification,
            ResponseSpecification responseSpecification) {
        super(requestSpecification, responseSpecification);
    }

    @Override
    public ValidatableResponse post(BaseModel model) {
        throw new UnsupportedOperationException(
                "GET request does not support POST");
    }

    public ValidatableResponse get(int accountId) {
        return given()
                .spec(requestSpecification)
                .pathParam("accountId", accountId)
                .get("/accounts/{accountId}")
                .then()
                .assertThat()
                .spec(responseSpecification);
    }

    @Override
    public ValidatableResponse put(BaseModel model) {
        throw new UnsupportedOperationException(
                "GET request does not support PUT");
    }
}