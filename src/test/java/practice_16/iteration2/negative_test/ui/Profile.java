package practice_16.iteration2.negative_test.ui;

import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import com.codeborne.selenide.Selenide;
import common.data.TestData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.UserDashboard;

import java.util.stream.Stream;

public class Profile extends BaseUiTest{

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
                Arguments.of(" ", "❌ Please enter a valid name."),
                Arguments.of("", "❌ Please enter a valid name.")
        );
    }

    @MethodSource("profileWithNotCorrectDate")
    @ParameterizedTest
    public void updateNameWithNotCorrectDate(String name, String expectedMessage) {

        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user.getUsername(), user.getPassword());

        UserDashboard dashboard = new UserDashboard().open();

        dashboard
                .openProfile()
                .enterNewName(name)
                .saveChanges();

        dashboard.checkAlertMessageAndAccept(expectedMessage);
        Selenide.refresh();
        dashboard.verifyProfileName(TestData.RECIPIENT_NAME.getString());
    }
}
