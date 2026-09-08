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
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.switchTo;
import static org.assertj.core.api.Assertions.assertThat;

public class TransferMoney {
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

    public static Stream<Arguments> transferNotCorrectDate() {
        return Stream.of(
                Arguments.of(new BigDecimal("0.00"), "Transfer amount must be at least 0.01"),
                Arguments.of(new BigDecimal("-0.01"), "Transfer amount must be at least 0.01"),
                Arguments.of(new BigDecimal("10000.01"), "Transfer amount cannot exceed 10000"));
    }

    @MethodSource("transferNotCorrectDate")
    @ParameterizedTest
    public void userCannotTransferWithInvalidAmount(BigDecimal amount, String expectedMessage) {

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

        $(Selectors.byText("➕ Create New Account")).click();
        Alert accountAlert2  = switchTo().alert();
        String accountAlertText2 = accountAlert2.getText();
        assertThat(accountAlertText2).contains("✅ New Account Created! Account Number:");
        accountAlert2.accept();

        String accountNumber = extractAccountNumber(accountAlertText);
        String accountNumber2 = extractAccountNumber(accountAlertText2);

        BigDecimal depositAmount = BigDecimal.valueOf(5000);

        $(Selectors.byText("💰 Deposit Money")).click();

        $("select.form-control.account-selector").shouldBe(Condition.visible).selectOptionContainingText(accountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(depositAmount.toString());
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $("select.form-control.account-selector").shouldBe(Condition.visible).selectOptionContainingText(accountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys("Noname");
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountNumber2);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(amount.toString());
        $(Selectors.byText("Confirm details are correct")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        Alert errorAlert = switchTo().alert();
        String actualErrorMessage = errorAlert.getText();
        assertThat(actualErrorMessage).as("Для суммы %s ожидалось сообщение: %s ", amount, expectedMessage).contains(expectedMessage);
        errorAlert.accept();

        int accountId1 = extractAccountId(accountNumber);

        List<TransactionResponse> transactions = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(accountId1)
                .extract()
                .as(new TypeRef<List<TransactionResponse>>() {});

        assertThat(transactions).as("Не должно быть транзакций перевода после неудачной попытки на сумму %s ", amount)
                .noneMatch(t -> t.getType().equals("TRANSFER_OUT"));
    }

    @Test
    public void userCannotTransferWithEmptyAmount() {

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

        $(Selectors.byText("➕ Create New Account")).click();
        Alert accountAlert2  = switchTo().alert();
        String accountAlertText2 = accountAlert2.getText();
        assertThat(accountAlertText2).contains("✅ New Account Created! Account Number:");
        accountAlert2.accept();

        String accountNumber = extractAccountNumber(accountAlertText);
        String accountNumber2 = extractAccountNumber(accountAlertText2);

        BigDecimal depositAmount = BigDecimal.valueOf(5000);

        $(Selectors.byText("💰 Deposit Money")).click();

        $("select.form-control.account-selector").shouldBe(Condition.visible).selectOptionContainingText(accountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(depositAmount.toString());
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();

        $("select.form-control.account-selector").shouldBe(Condition.visible).selectOptionContainingText(accountNumber);
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).sendKeys("Noname");
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).sendKeys(accountNumber2);
        $(Selectors.byText("Confirm details are correct")).click();
        $(Selectors.byText("\uD83D\uDE80 Send Transfer")).click();

        Alert errorAlert = switchTo().alert();
        String actualErrorMessage = errorAlert.getText();
        assertThat(actualErrorMessage).as("Для суммы %s ожидалось сообщение: %s ").contains("❌ Please fill all fields and confirm");
        errorAlert.accept();

        int accountId1 = extractAccountId(accountNumber);

        List<TransactionResponse> transactions = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                ResponseSpecs.requestReturnsOK())
                .get(accountId1)
                .extract()
                .as(new TypeRef<List<TransactionResponse>>() {});

        assertThat(transactions).as("Не должно быть транзакций перевода после неудачной попытки на сумму %s ")
                .noneMatch(t -> t.getType().equals("TRANSFER_OUT"));
    }

    private String extractAccountNumber(String alertText) {
        Pattern pattern = Pattern.compile("Account Number: (\\w+)");
        Matcher matcher = pattern.matcher(alertText);
        assertThat(matcher.find())
                .as("Account number should be present in alert: %s", alertText)
                .isTrue();
        return matcher.group(1);
    }

    private int extractAccountId(String accountNumber) {
        assertThat(accountNumber)
                .as("Account number should have ACC prefix")
                .startsWith("ACC");
        return Integer.parseInt(accountNumber.substring(3));
    }

    private List<TransactionResponse> getTransactions(String username, String password, int accountId) {
        return new GetAccountTransactionsRequester(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOK())
                .get(accountId)
                .extract()
                .as(new TypeRef<List<TransactionResponse>>() {});
    }
}