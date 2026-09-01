package practice_16.iteration2.negative_test.deposit_test;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class DepositMoney {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter())
        );
    }

    public static Stream<Arguments> depositNotCorrectDate() {
        return Stream.of(
                Arguments.of(1, 0.00),
                Arguments.of(1, -0.01),
                Arguments.of(1, 5000.01)
        );
    }

    @MethodSource("depositNotCorrectDate")
    @ParameterizedTest
    public void userCanDepositNotCorrectDate(int id, double balance) {

        double balanceBefore = given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/{accountId}/transactions")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("balance");

        String requestBody = String.format(
                Locale.US,
                """
                        {
                        "id": %d,
                        "balance": %.2f
                        }
                        """,id, balance);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/{accountId}/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(balanceBefore));
    }

    @Test
    public void userCanDepositNotAccountDate() {

        double balanceBefore = given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/{accountId}/transactions")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("balance");

        String requestBody =
                """
                        {
                        "id": 999999,
                        "balance": 100.00
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/{accountId}/transactions")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("balance", Matchers.equalTo(balanceBefore));
    }

    @Test
    public void userCanDepositNotIdDate() {
        String requestBody =
                """
                        {
                        "balance": 100.00
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCanDepositNotBalanceDate() {
        String requestBody =
                """
                        {
                        "id": 1,
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanDepositNotTypBalance() {
        String requestBody =
                """
                        {
                        "id": 999999,
                        "balance": "hello"
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanDepositNotTypId() {
        String requestBody =
                """
                        {
                        "id": "hello",
                        "balance": 100.00
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanDepositNotAuthorization() {
        String requestBody =
                """
                        {
                        "id": "hello",
                        "balance": 100.00
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/deposit")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}
