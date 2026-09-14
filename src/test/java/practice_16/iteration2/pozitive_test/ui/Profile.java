package practice_16.iteration2.pozitive_test.ui;

import api.models.CreateUserRequest;
import api.requests.steps.AdminSteps;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.UserDashboard;

import java.util.stream.Stream;

public class Profile extends BaseUiTest{

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
        authAsUser(user.getUsername(), user.getPassword());

        UserDashboard dashboard = new UserDashboard().open();

        dashboard
                .openProfile()
                .enterNewName(name)
                .saveChanges();

        dashboard.checkAlertMessageAndAccept(BankAlert.NAME_UPDATED_SUCCESSFULLY.getMessage());
        Selenide.refresh();
        dashboard.verifyProfileName(name);
    }
}
