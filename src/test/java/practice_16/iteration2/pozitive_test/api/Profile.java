package practice_16.iteration2.pozitive_test.api;

import api.dao.UserDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.CreateUserRequest;
import api.models.UpdateProfileRequest;
import api.models.UpdateProfileResponse;
import api.models.comparison.ModelAssertions;
import api.requests.steps.DataBaseSteps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import practice_16.iteration2.BaseTest;
import api.requests.UpdateProfileRequester;
import api.requests.steps.AdminSteps;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;

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

        UserDao userDao = DataBaseSteps.getUserByUsername(user.getUsername());

        softly.assertThat(userDao).as("Пользователь должен существовать в БД").isNotNull();
        softly.assertThat(userDao.getName()).as("Имя пользователя в БД должно соответствовать новому имени").isEqualTo(name);
        softly.assertThat(userDao.getUsername()).as("Username не должен измениться").isEqualTo(user.getUsername());
        softly.assertThat(userDao.getRole()).as("Роль не должна измениться").isEqualTo(user.getRole());

        if (response.getCustomer() != null) {
            DaoAndModelAssertions.assertThat(response.getCustomer(), userDao).match();
        }
    }
}
