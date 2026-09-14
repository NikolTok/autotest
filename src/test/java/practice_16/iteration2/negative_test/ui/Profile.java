package practice_16.iteration2.negative_test.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import api.requests.skelethon.Endpoint;
import api.requests.skelethon.requesters.CrudRequester;
import api.requests.steps.AdminSteps;
import api.spec.RequestSpecs;
import api.spec.ResponseSpecs;
import ui.pages.UserDashboard;

import java.util.Map;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

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
        dashboard.verifyProfileName("Noname");
    }
}
