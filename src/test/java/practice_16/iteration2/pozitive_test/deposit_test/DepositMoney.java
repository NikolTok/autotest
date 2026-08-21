package practice_16.iteration2.pozitive_test.deposit_test;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
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

    public static Stream<Arguments> depositWithCorrectDate() {
        return Stream.of(
                Arguments.of(1, 0.01),
                Arguments.of(1, 4999.99),
                Arguments.of(1, 5000.00)
        );
    }

    @MethodSource("depositWithCorrectDate")
    @ParameterizedTest
    public void userCanDepositWithCorrectDate(int id, double balance) {
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
                .statusCode(HttpStatus.SC_OK);
    }
}
