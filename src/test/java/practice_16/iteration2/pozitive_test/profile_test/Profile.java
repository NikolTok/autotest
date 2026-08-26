package practice_16.iteration2.pozitive_test.profile_test;

import generators.RandomData;
import models.CreateUserRequest;
import models.UpdateProfileRequest;
import models.UpdateProfileResponse;
import models.UserRole;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import requests.AdminCreateUserRequester;
import requests.UpdateProfileRequester;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.util.stream.Stream;

public class Profile extends BaseTest {

    public static Stream<Arguments> profileWithCorrectDate() {
        return Stream.of(
                Arguments.of("Kolya Tokarev"),
                Arguments.of("K T")
        );
    }

    @MethodSource("profileWithCorrectDate")
    @ParameterizedTest
    public void updateNameWithCorrectDate(String name) {

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

        UpdateProfileResponse response = new UpdateProfileRequester(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .put(profileRequest)
                .extract()
                .as(UpdateProfileResponse.class);

        softly.assertThat(response.getCustomer().getName()).isEqualTo(name);
        softly.assertThat(response.getMessage()).isEqualTo("Profile updated successfully");
        softly.assertThat(response.getCustomer()).isNotNull();
    }
}
