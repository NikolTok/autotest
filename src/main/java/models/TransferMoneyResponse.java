package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferMoneyResponse extends BaseModel {
    private int receiverAccountId;
    private BigDecimal amount;
    private String message;
    private int senderAccountId;
}
