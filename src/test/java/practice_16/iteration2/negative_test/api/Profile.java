package practice_16.iteration2.negative_test.api;

import api.models.BaseModel;
import api.models.CreateUserRequest;
import api.models.UpdateProfileRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import api.requests.UpdateProfileRequester;
import api.requests.steps.AdminSteps;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;

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

        CreateUserRequest user = AdminSteps.createUser();

        UpdateProfileRequest profileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        new UpdateProfileRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(expectedMessage))
                .put(profileRequest);
    }
}
