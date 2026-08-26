package models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepositMoneyResponse extends BaseModel{
    private int id;
    private String accountNumber;
    private BigDecimal balance;
    private List<TransactionResponse> transactions;
}
