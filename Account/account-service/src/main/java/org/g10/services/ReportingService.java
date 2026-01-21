package org.g10.services;

import org.g10.utils.StorageHandler;
import java.util.List;
import java.util.Map;

public class ReportingService {
    private final StorageHandler storageHandler = StorageHandler.getInstance();
    public ReportingService() {
    }

    public List<Map<String, Object>> getAllPayments() {
        return storageHandler.readPayments();
    }
    
}
