package models;

import lombok.Data;

import java.util.List;

@Data
public class CustomerResponse {
    private int id;
    private String username;
    private String password;
    private String name;
    private String role;
    private List<AccountResponse> accounts;
}
