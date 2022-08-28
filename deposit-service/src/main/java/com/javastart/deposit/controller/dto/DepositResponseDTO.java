package com.javastart.deposit.controller.dto;

import com.javastart.deposit.entity.Deposit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepositResponseDTO {

    private BigDecimal amount;

    private String email;

    public DepositResponseDTO(Deposit deposit) {
        amount = deposit.getAmount();
        email = deposit.getEmail();
    }
}
