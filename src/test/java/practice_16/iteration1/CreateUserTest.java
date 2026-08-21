package practice_16.iteration1;

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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

public class CreateUserTest {
    @BeforeAll
    public static void setupRestAssured() {
        RestAssured.filters(
                List.of(new RequestLoggingFilter(),
                        new ResponseLoggingFilter())
        );
    }

    @Test
    public void adminCanCreateUserWithCorrectDate() {
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body("""
                        {
                        "username": "Test2027",
                        "password": "Kate2000#",
                        "role": "USER"
                        }
                        """)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("username", Matchers.equalTo("Test2027"))
                .body("password", Matchers.not(Matchers.equalTo("Kate2000#")))
                .body("role", Matchers.equalTo("USER"));
    }

    public static Stream<Arguments> userInvalidDate() {
        return Stream.of(
                Arguments.of("   ", "Password33$", "USER", "username", Arrays.asList("Username cannot be blank", "Username must contain only letters, digits, dashes, underscores, and dots")),
                Arguments.of("ab", "Password33$", "USER", "username", Arrays.asList("Username must be between 3 and 15 characters")),
                Arguments.of("abc$", "Password33$", "USER", "username", Arrays.asList("Username must contain only letters, digits, dashes, underscores, and dots"))
        );
    }

    @MethodSource("userInvalidDate")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidDate(String username, String password, String role,
                                                     String errorKey, List<String> expectedErrors) {
        String requestBody = String.format(
                """
                        {
                        "username": "%s",
                        "password": "%s",
                        "role": "%s"
                        }
                        """, username, password, role);
        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Basic YWRtaW46YWRtaW4=")
                .body(requestBody)
                .post("http://localhost:4111/api/v1/admin/users")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(errorKey, Matchers.hasItems(expectedErrors.toArray(new String[0])));
    }
}
