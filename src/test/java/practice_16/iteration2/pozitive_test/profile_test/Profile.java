package practice_16.iteration2.pozitive_test.profile_test;

import models.CreateUserRequest;
import models.UpdateProfileRequest;
import models.UpdateProfileResponse;
import models.comparison.ModelAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import requests.UpdateProfileRequester;
import requests.steps.AdminSteps;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.util.stream.Stream;

public class Profile extends BaseTest {

    public static Stream<Arguments> updateNameWithCorrectData() {
        return Stream.of(
                Arguments.of("Kolya Tokarev"),
                Arguments.of("K T")
        );
    }

    @MethodSource("updateNameWithCorrectData")
    @ParameterizedTest
    public void updateNameWithCorrectDate(String name) {

        CreateUserRequest user = AdminSteps.createUser();

        UpdateProfileRequest profileRequest = UpdateProfileRequest.builder()
                .name(name)
                .build();

        UpdateProfileResponse response = new UpdateProfileRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .put(profileRequest)
                .extract()
                .as(UpdateProfileResponse.class);

        ModelAssertions.assertThatModels(profileRequest, response).match();
        softly.assertThat(response.getCustomer()).isNotNull();
        softly.assertThat(response.getMessage()).isEqualTo("Profile updated successfully");
    }
}
