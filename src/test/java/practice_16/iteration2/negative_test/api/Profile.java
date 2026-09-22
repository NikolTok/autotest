package practice_16.iteration2.negative_test.api;

import api.dao.UserDao;
import api.models.BaseModel;
import api.models.CreateUserRequest;
import api.models.UpdateProfileRequest;
import api.requests.UpdateProfileRequester;
import api.requests.steps.AdminSteps;
import api.requests.steps.DataBaseSteps;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
        SoftAssertions softly = new SoftAssertions();

        CreateUserRequest user = AdminSteps.createUser();

        UserDao userBefore = DataBaseSteps.getUserByUsername(user.getUsername());

        softly.assertThat(userBefore).as("Пользователь должен существовать в БД до обновления").isNotNull();

        String nameBefore = userBefore.getName();

        UpdateProfileRequest profileRequest = UpdateProfileRequest.builder().name(name).build();

        new UpdateProfileRequester(
                RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsBadRequestWithText(expectedMessage))
                .put(profileRequest);

        UserDao userAfter = DataBaseSteps.getUserByUsername(user.getUsername());

        softly.assertThat(userAfter).as("Пользователь должен существовать в БД после неудачного обновления").isNotNull();
        softly.assertThat(userAfter.getName()).as("Имя пользователя в БД не должно измениться после неудачного обновления").isEqualTo(nameBefore);
        softly.assertThat(userAfter.getUsername()).as("Username не должен измениться").isEqualTo(userBefore.getUsername());
        softly.assertThat(userAfter.getRole()).as("Role не должна измениться").isEqualTo(userBefore.getRole());
        softly.assertThat(userAfter.getId()).as("ID не должен измениться").isEqualTo(userBefore.getId());
    }
}
