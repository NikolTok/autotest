package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import org.openqa.selenium.By;

import java.math.BigDecimal;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

@Getter
public class UserDashboard extends BasePage<UserDashboard> {

    private SelenideElement welcomeText = $(Selectors.byClassName("welcome-text"));

    private SelenideElement createNewAccount = $(Selectors.byText("➕ Create New Account"));

    private SelenideElement depositMoney = $(Selectors.byText("💰 Deposit Money"));

    private SelenideElement accountSelector = $("select.form-control.account-selector");

    private SelenideElement amountInput = $(Selectors.byAttribute("placeholder", "Enter amount"));

    private SelenideElement depositButton = $(Selectors.byText("💵 Deposit"));

    @Override
    public String url() {
        return "/dashboard";
    }

    public UserDashboard createNewAccount() {
        createNewAccount.click();
        return this;
    }

    public UserDashboard openDepositMoney() {
        depositMoney.click();
        return this;
    }

    public UserDashboard selectAccount(String accountNumber) {
        accountSelector.selectOptionContainingText(accountNumber);
        return this;
    }

    public UserDashboard enterDepositAmount(BigDecimal amount) {
        amountInput.setValue(amount.toString());
        return this;
    }

    public UserDashboard enterDepositAmount(String amount) {
        amountInput.setValue(amount);
        return this;
    }

    public UserDashboard deposit() {
        depositButton.click();
        return this;
    }

    public String getAmountInputValue() {
        return amountInput.getValue();
    }

    public UserDashboard openTransferMoney() {
        $(Selectors.byText("\uD83D\uDD04 Make a Transfer")).click();
        return this;
    }

    public UserDashboard selectSenderAccount(String accountNumber) {
        $("select.form-control.account-selector").selectOptionContainingText(accountNumber);
        return this;
    }

    public UserDashboard enterRecipientName(String name) {
        $(Selectors.byAttribute("placeholder", "Enter recipient name")).setValue(name);
        return this;
    }

    public UserDashboard selectRecipientAccount(String accountNumber) {
        $(Selectors.byAttribute("placeholder", "Enter recipient account number")).setValue(accountNumber);
        return this;
    }

    public UserDashboard enterTransferAmount(BigDecimal amount) {
        $(Selectors.byAttribute("placeholder", "Enter amount")).setValue(amount.toString());
        return this;
    }

    public UserDashboard confirmDetails() {
        $(Selectors.byText("Confirm details are correct")).click();
        return this;
    }

    public UserDashboard sendTransfer() {
        $(Selectors.byText("🚀 Send Transfer")).click();
        return this;
    }

    public UserDashboard clickTransferAgain() {
        $(By.xpath("//*[contains(text(), '\uD83D\uDD01 Transfer Again')]")).shouldBe(Condition.clickable).click();
        return this;
    }

    public UserDashboard searchTransaction(String searchText) {
        $(Selectors.byAttribute("placeholder", "Enter name to find transactions")).setValue(searchText);
        $(By.xpath("//*[contains(text(), '\uD83D\uDD0D Search Transactions')]")).shouldBe(Condition.clickable).click();
        return this;
    }

    public UserDashboard clickRepeat() {
        $$(By.xpath("//*[contains(text(), '\uD83D\uDD01 Repeat')]")).first().shouldBe(Condition.clickable).click();
        return this;
    }

    public UserDashboard selectAccountInModal(String accountNumber) {
        $("div.modal.show select.form-control").selectOptionContainingText(accountNumber);
        return this;
    }

    public UserDashboard enterAmountInModal(BigDecimal amount) {
        $("div.modal.show input.form-control[type='number']").setValue(amount.toString());
        return this;
    }

    public UserDashboard sendTransferInModal() {
        $(By.xpath("//*[contains(text(), '🚀 Send Transfer')]")).shouldBe(Condition.clickable).click();
        return this;
    }

    public UserDashboard openProfile() {
        $(By.xpath("//*[contains(text(), 'Noname')]")).shouldBe(Condition.clickable).click();
        return this;
    }

    public UserDashboard enterNewName(String name) {
        $(Selectors.byAttribute("placeholder", "Enter new name")).shouldBe(Condition.visible).setValue(name);
        return this;
    }

    public UserDashboard saveChanges() {
        $(By.xpath("//*[contains(text(), '\uD83D\uDCBE Save Changes')]")).shouldBe(Condition.clickable).click();
        return this;
    }

    public UserDashboard verifyProfileName(String expectedName) {
        $(By.xpath("//*[contains(text(), '" + expectedName + "')]")).shouldBe(Condition.visible);
        return this;
    }
}