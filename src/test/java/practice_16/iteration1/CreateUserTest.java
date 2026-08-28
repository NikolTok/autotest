package practice_16.iteration1;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.skelethon.requesters.ValidatedCrudRequester;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {

    @Test
    public void adminCanCreateUserWithCorrectDate() {
        CreateUserRequest createUserRequest =
                RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>(RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.entityWasCreated())
                .post(createUserRequest);

        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();
    }

    public static Stream<Arguments> userInvalidDate() {
        return Stream.of(
                Arguments.of("   ", "Password33$", "USER", "username", Arrays.asList("Username must contain only letters, digits, dashes, underscores, and dots", "Username cannot be blank")),
                Arguments.of("ab", "Password33$", "USER", "username", Arrays.asList("Username must be between 3 and 15 characters")),
                Arguments.of("abc$", "Password33$", "USER", "username", Arrays.asList("Username must contain only letters, digits, dashes, underscores, and dots"))
        );
    }

    @MethodSource("userInvalidDate")
    @ParameterizedTest
    public void adminCanNotCreateUserWithInvalidDate(String username, String password, String role, String errorKey, List<String> expectedErrors) {

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                Endpoint.ADMIN_USER,
                ResponseSpecs.requestReturnsBadRequest(errorKey, expectedErrors))
                .post(createUserRequest);
    }
}
