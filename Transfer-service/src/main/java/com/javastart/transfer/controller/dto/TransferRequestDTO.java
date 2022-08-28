package com.javastart.transfer.controller.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequestDTO {

    private Long senderAccountId;

    private Long recipientAccountId;

    private Long senderBillId;

    private Long recipientBillId;

    private BigDecimal amount;
}
