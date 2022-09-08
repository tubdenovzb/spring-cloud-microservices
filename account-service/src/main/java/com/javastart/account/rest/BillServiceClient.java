package com.javastart.account.rest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "bill-service")
public interface BillServiceClient {

    @RequestMapping(value = "/bills/account/{accountId}", method = RequestMethod.GET)
    List<BillResponseDTO> getBillsByAccountId(@PathVariable("accountId") Long accountId);

    @RequestMapping(value = "/bills/default/", method = RequestMethod.POST)
    Long createDefaultBill(@RequestBody BillRequestDTO billRequestDTO);
}
