package practice_16.iteration2.negative_test.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

public class Profile {

    @BeforeAll
    public static void setupSelenoid() {
        Configuration.remote = "http://localhost:4444/wd/hub\n";
        Configuration.baseUrl = "http://172.31.80.1:3000";
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";

        Configuration.browserCapabilities.setCapability("selenoid:options",
                Map.of("enableVNC", true, "enableLog", true)
        );
    }

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

        String userAuthHeader = new CrudRequester(
                RequestSpecs.unAuthSpec(),
                Endpoint.LOGIN,
                ResponseSpecs.requestReturnsOK())
                .post(LoginUserRequest.builder().username(user.getUsername()).password(user.getPassword()).build())
                .extract()
                .header("Authorization");

        Selenide.open("/");
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);

        Selenide.open("/dashboard");

        $(Selectors.byText("Noname")).click();

        $(Selectors.byAttribute("placeholder", "Enter new name")).setValue(name);
        $(Selectors.byText("\uD83D\uDCBE Save Changes")).click();

        Alert errorAlert = switchTo().alert();
        String actualErrorMessage = errorAlert.getText();
        assertThat(actualErrorMessage).as("Ошибка", expectedMessage).contains(expectedMessage);
        errorAlert.accept();
    }
}
