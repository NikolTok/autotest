package spec;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;

import java.util.List;

public class ResponseSpecs {
    private ResponseSpecs(){}

    private static ResponseSpecBuilder defaultResponseBuilder() {
        return new ResponseSpecBuilder();
    }

    public static ResponseSpecification entityWasCreated() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_CREATED)
                .build();
    }

    public static ResponseSpecification requestReturnsOK() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequest(
            String errorKey,
            List<String> expectedErrors) {

        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(errorKey, Matchers.equalTo(expectedErrors))
                .build();
    }

    public static ResponseSpecification requestReturnsBadRequestWithText(String expectedMessage) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_BAD_REQUEST)
                .expectBody(Matchers.equalTo(expectedMessage))
                .build();
    }

    public static ResponseSpecification requestReturnsForbiddenWithText(String expectedMessage) {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_FORBIDDEN)
                .expectBody(Matchers.equalTo(expectedMessage))
                .build();
    }

    public static ResponseSpecification requestReturnsInternalServerErrorWithText() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR)
                .expectBody("status", Matchers.equalTo(500))
                .expectBody("error", Matchers.equalTo("Internal Server Error"))
                .build();
    }

    public static ResponseSpecification requestReturnUnauthorized() {
        return defaultResponseBuilder()
                .expectStatusCode(HttpStatus.SC_UNAUTHORIZED)
                .build();
    }
}
