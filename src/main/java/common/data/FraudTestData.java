package common.data;

import java.math.BigDecimal;

public final class FraudTestData {

    private FraudTestData() {}

    // ===== Суммы =====
    public static final BigDecimal DEPOSIT_AMOUNT = new BigDecimal("5000.00");
    public static final BigDecimal TRANSFER_AMOUNT = new BigDecimal("100.00");

    // ===== Risk scores =====
    public static final double LOW_RISK_SCORE = 0.2;
    public static final double HIGH_RISK_SCORE = 0.95;
    public static final double SUSPICIOUS_RISK_SCORE = 0.6;
    public static final double VERIFICATION_RISK_SCORE = 0.75;

    // ===== Reasons =====
    public static final String LOW_RISK_REASON = "Low risk transaction";
    public static final String HIGH_RISK_REASON = "High risk transaction";
    public static final String SUSPICIOUS_REASON = "Suspicious activity";
    public static final String VERIFICATION_REASON = "Additional verification needed";

    // ===== Messages =====
    public static final String APPROVED_MESSAGE = "Transfer approved and processed immediately";
    public static final String BLOCKED_MESSAGE_PART = "blocked";

    // ===== Ports =====
    public static final int PORT_TIMEOUT = 8081;
    public static final int PORT_SERVER_ERROR = 8082;
    public static final int PORT_CONNECTION_ERROR = 8083;

    // ===== Delay =====
    public static final int TIMEOUT_DELAY_MS = 10_000;
}