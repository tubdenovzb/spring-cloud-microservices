package com.javastart.transfer.controller;

import com.javastart.transfer.controller.dto.TransferRequestDTO;
import com.javastart.transfer.controller.dto.TransferResponseDTO;
import com.javastart.transfer.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class TransferController {

    private final TransferService transferService;

    @Autowired
    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/transfers")
    public TransferResponseDTO transfer(@RequestBody TransferRequestDTO transferRequestDTO) {
        return transferService.transfer(transferRequestDTO.getSenderAccountId(), transferRequestDTO.getRecipientAccountId(),
                transferRequestDTO.getSenderBillId(), transferRequestDTO.getRecipientBillId(), transferRequestDTO.getAmount());
    }

    @GetMapping("/transfers/{transferId}")
    public TransferResponseDTO getTransfer(@PathVariable Long transferId) {
        return new TransferResponseDTO(transferService.getTransferById(transferId));
    }

    @GetMapping("/transfers/bill/sender/{senderBillId}")
    public List<TransferResponseDTO> getTransfersBySenderBillId(@PathVariable Long senderBillId) {
        return transferService.getTransfersBySenderBillId(senderBillId).stream()
                .map(TransferResponseDTO::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/transfers/bill/recipient/{recipientBillId}")
    public List<TransferResponseDTO> getTransfersByRecipientBillId(@PathVariable Long recipientBillId) {
        return transferService.getTransfersByRecipientBillId(recipientBillId).stream()
                .map(TransferResponseDTO::new)
                .collect(Collectors.toList());
    }
}
