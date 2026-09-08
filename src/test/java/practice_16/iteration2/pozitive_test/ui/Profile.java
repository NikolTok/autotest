package practice_16.iteration2.pozitive_test.ui;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import models.CreateUserRequest;
import models.LoginUserRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.*;
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

        String profileAlertText = switchTo().alert().getText();

        String expectedAlertText = String.format(Locale.US, "✅ Name updated successfully!");
        assertThat(profileAlertText).isEqualTo(expectedAlertText);
        switchTo().alert().accept();
    }
}
