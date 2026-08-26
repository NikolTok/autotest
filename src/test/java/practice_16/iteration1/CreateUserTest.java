package practice_16.iteration1;

import generators.RandomData;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import requests.AdminCreateUserRequester;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {

    @Test
    public void adminCanCreateUserWithCorrectDate() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        CreateUserResponse createUserResponse = new AdminCreateUserRequester(RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsOK())
                .post(createUserRequest)
                .extract().as(CreateUserResponse.class);

        softly.assertThat(createUserResponse.getUsername()).isEqualTo(createUserResponse.getUsername());
        softly.assertThat(createUserResponse.getPassword()).isNotEqualTo(createUserResponse.getPassword());
        softly.assertThat(createUserResponse.getRole()).isEqualTo(createUserResponse.getRole());

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
                                                     String errorKey, String errorValue, List<String> expectedErrors) {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue))
                .post(createUserRequest);
    }
}
