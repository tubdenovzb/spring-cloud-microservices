package com.javastart.bill.service;

import com.javastart.bill.entity.Bill;
import com.javastart.bill.exception.BillNotFoundException;
import com.javastart.bill.repository.BillRepository;
import com.javastart.bill.rest.AccountRequestDTO;
import com.javastart.bill.rest.AccountResponseDTO;
import com.javastart.bill.rest.AccountServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BillService {

    private final BillRepository billRepository;

    private final AccountServiceClient accountServiceClient;

    @Autowired
    public BillService(BillRepository billRepository, AccountServiceClient accountServiceClient) {
        this.billRepository = billRepository;
        this.accountServiceClient = accountServiceClient;
    }

    public Bill getBillById(Long billId) {
        return billRepository.findById(billId)
                .orElseThrow(() -> new BillNotFoundException("Unable to find bill with id: " + billId));
    }

    public Long createBill(Long accountId, BigDecimal amount, Boolean isDefault, Boolean overdraftEnabled) {
        Bill bill = billRepository.save(new Bill(accountId, amount, isDefault, OffsetDateTime.now(), overdraftEnabled));
        AccountResponseDTO account = accountServiceClient.getAccountById(accountId);

        AccountRequestDTO accountRequestDTO = new AccountRequestDTO();
        accountRequestDTO.setName(account.getName());
        accountRequestDTO.setEmail(account.getEmail());
        accountRequestDTO.setPhone(account.getPhone());
        List<Long> billsByAccount = account.getBills();
        billsByAccount.add(bill.getBillId());
        accountRequestDTO.setBills(billsByAccount);
        accountRequestDTO.setCreationDate(account.getCreationDate());
        accountServiceClient.update(accountId, accountRequestDTO);
        return bill.getBillId();
    }

    public Long createDefaultBill(Long accountId, BigDecimal amount, Boolean isDefault, Boolean overdraftEnabled) {
        Bill defaultBill = new Bill(accountId, amount, isDefault, OffsetDateTime.now(), overdraftEnabled);
        return billRepository.save(defaultBill).getBillId();
    }

    public Bill updateBill(Long billId, Long accountId, BigDecimal amount,
                           Boolean isDefault, Boolean overdraftEnabled) {
        Bill bill = new Bill(accountId, amount, isDefault, OffsetDateTime.now(), overdraftEnabled);
        bill.setBillId(billId);
        return billRepository.save(bill);
    }

    public Bill deleteBill(Long billId) {
        Bill deletedBill = getBillById(billId);
        billRepository.deleteById(billId);
        return deletedBill;
    }

    public List<Bill> getBillsByAccountId(Long accountId) {
        return billRepository.getBillsByAccountId(accountId);
    }
}
