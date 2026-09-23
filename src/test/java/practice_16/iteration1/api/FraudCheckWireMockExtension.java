package practice_16.iteration1.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common.annotations.FraudCheckMock;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class FraudCheckWireMockExtension implements BeforeEachCallback, AfterEachCallback {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private WireMockServer wireMockServer;

    @Override
    public void beforeEach(ExtensionContext context) {
        FraudCheckMock mockConfig = resolveAnnotation(context);

        if (mockConfig != null) {
            setupWireMock(mockConfig);
        }
    }

    private FraudCheckMock resolveAnnotation(ExtensionContext context) {
        return context.getTestMethod()
                .map(m -> m.getAnnotation(FraudCheckMock.class))
                .orElseGet(() -> context.getTestClass()
                        .map(c -> c.getAnnotation(FraudCheckMock.class))
                        .orElse(null));
    }

    private void setupWireMock(FraudCheckMock config) {
        wireMockServer = new WireMockServer(
                WireMockConfiguration.wireMockConfig().port(config.port())
        );
        wireMockServer.start();
        WireMock.configureFor("0.0.0.0", config.port());

        // Fallback для connection error — остановить сервер
        if (config.connectionError()) {
            wireMockServer.stop();
            return;
        }

        stubFor(post(urlPathMatching(config.endpoint()))
                .willReturn(aResponse()
                        .withStatus(config.httpStatus())
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(config.delayMs())   // ← задержка
                        .withBody(buildResponseBody(config))));
    }

    private String buildResponseBody(FraudCheckMock config) {
        try {
            Map<String, Object> body = Map.of(
                    "status", config.status(),
                    "decision", config.decision(),
                    "riskScore", config.riskScore(),
                    "reason", config.reason(),
                    "requiresManualReview", config.requiresManualReview(),
                    "additionalVerificationRequired", config.additionalVerificationRequired()
            );
            return MAPPER.writeValueAsString(body);
        } catch (Exception e) {
            throw new RuntimeException("Failed to build fraud-check response", e);
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }

    public String getBaseUrl() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            return "http://host.docker.internal:" + wireMockServer.port();
        }
        return null;
    }
}