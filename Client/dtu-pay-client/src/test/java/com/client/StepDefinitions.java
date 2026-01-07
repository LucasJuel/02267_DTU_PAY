package com.client;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StepDefinitions {
    @Given("The program runs")
                public void program_runs() {
            System.out.println("The program runs");


        }
        @Then("The answer is Hello")
        public void the_answer_is_hello() {
            String result = Main.helloFromMain();
            assert(result.equals("Hello"));
            System.out.println("Testen bestået: Svaret er Hello");
        }
}

