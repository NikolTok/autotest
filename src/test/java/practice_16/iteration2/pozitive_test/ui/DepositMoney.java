package practice_16.iteration2.pozitive_test.ui;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import generators.RandomData;
import io.restassured.common.mapper.TypeRef;
import models.CreateUserRequest;
import models.LoginUserRequest;
import models.TransactionResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
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

    @Test
    public void userCanDepositWithCorrectDate() {

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

        BigDecimal depositAmount = RandomData.getAmount();

        $("select.form-control.account-selector").shouldBe(Condition.visible).selectOptionContainingText(accountNumber);
        $(Selectors.byAttribute("placeholder", "Enter amount")).sendKeys(depositAmount.toString());
        $(Selectors.byText("\uD83D\uDCB5 Deposit")).click();

        String depositAlertText = switchTo().alert().getText();

        String expectedAlertText = String.format(Locale.US, "✅ Successfully deposited $%.2f to account %s!", depositAmount, accountNumber);
        assertThat(depositAlertText).isEqualTo(expectedAlertText);
        switchTo().alert().accept();

        int accountId = extractAccountId(accountNumber);

        List<TransactionResponse> transactions = new GetAccountTransactionsRequester(RequestSpecs.authAsUser(user.getUsername(), user.getPassword()),
                        ResponseSpecs.requestReturnsOK())
                        .get(accountId)
                        .extract()
                        .as(new TypeRef<List<TransactionResponse>>() {});

        assertThat(transactions).hasSize(1);
        TransactionResponse transaction = transactions.get(0);
        assertThat(transaction.getAmount()).isEqualByComparingTo(depositAmount);
        assertThat(transaction.getType()).isEqualTo("DEPOSIT");
        assertThat(transaction.getRelatedAccountId()).isEqualTo(accountId);
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
