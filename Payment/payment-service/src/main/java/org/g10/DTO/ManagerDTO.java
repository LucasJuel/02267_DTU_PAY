package org.g10.DTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
/**
 @author Martin-Surlykke
 **/
public class ManagerDTO {
    private List<Map<String, Object>> payments;
    private int paymentCount;
    private BigDecimal totalAmount;

    public ManagerDTO() {
    }

    public ManagerDTO(List<Map<String, Object>> payments, int paymentCount, BigDecimal totalAmount) {
        this.payments = payments;
        this.paymentCount = paymentCount;
        this.totalAmount = totalAmount;
    }

    public List<Map<String, Object>> getPayments() {
        return payments;
    }

    public void setPayments(List<Map<String, Object>> payments) {
        this.payments = payments;
    }

    public int getPaymentCount() {
        return paymentCount;
    }

    public void setPaymentCount(int paymentCount) {
        this.paymentCount = paymentCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
