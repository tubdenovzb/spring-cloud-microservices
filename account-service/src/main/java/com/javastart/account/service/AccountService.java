package com.javastart.account.service;

import com.javastart.account.entity.Account;
import com.javastart.account.exception.AccountNotFoundException;
import com.javastart.account.repository.AccountRepository;
import com.javastart.account.rest.BillRequestDTO;
import com.javastart.account.rest.BillResponseDTO;
import com.javastart.account.rest.BillServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    private final BillServiceClient billServiceClient;

    @Autowired
    public AccountService(AccountRepository accountRepository, BillServiceClient billServiceClient) {
        this.accountRepository = accountRepository;
        this.billServiceClient = billServiceClient;
    }

    public Account getAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Unable to find account with id: " + accountId));
    }

    public Long createAccount(String name, String email, String phone, List<Long> bills) {
        Account account = accountRepository.save(new Account(name, email, phone, OffsetDateTime.now(), bills));

        List<BillResponseDTO> billsByAccountId = billServiceClient.getBillsByAccountId(account.getAccountId());
        if (billsByAccountId.isEmpty()) {
            BillRequestDTO billRequestDTO = new BillRequestDTO();
            billRequestDTO.setAccountId(account.getAccountId());
            billRequestDTO.setAmount(new BigDecimal(0));
            billRequestDTO.setIsDefault(true);
            billRequestDTO.setOverdraftEnabled(false);
            account.getBills().add(billServiceClient.createDefaultBill(billRequestDTO));
            accountRepository.save(account);
        }

        account.getBills().addAll(billsByAccountId.stream()
                .map(BillResponseDTO::getBillId)
                .collect(Collectors.toList()));
        accountRepository.save(account);
        return account.getAccountId();
    }

    public Account updateAccount(Long accountId, String name,
                                 String email, String phone, List<Long> bills) {
        Account account = new Account(name, email, phone, OffsetDateTime.now(), bills);
        account.setAccountId(accountId);
        return accountRepository.save(account);
    }

    public Account deleteAccount(Long accountId) {
        Account deletedAccount = getAccountById(accountId);
        accountRepository.deleteById(accountId);
        return deletedAccount;
    }
}
