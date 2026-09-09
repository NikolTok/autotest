package generators;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class RandomData {
    private RandomData() {}

    public static String getUsername() {
        return RandomStringUtils.randomAlphabetic(10);
    }

    public static String getPassword() {
        return RandomStringUtils.randomAlphabetic(3).toUpperCase() +
                RandomStringUtils.randomAlphabetic(5).toLowerCase() +
                RandomStringUtils.randomNumeric(3) + "%$#";
    }

    public static BigDecimal getBalance() {
        return BigDecimal.valueOf(
                RandomUtils.nextDouble(0.01, 4999.99)
        ).setScale(2, RoundingMode.DOWN);
    }

    public static BigDecimal getAmount() {
        return BigDecimal.valueOf(RandomUtils.nextDouble(0.01, 4999.99)
        ).setScale(2, RoundingMode.DOWN);
    }

    public static int getRandomNonExistentAccountId() {
        int min = 100000;
        int max = 999999;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
