package practice_16.iteration2.negative_test.profile_test;

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
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class Profile {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter())
        );
    }

    public static Stream<Arguments> profileWithNotCorrectDate() {
        return Stream.of(
                Arguments.of("Kolya Tokarev Aleksandrovich"),
                Arguments.of("Kolya1 Tokarev"),
                Arguments.of("Kolya 123"),
                Arguments.of("123 Tokarev"),
                Arguments.of("123 456"),
                Arguments.of("Kolya  Tokarev"),
                Arguments.of("Kolya"),
                Arguments.of("Kolya Tokarev!"),
                Arguments.of(" Kolya Tokarev"),
                Arguments.of("Kolya Tokarev "),
                Arguments.of(" "),
                Arguments.of("")
        );
    }

    @MethodSource("profileWithNotCorrectDate")
    @ParameterizedTest
    public void updateNameWithNotCorrectDate(String name) {

        String nameBefore = given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("name");

        String requestBody = String.format(
                """
                        {
                          "name": "%s"
                        }
                        """, name);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.equalTo(nameBefore));
    }

    @Test
    public void updateNameWithNotCorrectTypNull() {

        String nameBefore = given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("name");

        String requestBody =
                """
                        {
                          "name": null
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.equalTo(nameBefore));
    }

    @Test
    public void updateNameWithNotCorrectTypInt() {

        String nameBefore = given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("name");

        String requestBody =
                """
                        {
                          "name": 123
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.equalTo(nameBefore));
    }

    @Test
    public void updateNameWithEmptyName() {

        String nameBefore = given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .path("name");

        String requestBody =
                """
                        {
                          "name":
                        }
                        """;
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .body(requestBody)
                .put("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);

        given()
                .header("Authorization", "Basic VGVzdDIwMjc6S2F0ZTIwMDAj")
                .accept(ContentType.JSON)
                .get("http://localhost:4111/api/v1/customer/profile")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("name", Matchers.equalTo(nameBefore));
    }
}
