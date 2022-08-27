package com.javastart.payment.controller;

import com.javastart.payment.controller.dto.PaymentRequestDTO;
import com.javastart.payment.controller.dto.PaymentResponseDTO;
import com.javastart.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments")
    public PaymentResponseDTO payment(@RequestBody PaymentRequestDTO requestDTO) {
        return paymentService.payment(requestDTO.getAccountId(), requestDTO.getBillId(), requestDTO.getAmount());
    }
}
