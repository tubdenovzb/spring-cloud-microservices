package com.javastart.payment.controller.dto;

import com.javastart.payment.entity.Payment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private BigDecimal amount;

    private String email;

    public PaymentResponseDTO(Payment payment) {
        amount = payment.getAmount();
        email = payment.getEmail();
    }
}
