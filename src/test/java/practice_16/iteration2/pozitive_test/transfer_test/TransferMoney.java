package practice_16.iteration2.pozitive_test.transfer_test;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class TransferMoney {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter())
        );
    }

    public static Stream<Arguments> transferWithCorrectDate() {
        return Stream.of(
                Arguments.of(1, 2, 0.01),
                Arguments.of(3, 1, 9999.99),
                Arguments.of(1, 2, 10000.00)
        );
    }

    @MethodSource("transferWithCorrectDate")
    @ParameterizedTest
    public void userCanTransferWithCorrectDate(int senderAccountId, int receiverAccountId, double amount) {
        String requestBody = String.format(
                Locale.US,
                """
                        {
                        "senderAccountId": %d,
                        "receiverAccountId": %d,
                        "amount": %.2f
                        }
                        """, senderAccountId, receiverAccountId, amount);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }
}
