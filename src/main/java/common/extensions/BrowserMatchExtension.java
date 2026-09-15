package common.extensions;

import com.codeborne.selenide.Configuration;
import common.annotations.Browsers;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;

public class BrowserMatchExtension implements ExecutionCondition {
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Browsers annotation = context.getElement()
                .map(el -> el.getAnnotation(Browsers.class))
                .orElse(null);

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("Нет ограничений к браузеру");
        }

        String currentBrowser = Configuration.browser;
        boolean matches = Arrays.stream(annotation.value())
                .anyMatch(browser -> browser.equals(currentBrowser));

        if (matches) {
            return ConditionEvaluationResult.enabled("Текущий браузер удовл. условию: " + currentBrowser);
        } {
            return ConditionEvaluationResult.disabled("Тест пропущен, т.к. текущий браузер не удовл. условию: " + currentBrowser +
                    " не находится в списке допустимых браузеров: " + Arrays.toString(annotation.value()));
        }
    }
}
