package com.javastart.transfer.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long transferId;

    private BigDecimal amount;

    private Long senderBillId;

    private Long recipientBillId;

    private OffsetDateTime creationDate;

    private String senderEmail;

    private String recipientEmail;

    public Transfer(BigDecimal amount, Long senderBillId, Long recipientBillId,
                    OffsetDateTime creationDate, String senderEmail, String recipientEmail) {
        this.amount = amount;
        this.senderBillId = senderBillId;
        this.recipientBillId = recipientBillId;
        this.creationDate = creationDate;
        this.senderEmail = senderEmail;
        this.recipientEmail = recipientEmail;
    }
}
