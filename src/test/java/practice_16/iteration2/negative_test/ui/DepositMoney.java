package practice_16.iteration2.negative_test.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import io.restassured.common.mapper.TypeRef;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.TransactionResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import requests.GetAccountTransactionsRequester;
import requests.skelethon.Endpoint;
import requests.skelethon.requesters.CrudRequester;
import requests.steps.AdminSteps;
import spec.RequestSpecs;
import spec.ResponseSpecs;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DepositMoney {

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

    public static Stream<Arguments> depositNotCorrectDate() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.00"),"❌ Please enter a valid amount."),
                Arguments.of(new BigDecimal("-0.01"), "❌ Please enter a valid amount."),
                Arguments.of(new BigDecimal("5000.01"), "❌ Please deposit less or equal to 5000$.")
        );
    }

    @MethodSource("depositNotCorrectDate")
    @ParameterizedTest
    public void userCannotDepositInvalidAmount(BigDecimal balance, String expectedMessage) {

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
        $(Selectors.byText("➕ Create New Account")).click();

        Alert accountAlert  = switchTo().alert();
        String accountAlertText = accountAlert .getText();
        assertThat(accountAlertText).contains("✅ New Account Created! Account Number:");
        accountAlert .accept();

        String accountNumber = extractAccountNumber(accountAlertText);

        $(Selectors.byText("💰 Deposit Money")).click();

        $("select.form-control.account-selector").shouldBe(Condition.visible).selectOptionContainingText(accountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(balance.toString());
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert errorAlert = switchTo().alert();
        String actualErrorMessage = errorAlert.getText();
        assertThat(actualErrorMessage).as("Для суммы %s ожидалось сообщение: %s ", balance, expectedMessage).contains(expectedMessage);
        errorAlert.accept();

        int accountId = extractAccountId(accountNumber);

        List<TransactionResponse> transactions = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(accountId)
                .extract()
                .as(new TypeRef<List<TransactionResponse>>() {});

        assertThat(transactions).as("Транзакций не должно быть после неудачного депозита на сумму %s ", balance).isEmpty();
    }

    @Test
    public void userCannotDepositInvalidWords() {

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
        $(Selectors.byText("➕ Create New Account")).click();

        Alert accountAlert  = switchTo().alert();
        String accountAlertText = accountAlert .getText();
        assertThat(accountAlertText).contains("✅ New Account Created! Account Number:");
        accountAlert .accept();

        String accountNumber = extractAccountNumber(accountAlertText);

        $(Selectors.byText("💰 Deposit Money")).click();

        $("select.form-control.account-selector").shouldBe(Condition.visible).selectOptionContainingText(accountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue("abc");

        String actualValue = $(Selectors.byAttribute("placeholder", "Enter amount")).getValue();

        assertThat(actualValue).as("Поле для ввода суммы не должно содержать буквы").isEmpty();
    }

    @Test
    public void userCannotDepositEmpty() {

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
        $(Selectors.byText("➕ Create New Account")).click();

        Alert accountAlert  = switchTo().alert();
        String accountAlertText = accountAlert .getText();
        assertThat(accountAlertText).contains("✅ New Account Created! Account Number:");
        accountAlert .accept();

        String accountNumber = extractAccountNumber(accountAlertText);

        $(Selectors.byText("💰 Deposit Money")).click();

        $("select.form-control.account-selector").shouldBe(Condition.visible).selectOptionContainingText(accountNumber);
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        Alert errorAlert = switchTo().alert();
        String actualErrorMessage = errorAlert.getText();
        assertThat(actualErrorMessage).as("Для суммы %s ожидалось сообщение: %s ").contains("❌ Please enter a valid amount.");
        errorAlert.accept();

        int accountId = extractAccountId(accountNumber);

        List<TransactionResponse> transactions = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(accountId)
                .extract()
                .as(new TypeRef<List<TransactionResponse>>() {});

        assertThat(transactions).as("Транзакций не должно быть после неудачного депозита на сумму %s ").isEmpty();
    }

    private String extractAccountNumber(String alertText) {

        Pattern pattern = Pattern.compile("Account Number: (\\w+)");
        Matcher matcher = pattern.matcher(alertText);
        assertThat(matcher.find()).as("Account number should be present in alert: %s", alertText).isTrue();
        return matcher.group(1);
    }

    private int extractAccountId(String accountNumber) {

        assertThat(accountNumber).as("Account number should have ACC prefix").startsWith("ACC");
        return Integer.parseInt(accountNumber.substring(3));
    }
}
