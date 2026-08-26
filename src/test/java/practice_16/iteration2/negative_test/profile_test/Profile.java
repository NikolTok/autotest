package practice_16.iteration2.negative_test.profile_test;

import generators.RandomData;
import models.BaseModel;
import models.CreateUserRequest;
import models.UpdateProfileRequest;
import models.UserRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.AdminCreateUserRequester;
import requests.UpdateProfileRequester;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.util.stream.Stream;

public class Profile extends BaseModel {

    public static Stream<Arguments> profileWithNotCorrectDate() {
        return Stream.of(
                Arguments.of("Kolya Tokarev Aleksandrovich", "Name must contain two words with letters only"),
                Arguments.of("Kolya1 Tokarev", "Name must contain two words with letters only"),
                Arguments.of("Kolya 123", "Name must contain two words with letters only"),
                Arguments.of("123 Tokarev", "Name must contain two words with letters only"),
                Arguments.of("123 456", "Name must contain two words with letters only"),
                Arguments.of("Kolya  Tokarev", "Name must contain two words with letters only"),
                Arguments.of("Kolya", "Name must contain two words with letters only"),
                Arguments.of("Kolya Tokarev!", "Name must contain two words with letters only"),
                Arguments.of(" Kolya Tokarev", "Name must contain two words with letters only"),
                Arguments.of("Kolya Tokarev ", "Name must contain two words with letters only"),
                Arguments.of(" ", "Name must contain two words with letters only"),
                Arguments.of("", "Name must contain two words with letters only")
        );
    }

    @MethodSource("profileWithNotCorrectDate")
    @ParameterizedTest
    public void updateNameWithNotCorrectDate(String name, String expectedMessage) {

        CreateUserRequest userRequest = CreateUserRequest.builder()
                .username(RandomData.getUsername())
                .password(RandomData.getPassword())
                .role(UserRole.USER.toString())
                .build();

        new AdminCreateUserRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated())
                .post(userRequest);

        UpdateProfileRequest profileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        new UpdateProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(expectedMessage))
                .put(profileRequest);
    }
}
