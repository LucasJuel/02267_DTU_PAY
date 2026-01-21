package org.g10;
 
import org.g10.DTO.CustomerDTO;
import org.g10.services.CustomerService;
import org.g10.services.ReportingService;
import org.g10.utils.StorageHandler;

import java.util.List;
import java.util.Map;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReportingServiceSteps {
    private final ReportingService reportingService = new ReportingService();
    private final StorageHandler storageHandler = StorageHandler.getInstance();
    private String customerId;
    private List<Map<String, Object>> report;

    @When("I generate a report for the customer")
    public void i_generate_a_report_for_the_customer() {
        report = reportingService.getAllPayments();
        assertNotNull(report);
    }
    @Then("the report returns an empty list of payments")
    public void the_report_returns_an_empty_list_of_payments() {
        assertEquals(0, report.size());
    }
}
