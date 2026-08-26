package models;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountResponse {

    private int id;
    private String accountNumber;
    private BigDecimal balance;
    private List<TransactionResponse> transactions;
}