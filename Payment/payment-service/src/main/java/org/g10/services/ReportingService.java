package org.g10.services;

import java.math.BigDecimal;
import org.g10.DTO.ReportDTO;
import org.g10.DTO.ManagerDTO;
import org.g10.utils.StorageHandler;
import java.util.List;
import java.util.Map;
/**
 @author BertramKjær
 **/

public class ReportingService {
    private final StorageHandler storageHandler = StorageHandler.getInstance();
    public ReportingService() {
    }

    public List<Map<String, Object>> getAllPayments(ReportDTO reportRequest) {
        if (reportRequest != null && "manager".equals(reportRequest.getAccountType())) {
            return storageHandler.readPayments();
        }
        String id = reportRequest.getAccountId();
        List<Map<String, Object>> allPayments = storageHandler.readPayments();
        List<Map<String, Object>> filteredPayments = new java.util.ArrayList<>();
        for (Map<String, Object> payment : allPayments) {
            if(reportRequest.getAccountType().equals("merchant")) {
                if (payment.get("merchantId").equals(id)) {
                    filteredPayments.add(payment);
                }
            } else if(reportRequest.getAccountType().equals("customer")) {
                if (payment.get("customerId").equals(id)) {
                    filteredPayments.add(payment);
                }
            }
        }
        return filteredPayments;
    }

    public ManagerDTO getManagerReport(ReportDTO reportRequest) {
        List<Map<String, Object>> payments = getAllPayments(reportRequest);
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Map<String, Object> payment : payments) {
            Object amountObj = payment.get("amount");
            if (amountObj instanceof BigDecimal amount) {
                totalAmount = totalAmount.add(amount);
            }
        }
        return new ManagerDTO(payments, payments.size(), totalAmount);
    }
    
}
