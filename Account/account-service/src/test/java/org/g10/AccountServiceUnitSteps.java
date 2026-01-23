package org.g10;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.g10.DTO.CustomerDTO;
import org.g10.DTO.MerchantDTO;
import org.g10.services.CustomerService;
import org.g10.services.MerchantService;
import org.g10.utils.StorageHandler;

import static org.junit.jupiter.api.Assertions.*;

public class AccountServiceUnitSteps {

    private CustomerService customerService;
    private MerchantService merchantService;
    private CustomerDTO registeredCustomer;
    private MerchantDTO registeredMerchant;
    private String customerId;
    private String merchantId;
    private String secondCustomerId;
    private String secondMerchantId;
    private CustomerDTO fetchedCustomer;
    private MerchantDTO fetchedMerchant;
    private StorageHandler mockStorage;
    String deregisterSuccess;

    @Given("a clean account storage")
    public void aCleanAccountStorage() {
        StorageHandler.getInstance().clearAll();
        customerService = new CustomerService();
        merchantService = new MerchantService();
    }

    @Given("I register a customer with first name {string}, last name {string}, CPR {string}, and bank account {string}")
    public void iRegisterACustomer(String firstName, String lastName, String cpr, String bankAccountId) {
        registeredCustomer = new CustomerDTO(firstName, lastName, cpr, bankAccountId);
        customerId = customerService.register(registeredCustomer);
    }

    @Then("the stored customer matches the registration")
    public void theStoredCustomerMatchesTheRegistration() {
        assertNotNull(customerId);
        CustomerDTO stored = customerService.getCustomer(customerId);
        assertNotNull(stored);
        assertEquals(registeredCustomer.getFirstName(), stored.getFirstName());
        assertEquals(registeredCustomer.getLastName(), stored.getLastName());
        assertEquals(registeredCustomer.getCpr(), stored.getCpr());
        assertEquals(registeredCustomer.getBankAccountId(), stored.getBankAccountId());
    }

    @When("I register a merchant with first name {string}, last name {string}, CPR {string}, and bank account {string}")
    public void iRegisterAMerchant(String firstName, String lastName, String cpr, String bankAccountId) {
        registeredMerchant = new MerchantDTO(firstName, lastName, cpr, bankAccountId);
        merchantId = merchantService.register(registeredMerchant);
    }

    @Then("the stored merchant matches the registration")
    public void theStoredMerchantMatchesTheRegistration() {
        assertNotNull(merchantId);
        MerchantDTO stored = merchantService.getMerchant(merchantId);
        assertNotNull(stored);
        assertEquals(registeredMerchant.getFirstName(), stored.getFirstName());
        assertEquals(registeredMerchant.getLastName(), stored.getLastName());
        assertEquals(registeredMerchant.getCpr(), stored.getCpr());
        assertEquals(registeredMerchant.getBankAccountId(), stored.getBankAccountId());
    }

    @When("I fetch a customer with id {string}")
    public void iFetchACustomerWithId(String id) {
        fetchedCustomer = customerService.getCustomer(id);
    }

    @Then("no customer is returned")
    public void noCustomerIsReturned() {
        assertNull(fetchedCustomer);
    }

    @When("I fetch a merchant with id {string}")
    public void iFetchAMerchantWithId(String id) {
        fetchedMerchant = merchantService.getMerchant(id);
    }

    @Then("no merchant is returned")
    public void noMerchantIsReturned() {
        assertNull(fetchedMerchant);
    }

    @When("I register the same customer again")
    public void iRegisterTheSameCustomerAgain() {
        secondCustomerId = customerService.register(registeredCustomer);
    }

    @Then("both customer registrations are stored separately")
    public void bothCustomerRegistrationsAreStoredSeparately() {
        assertNotNull(customerId);
        assertNotNull(secondCustomerId);
        assertNotEquals(customerId, secondCustomerId);
        assertNotNull(customerService.getCustomer(customerId));
        assertNotNull(customerService.getCustomer(secondCustomerId));
    }

    @When("I register the same merchant again")
    public void iRegisterTheSameMerchantAgain() {
        secondMerchantId = merchantService.register(registeredMerchant);
    }

    @Then("both merchant registrations are stored separately")
    public void bothMerchantRegistrationsAreStoredSeparately() {
        assertNotNull(merchantId);
        assertNotNull(secondMerchantId);
        assertNotEquals(merchantId, secondMerchantId);
        assertNotNull(merchantService.getMerchant(merchantId));
        assertNotNull(merchantService.getMerchant(secondMerchantId));
    }

    @Then("customer and merchant ids do not resolve across stores")
    public void customerAndMerchantIdsDoNotResolveAcrossStores() {
        assertNotNull(customerId);
        assertNotNull(merchantId);
        assertNull(customerService.getCustomer(merchantId));
        assertNull(merchantService.getMerchant(customerId));
    }

    @When("I deregister the customer with the customer id")
    public void iDeregisterTheCustomerWithTheCustomerId() {
        assertNotNull(customerId, "Customer ID should be set before deregistration");
        customerService.deregister(customerId);
    }

    @Then("the customer is no longer in storage")
    public void theCustomerIsNoLongerInStorage() {
        CustomerDTO stored = customerService.getCustomer(customerId);
        assertNull(stored, "Customer should be null after deregistration");
    }

    @When("I deregister the merchant with the merchant id")
    public void iDeregisterTheMerchantWithTheMerchantId() {
        assertNotNull(merchantId, "Merchant ID should be set before deregistration");
        merchantService.deregister(merchantId);
    }

    @Then("the merchant is no longer in storage")
    public void theMerchantIsNoLongerInStorage() {
        MerchantDTO stored = merchantService.getMerchant(merchantId);
        assertNull(stored, "Merchant should be null after deregistration");
    }

    @When("I attempt to deregister a customer with id {string}")
    public void iAttemptToDeregisterACustomerWithId(String id) {
        // Attempt to deregister, should not throw exception even if customer does not exist
        customerService.deregister(id);
    }

    @When("I attempt to deregister a merchant with id {string}")
    public void iAttemptToDeregisterAMerchantWithId(String id) {
        // Attempt to deregister, should not throw exception even if merchant does not exist
        merchantService.deregister(id);
    }

    @Then("the deregistration completes without error")
    public void theDeregistrationCompletesWithoutError() {
        assertTrue(true);
    }

    @Given("a mocked account storage that fails on register")
    public void a_mocked_account_storage_that_fails_on_register() {
        mockStorage = new StorageHandler(){
            @Override
            public void storeCustomer(String key, CustomerDTO customer) throws Exception {
                throw new Exception("Simulated storage failure on customer register");
            }

            @Override
            public void storeMerchant(String key, MerchantDTO merchant) throws Exception {
                throw new Exception("Simulated storage failure on merchant register");
            }
        };
    }

    @When("I attempt to register a customer with first name {string}, last name {string}, CPR {string}, and bank account {string}")
    public void i_attempt_to_register_a_customer_with_first_name_last_name_cpr_and_bank_account(String string, String string2, String string3, String string4) {
        CustomerService faultyCustomerService = new CustomerService(mockStorage);
        registeredCustomer = new CustomerDTO(string, string2, string3, string4);
        customerId = faultyCustomerService.register(registeredCustomer);
    }

    @When("I attempt to register a merchant with first name {string}, last name {string}, CPR {string}, and bank account {string}")
    public void i_attempt_to_register_a_merchant_with_first_name_last_name_cpr_and_bank_account(String string, String string2, String string3, String string4) {
       MerchantService faultyMerchantService = new MerchantService(mockStorage);
        registeredMerchant = new MerchantDTO(string, string2, string3, string4);
        merchantId = faultyMerchantService.register(registeredMerchant);
    }

    @Then("the registration fails with an error")
    public void the_registration_fails_with_an_error() {
        assertNull(customerId, "Customer ID should be null due to registration failure");
    }
    @Given("a mocked account storage that fails on deregister")
    public void a_mocked_account_storage_that_fails_on_deregister() {
        // First initialize the services normally so they can be used
        StorageHandler.getInstance().clearAll();
        customerService = new CustomerService();
        merchantService = new MerchantService();
        
        // Create mock storage for failure testing
        mockStorage = new StorageHandler(){
            @Override
            public CustomerDTO removeCustomer(String customerId) {
                throw new RuntimeException("Simulated storage failure on customer deregister");
            }

            @Override
            public MerchantDTO removeMerchant(String merchantId) {
                throw new RuntimeException("Simulated storage failure on merchant deregister");
            }
        };
    }
    @When("I attempt to deregister the merchant with the merchant id")
    public void i_attempt_to_deregister_the_merchant_with_the_merchant_id() {
        MerchantService faultyMerchantService = new MerchantService(mockStorage);
        deregisterSuccess = faultyMerchantService.deregister(merchantId);
        
    }
    @Then("the deregistration fails with an error")
    public void the_deregistration_fails_with_an_error() {
        assertEquals("Failure!", deregisterSuccess, "Deregistration should fail due to storage error");
    }
    @When("I attempt to deregister the customer with the customer id")
    public void i_attempt_to_deregister_the_customer_with_the_customer_id() {
        CustomerService faultyCustomerService = new CustomerService(mockStorage);
        deregisterSuccess = faultyCustomerService.deregister(customerId);
    }
}
