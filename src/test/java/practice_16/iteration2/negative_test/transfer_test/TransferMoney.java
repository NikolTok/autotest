package practice_16.iteration2.negative_test.transfer_test;

import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

    public static Stream<Arguments> transferNotCorrectDate() {
        return Stream.of(
                Arguments.of(1, 2, 0.00),
                Arguments.of(1, 2, -0.01),
                Arguments.of(1, 3, 100000000.00),
                Arguments.of(1, 2, 10000.01)
        );
    }

    @MethodSource("transferNotCorrectDate")
    @ParameterizedTest
    public void userCanTransferWithNotCorrectDate(int senderAccountId, int receiverAccountId, double amount) {
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
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanTransferWithNotCorrectReceiverAccountId() {
        String requestBody =
                """
                        {
                        "senderAccountId": 1,
                        "receiverAccountId": 999999,
                        "amount": 1000.01
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanTransferWithNotCorrectSenderAccountId() {
        String requestBody =
                """
                        {
                        "senderAccountId": 999999,
                        "receiverAccountId": 1,
                        "amount": 1000.00
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void userCanTransferWithNotSenderAccountId() {
        String requestBody =
                """
                        {
                        "receiverAccountId": 1,
                        "amount": 1000.00
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCanTransferWithNotReceiverAccountId() {
        String requestBody =
                """
                        {
                        "senderAccountId": 1,
                        "amount": 1000.50
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCanTransferWithNotAmount() {
        String requestBody =
                """
                        {
                        "senderAccountId": 1,
                        "receiverAccountId": 2,
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCanTransferWithNotCorrectTypSenderAccountId() {
        String requestBody =
                """
                        {
                        "senderAccountId": "hello",
                        "receiverAccountId": 1,
                        "amount": 1000.00
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCanTransferWithNotCorrectTypReceiverAccountId() {
        String requestBody =
                """
                        {
                        "senderAccountId": 1,
                        "receiverAccountId": "hello",
                        "amount": 1000.00
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCanTransferWithNotCorrectTypAmount() {
        String requestBody =
                """
                        {
                        "senderAccountId": 1,
                        "receiverAccountId": 1,
                        "amount": "hello"
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCanTransferSenderAccountIdOnReceiverAccountId() {
        String requestBody =
                """
                        {
                        "senderAccountId": 1,
                        "receiverAccountId": 1,
                        "amount": 123.12
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void userCanTransferNotAuthorization() {
        String requestBody =
                """
                        {
                        "senderAccountId": 1,
                        "receiverAccountId": 2,
                        "amount": 123.12
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(requestBody)
                .post("http://localhost:4111/api/v1/accounts/transfer")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}
