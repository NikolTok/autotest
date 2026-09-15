package common.data;

import java.math.BigDecimal;

public enum TestData {

    MAX_DEPOSIT(new BigDecimal("5000.00")),

    THOUSAND_TRANSFER(new BigDecimal("100.00")),
    MIN_TRANSFER(new BigDecimal("1.00")),
    DEFAULT_TRANSFER(new BigDecimal("50.00")),

    TEST_WORD("abc"),
    RECIPIENT_NAME("Noname");

    private final Object value;

    TestData(Object value) {
        this.value = value;
    }

    public BigDecimal getAmount() {
        return (BigDecimal) value;
    }

    public String getString() {
        return (String) value;
    }
}
