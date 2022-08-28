package com.javastart.payment.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.payment.controller.dto.PaymentResponseDTO;
import com.javastart.payment.entity.Payment;
import com.javastart.payment.exception.PaymentServiceException;
import com.javastart.payment.repository.PaymentRepository;
import com.javastart.payment.rest.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class PaymentService {

    private static final String TOPIC_EXCHANGE_PAYMENT = "js.payment.notify.exchange";

    private static final String ROUTING_KEY_PAYMENT = "js.key.payment";

    private final PaymentRepository paymentRepository;

    private AccountServiceClient accountServiceClient;

    private final BillServiceClient billServiceClient;

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, AccountServiceClient accountServiceClient,
                          BillServiceClient billServiceClient, RabbitTemplate rabbitTemplate) {
        this.paymentRepository = paymentRepository;
        this.accountServiceClient = accountServiceClient;
        this.billServiceClient = billServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    public PaymentResponseDTO payment(Long accountId, Long billId, BigDecimal amount) {
        if (accountId == null && billId == null) {
            throw new PaymentServiceException("Account is null and bill is null");
        }

        if (billId != null) {
            BillResponseDTO billResponseDTO = billServiceClient.getBillById(billId);
            BillRequestDTO billRequestDTO = createBillRequest(amount, billResponseDTO);

            billServiceClient.update(billId, billRequestDTO);

            AccountResponseDTO accountResponseDTO = accountServiceClient.getAccountById(billResponseDTO.getAccountId());
            paymentRepository.save(new Payment(amount, billId, OffsetDateTime.now(), accountResponseDTO.getEmail()));

            return createResponse(amount, accountResponseDTO);
        }

        BillResponseDTO defaultBill = getDefaultBill(accountId);
        BillRequestDTO billRequestDTO = createBillRequest(amount, defaultBill);
        billServiceClient.update(defaultBill.getBillId(), billRequestDTO);
        AccountResponseDTO account = accountServiceClient.getAccountById(accountId);
        paymentRepository.save(new Payment(amount, defaultBill.getBillId(), OffsetDateTime.now(), account.getEmail()));
        return createResponse(amount, account);
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentServiceException("Unable to find payment by id: " + paymentId));
    }

    public List<Payment> getPaymentsByBillId(Long billId) {
        List<Payment> paymentsByBillId = paymentRepository.getPaymentsByBillId(billId);
        if (paymentsByBillId.isEmpty()) {
            throw new PaymentServiceException("Unable to find payments for bill: " + billId);
        }
        return paymentsByBillId;
    }

    private PaymentResponseDTO createResponse(BigDecimal amount, AccountResponseDTO accountResponseDTO) {
        PaymentResponseDTO paymentResponseDTO = new PaymentResponseDTO(amount, accountResponseDTO.getEmail());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_PAYMENT, ROUTING_KEY_PAYMENT,
                    objectMapper.writeValueAsString(paymentResponseDTO));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new PaymentServiceException("Can't send message to RabbitMQ");
        }
        return paymentResponseDTO;
    }

    private static BillRequestDTO createBillRequest(BigDecimal amount, BillResponseDTO billResponseDTO) {
        BillRequestDTO billRequestDTO = new BillRequestDTO();
        billRequestDTO.setAccountId(billResponseDTO.getAccountId());
        billRequestDTO.setCreationDate(billResponseDTO.getCreationDate());
        billRequestDTO.setIsDefault(billResponseDTO.getIsDefault());
        billRequestDTO.setOverdraftEnabled(billResponseDTO.getOverdraftEnabled());
        billRequestDTO.setAmount(billResponseDTO.getAmount().subtract(amount));
        return billRequestDTO;
    }

    private BillResponseDTO getDefaultBill(Long accountId) {
        return billServiceClient.getBillsByAccountId(accountId).stream()
                .filter(BillResponseDTO::getIsDefault)
                .findAny()
                .orElseThrow(() -> new PaymentServiceException("Unable to find default bill for account: " + accountId));
    }
}
