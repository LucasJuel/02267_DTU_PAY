package org.g10;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
public class TokenSteps {
    @Given("^A customer has a token$")
    public void aCustomerHasAToken() {
        assert(true);
    }


    @Then("^i print Hello world$")
    public void iPrintHelloWorld() {
        String helloWorld = "Hello World";
        System.out.println(helloWorld);
        assert(helloWorld.equals("Hello World"));
    }
}
