package com.javastart.transfer.controller.dto;

import com.javastart.transfer.entity.Transfer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDTO {

    private BigDecimal amount;

    private String senderEmail;

    private String recipientEmail;

    public TransferResponseDTO(Transfer transfer) {
        amount = transfer.getAmount();
        senderEmail = transfer.getSenderEmail();
        recipientEmail = transfer.getRecipientEmail();
    }
}
