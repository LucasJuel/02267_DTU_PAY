package org.g10;
import dtu.ws.fastmoney.*;
import io.cucumber.java.After;
import org.g10.DTO.PaymentDTO;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.g10.services.PaymentService;
import org.g10.utils.StorageHandler;
import org.g10.services.ReportingService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
/**
 @author gh05tdog
 **/

public class PaymentConsumerSteps {
    private PaymentDTO paymentDTO = new PaymentDTO();
    private PaymentDTO paymentDTO2;
    private final BankService bank = new BankService_Service().getBankServicePort();
    private final String apiKey = "kayak2098";
    private final User customerUser = new User();
    private final User merchantUser = new User();
    private String customerAccount;
    private String merchantAccount;
    private final List<String> accounts = new ArrayList<>();
    private final StorageHandler storageHandler = StorageHandler.getInstance();
    private PaymentService paymentService = new PaymentService();
    private final ReportingService reportingService = new ReportingService();
    private BankService mockBankService;
    private String paymentResponse;
    private String paymentResponse2;
    private List<Map<String, Object>> paymentList;

    @Before
    public void setup() throws IOException, TimeoutException {
        // factory = new ConnectionFactory();
        // factory.setHost("localhost");
        // factory.setPort(5672);
        // factory.setUsername("guest");
        // factory.setPassword("guest");
        // connection = factory.newConnection();
        // channel = connection.createChannel();
        // channel.queueDeclare("payment.requests", true, false, false, null);
        
        // thread = new Thread(() -> {
        //     app = new PaymentServiceApplication();
        //     PaymentServiceApplication.main(new String[]{});
        // });
        // thread.start();
        // try {
        //     Thread.sleep(2000); // Wait for the service to start
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // }
    }


    @Given("the customer firstname {string} lastname {string} with cpr {string} is registered with the bank with an initial balance of {int} kr")
    public void theCustomerFirstnameLastnameWithCprIsRegisteredWithTheBankWithAnInitialBalanceOfKr(String arg0, String arg1, String arg2, int initBalance) throws BankServiceException_Exception {

        customerUser.setFirstName(arg0);
        customerUser.setLastName(arg1);
        customerUser.setCprNumber(arg2);

        customerAccount = bank.createAccountWithBalance(apiKey, customerUser, new BigDecimal(initBalance));
        accounts.add(customerAccount);
        assert (bank.getAccount(customerAccount).getBalance().equals(new BigDecimal(initBalance)));
    }
    @And("the merchant firstname {string} lastname {string} with cpr {string} is registered with the bank with an initial balance of {int} kr")
    public void theMerchantFirstnameLastnameWithCprIsRegisteredWithTheBankWithAnInitialBalanceOfKr(String arg0, String arg1, String arg2, int initBalance) throws BankServiceException_Exception {

        merchantUser.setFirstName(arg0);
        merchantUser.setLastName(arg1);
        merchantUser.setCprNumber(arg2);

        BigDecimal balance = new BigDecimal(initBalance);
        
        merchantAccount = bank.createAccountWithBalance(apiKey, merchantUser, balance);

        accounts.add(merchantAccount);

        assert (Objects.equals(bank.getAccount(merchantAccount).getBalance(), balance));
    }

    @Given("a transaction between the customer and the merchant is initiated with amount {double} kr and message {string}")
    public void aTransactionBetweenTheCustomerAndTheMerchantIsInitiatedWithAmountKr(Double arg0, String message) {
        BigDecimal val = new BigDecimal(arg0);
        paymentDTO.setAmount(val);
        paymentDTO.setCustomerAccountId(customerAccount);
        paymentDTO.setMerchantAccountId(merchantAccount);
        paymentDTO.setMessage(message);
    }



    @When("I register the payment with the payment service") 
    public void i_register_the_payment_with_the_payment_service() {
        paymentResponse = paymentService.register(paymentDTO);
        
    }

    @Then("the payment service should respond with a success message")
    public void the_payment_service_should_respond_with_a_success_message() {

        assertEquals("Success!", paymentResponse);
    }

    @And("The customer has balance {double} on their bank account and merchant has balance {double}")
    public void theCustomerHasBalanceDoubleOnTheirBankAccountAndMerchantHasBalanceDouble(double customerAmount, double merchantAmount) throws BankServiceException_Exception {
        BigDecimal customerBalance = bank.getAccount(customerAccount).getBalance();
        BigDecimal merchantBalance = bank.getAccount(merchantAccount).getBalance();


        assertEquals(customerBalance, new BigDecimal(customerAmount));
        assertEquals(merchantBalance, new BigDecimal(merchantAmount));

    }

    @Then("the payment event should be stored in the storage handler with correct details")
    public void the_payment_event_should_be_stored_in_the_storage_handler_with_correct_details() {
        Map<String, String> expectedData = Map.of(
            "merchantId", merchantAccount,
            "customerId", customerAccount,
            "amount", paymentDTO.getAmount().toString(),
            "message", paymentDTO.getMessage()
        );

        List<Map<String, Object>> payments = storageHandler.readPayments();
        assertNotNull(payments);
        assertEquals(1, payments.size());

        Map<String, Object> storedPayment = payments.get(0);

        
        assertEquals(expectedData.get("merchantId"), storedPayment.get("merchantId"));
        assertEquals(expectedData.get("customerId"), storedPayment.get("customerId"));
        assertEquals(new BigDecimal(expectedData.get("amount")), storedPayment.get("amount"));
        assertEquals(expectedData.get("message"), storedPayment.get("message"));
    }

    @Then("the payment service should respond with a failure message indicating invalid amount")
    public void the_payment_service_should_respond_with_a_failure_message_indicating_invalid_amount() {
        assertEquals("{\"error\": \"Payment amount must be greater than zero.\"}", paymentResponse);
    }

    @Given("a transaction between the customer and the merchant is initiated with amount {int} kr and no message")
    public void a_transaction_between_the_customer_and_the_merchant_is_initiated_with_amount_kr_and_no_message(Integer int1) {
        BigDecimal val = new BigDecimal(int1);
        paymentDTO.setAmount(val);
        paymentDTO.setCustomerAccountId(customerAccount);
        paymentDTO.setMerchantAccountId(merchantAccount);
        paymentDTO.setMessage(null);
    }

    @Then("the payment message should default to standard message")
    public void the_payment_message_should_default_to_standard_message() {
        String expectedMessage = "Payment from " + customerAccount + " to " + merchantAccount;
        //Find the stored payment
        List<Map<String, Object>> payments = storageHandler.readPayments();
        assertNotNull(payments);
        assertEquals(1, payments.size());
        Map<String, Object> storedPayment = payments.get(0);
        String storedMessage = (String) storedPayment.get("message");
        assertEquals(expectedMessage, storedMessage);
    }

    @Then("the payment service should respond with a failure message indicating error")
    public void the_payment_service_should_respond_with_a_failure_message_indicating_error() {
        paymentResponse = paymentService.register(paymentDTO);
        assertEquals("{\"error\": \"Failed to process payment:\"}", paymentResponse);
    }

    @Given("the bank service is mocked to throw a bank exception on transfer")
    public void the_bank_service_is_mocked_to_throw_a_bank_exception_on_transfer() { 
        mockBankService = new BankService() {
            @Override
            public void transferMoneyFromTo(String fromAccountId, String toAccountId, BigDecimal amount, String message) throws BankServiceException_Exception {
                throw new BankServiceException_Exception("Mocked bank exception", new BankServiceException());
            }

            @Override
            public Account getAccount(String accountId) throws BankServiceException_Exception {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getAccount'");
            }

            @Override
            public Account getAccountByCprNumber(String cpr) throws BankServiceException_Exception {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getAccountByCprNumber'");
            }

            @Override
            public String createAccountWithBalance(String bankApiKey, User user, BigDecimal balance)
                    throws BankServiceException_Exception {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'createAccountWithBalance'");
            }

            @Override
            public void retireAccount(String bankApiKey, String accountId) throws BankServiceException_Exception {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'retireAccount'");
            }

            @Override
            public List<AccountInfo> getAccounts() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getAccounts'");
            }
        };
        // Inject the mock bank service into the payment service
        paymentService = new PaymentService(mockBankService, storageHandler);
    }

    @Given("another transaction between the customer and the merchant is initiated with amount {int} kr and message {string}")
    public void another_transaction_between_the_customer_and_the_merchant_is_initiated_with_amount_kr_and_message(Integer int1, String string) {
        BigDecimal val = new BigDecimal(int1);
        paymentDTO2 = new PaymentDTO();
        paymentDTO2.setAmount(val);
        paymentDTO2.setCustomerAccountId(customerAccount);
        paymentDTO2.setMerchantAccountId(merchantAccount);
        paymentDTO2.setMessage(string);
    }
    @When("I register both payments with the payment service")
    public void i_register_both_payments_with_the_payment_service() {
        paymentResponse = paymentService.register(paymentDTO);
        paymentResponse2 = paymentService.register(paymentDTO2);
    }
    @Then("I request all payments for merchant {string}")
    public void i_request_all_payments_for_merchant(String string) {
        paymentList = reportingService.getAllPayments(new org.g10.DTO.ReportDTO(merchantAccount, "merchant"));

    }
    @Then("I should receive a list containing both payment events with correct details")
    public void i_should_receive_a_list_containing_both_payment_events_with_correct_details() {
        assertNotNull(paymentList);
        assertEquals(2, paymentList.size());

        Map<String, Object> firstPayment = paymentList.get(0);
        Map<String, Object> secondPayment = paymentList.get(1);

        assertEquals(merchantAccount, firstPayment.get("merchantId"));
        assertEquals(customerAccount, firstPayment.get("customerId"));
        assertEquals(paymentDTO.getAmount(), firstPayment.get("amount"));
        assertEquals(paymentDTO.getMessage(), firstPayment.get("message"));

        assertEquals(merchantAccount, secondPayment.get("merchantId"));
        assertEquals(customerAccount, secondPayment.get("customerId"));
        assertEquals(paymentDTO2.getAmount(), secondPayment.get("amount"));
        assertEquals(paymentDTO2.getMessage(), secondPayment.get("message"));
    }


    @After
    public void deleteAccounts() throws BankServiceException_Exception {
        for (String account : accounts) {
            bank.retireAccount(apiKey, account);
            
        }
        storageHandler.clear();
        accounts.clear();
    }
}
