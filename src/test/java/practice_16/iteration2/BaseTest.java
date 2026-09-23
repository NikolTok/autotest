package practice_16.iteration2;

import api.configs.Config;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.baseURI;

public class BaseTest {

    protected SoftAssertions softly;

    @BeforeAll
    public static void setUp() {
        baseURI = Config.getProperty("api.baseUrl");
    }

    @BeforeEach
    public void initSoftAssertions() {
        softly = new SoftAssertions();
    }

    @AfterEach
    public void assertAll() {
        softly.assertAll();
    }
}