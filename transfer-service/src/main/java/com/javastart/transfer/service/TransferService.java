package com.javastart.transfer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javastart.transfer.controller.dto.TransferResponseDTO;
import com.javastart.transfer.entity.Transfer;
import com.javastart.transfer.exception.TransferServiceException;
import com.javastart.transfer.repository.TransferRepository;
import com.javastart.transfer.rest.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class TransferService {

    private static final String TOPIC_EXCHANGE_TRANSFER = "js.transfer.notify.exchange";

    private static final String ROUTING_KEY_TRANSFER = "js.key.transfer";

    private final TransferRepository transferRepository;

    private final AccountServiceClient accountServiceClient;

    private final BillServiceClient billServiceClient;

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public TransferService(TransferRepository transferRepository, AccountServiceClient accountServiceClient,
                           BillServiceClient billServiceClient, RabbitTemplate rabbitTemplate) {
        this.transferRepository = transferRepository;
        this.accountServiceClient = accountServiceClient;
        this.billServiceClient = billServiceClient;
        this.rabbitTemplate = rabbitTemplate;
    }

    public TransferResponseDTO transfer(Long senderAccountId, Long recipientAccountId,
                                        Long senderBillId, Long recipientBillId, BigDecimal amount) {
        if (senderAccountId == null && senderBillId == null) {
            throw new TransferServiceException("Sender account is null and bill is null");
        }

        if (recipientAccountId == null && recipientBillId == null) {
            throw new TransferServiceException("Recipient account is null and bill is null");
        }

        if (senderBillId != null && recipientBillId != null) {
            BillResponseDTO senderBillResponseDTO = billServiceClient.getBillById(senderBillId);
            BillRequestDTO senderBillRequestDTO = createBillRequestBySender(amount, senderBillResponseDTO);
            billServiceClient.update(senderBillId, senderBillRequestDTO);

            BillResponseDTO recipientBillResponseDTO = billServiceClient.getBillById(recipientBillId);
            BillRequestDTO recipientBillRequestDTO = createBillRequestByRecipient(amount, recipientBillResponseDTO);
            billServiceClient.update(recipientBillId, recipientBillRequestDTO);

            AccountResponseDTO senderAccountResponseDTO = accountServiceClient.getAccountById(senderBillRequestDTO.getAccountId());
            AccountResponseDTO recipientAccountResponseDTO = accountServiceClient.getAccountById(recipientBillRequestDTO.getAccountId());

            transferRepository.save(new Transfer(amount, senderBillId, recipientBillId, OffsetDateTime.now(),
                    senderAccountResponseDTO.getEmail(), recipientAccountResponseDTO.getEmail()));

            return createResponse(amount, senderAccountResponseDTO, recipientAccountResponseDTO);
        }

        BillResponseDTO defaultSenderBill = getDefaultBill(senderAccountId);
        BillRequestDTO senderBillRequestDTO = createBillRequestBySender(amount, defaultSenderBill);
        billServiceClient.update(defaultSenderBill.getBillId(), senderBillRequestDTO);
        AccountResponseDTO senderAccount = accountServiceClient.getAccountById(senderAccountId);

        BillResponseDTO defaultRecipientBill = getDefaultBill(recipientAccountId);
        BillRequestDTO recipientBillRequestDTO = createBillRequestByRecipient(amount, defaultRecipientBill);
        billServiceClient.update(defaultRecipientBill.getBillId(), recipientBillRequestDTO);
        AccountResponseDTO recipientAccount = accountServiceClient.getAccountById(recipientAccountId);

        return createResponse(amount, senderAccount, recipientAccount);
    }

    public Transfer getTransferById(Long transferId) {
        return transferRepository.findById(transferId)
                .orElseThrow(() -> new TransferServiceException("Unable to find transfer by id: " + transferId));
    }

    public List<Transfer> getTransfersBySenderBillId(Long senderBillId) {
        List<Transfer> transferBySenderBillId = transferRepository.getTransferBySenderBillId(senderBillId);
        if (transferBySenderBillId.isEmpty()) {
            throw new TransferServiceException("Unable to find transfers for sender's bill: " + senderBillId);
        }
        return transferBySenderBillId;
    }

    public List<Transfer> getTransfersByRecipientBillId(Long recipientBillId) {
        List<Transfer> transferByRecipientBillId = transferRepository.getTransferByRecipientBillId(recipientBillId);
        if (transferByRecipientBillId.isEmpty()) {
            throw new TransferServiceException("Unable to find transfers for recipient's bill: " + recipientBillId);
        }
        return transferByRecipientBillId;
    }

    private TransferResponseDTO createResponse(BigDecimal amount, AccountResponseDTO senderAccountResponseDTO, AccountResponseDTO recipientAccountResponseDTO) {
        TransferResponseDTO transferResponseDTO = new TransferResponseDTO(amount,
                senderAccountResponseDTO.getEmail(), recipientAccountResponseDTO.getEmail());

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            rabbitTemplate.convertAndSend(TOPIC_EXCHANGE_TRANSFER, ROUTING_KEY_TRANSFER,
                    objectMapper.writeValueAsString(transferResponseDTO));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new TransferServiceException("Can't send message to RabbitMQ");
        }
        return transferResponseDTO;
    }

    private static BillRequestDTO createBillRequestBySender(BigDecimal amount, BillResponseDTO senderBillResponseDTO) {
        BillRequestDTO BillRequestDTO = new BillRequestDTO();
        BillRequestDTO.setAccountId(senderBillResponseDTO.getAccountId());
        BillRequestDTO.setCreationDate(senderBillResponseDTO.getCreationDate());
        BillRequestDTO.setIsDefault(senderBillResponseDTO.getIsDefault());
        BillRequestDTO.setOverdraftEnabled(senderBillResponseDTO.getOverdraftEnabled());
        BillRequestDTO.setAmount(senderBillResponseDTO.getAmount().subtract(amount));
        return BillRequestDTO;
    }

    private static BillRequestDTO createBillRequestByRecipient(BigDecimal amount, BillResponseDTO recipientBillResponseDTO) {
        BillRequestDTO BillRequestDTO = new BillRequestDTO();
        BillRequestDTO.setAccountId(recipientBillResponseDTO.getAccountId());
        BillRequestDTO.setCreationDate(recipientBillResponseDTO.getCreationDate());
        BillRequestDTO.setIsDefault(recipientBillResponseDTO.getIsDefault());
        BillRequestDTO.setOverdraftEnabled(recipientBillResponseDTO.getOverdraftEnabled());
        BillRequestDTO.setAmount(recipientBillResponseDTO.getAmount().add(amount));
        return BillRequestDTO;
    }

    private BillResponseDTO getDefaultBill(Long accountId) {
        return billServiceClient.getBillsByAccountId(accountId).stream()
                .filter(BillResponseDTO::getIsDefault)
                .findAny()
                .orElseThrow(() -> new TransferServiceException("Unable to find default bill by account: " + accountId));
    }
}
