package org.g10.services;

import org.g10.DTO.ReportDTO;
import org.g10.utils.StorageHandler;
import java.util.List;
import java.util.Map;

public class ReportingService {
    private final StorageHandler storageHandler = StorageHandler.getInstance();
    public ReportingService() {
    }

    public List<Map<String, Object>> getAllPayments(ReportDTO reportRequest) {
        String id = reportRequest.getAccountId();
        System.out.println("Fetching all payments for report ID: " + id);
        List<Map<String, Object>> allPayments = storageHandler.readPayments();
        List<Map<String, Object>> filteredPayments = new java.util.ArrayList<>();
        for (Map<String, Object> payment : allPayments) {
            if(reportRequest.getAccountType().equals("merchant")) {
                if (payment.get("merchantId").equals(id)) {
                    System.out.println("Included payment for merchant ID " + id + ": " + payment);
                    filteredPayments.add(payment);
                }
            } else if(reportRequest.getAccountType().equals("customer")) {
                if (payment.get("customerId").equals(id)) {
                    System.out.println("Included payment for customer ID " + id + ": " + payment);
                    filteredPayments.add(payment);
                }
            }
        }
        System.out.println("Total payments retrieved: " + allPayments.size());
        return filteredPayments;
    }
    
}
