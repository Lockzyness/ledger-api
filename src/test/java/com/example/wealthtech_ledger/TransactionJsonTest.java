/*package com.example.wealthtech_ledger;

import com.jayway.jsonpath.JsonPath;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class TransactionJsonTest {

    @Autowired
    private JacksonTester<Transaction> json;

    @Autowired
    private JacksonTester<Transaction[]> jsonList;

    private Transaction[] transactions;

    @BeforeEach
    void setUp() {
        transactions = Arrays.array(
                new Transaction(99L,"first test" ,new BigDecimal("123.45"), "sarah1"),
                new Transaction(100L, "sec test", new BigDecimal("1.00"), "sarah1"),
                new Transaction(101L, "third test", new BigDecimal("150.00"), "sarah1"));
    }

    @Test
    void transactionSerializationTest () throws IOException {
        Transaction transaction = new Transaction(99L, "serialization", new BigDecimal("100"), "sarah1");

        assertThat(json.write(transaction)).isStrictlyEqualToJson("expected.json");
        assertThat(json.write(transaction)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(transaction)).extractingJsonPathNumberValue("@.id").isEqualTo(99);
        assertThat(json.write(transaction)).hasJsonPathStringValue("@.description");
        assertThat(json.write(transaction)).extractingJsonPathStringValue("@.description").isEqualTo("serialization");
        assertThat(json.write(transaction)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(transaction)).extractingJsonPathNumberValue("@.amount").isEqualTo(100);
    }

    @Test
    void transactionDeserializationTest ()throws IOException{
        String expected = """
               {"id": 99, "description" : "serialization", "amount": 100, "owner": "sarah1" }
        """;

        assertThat(json.parse(expected)).isEqualTo(new Transaction(99L, "serialization", new BigDecimal(100), "sarah1"));

        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).description()).isEqualTo("serialization");
        assertThat(json.parseObject(expected).amount()).isEqualTo(new BigDecimal("100"));


    }

    @Test
    void transactionListSerializationTest() throws IOException{
        assertThat(jsonList.write(transactions)).isStrictlyEqualToJson("list.json");
    }

    @Test
    void transactionListDeserializationTest() throws IOException{
        String expected = """
                [
                  {"id": 99, "description": "first test", "amount": 123.45, "owner": "sarah1"},
                  {"id": 100, "description": "sec test", "amount": 1.00, "owner": "sarah1" },
                  {"id": 101, "description": "third test", "amount": 150.00, "owner": "sarah1" }
                ]
                """;

        assertThat(jsonList.parse(expected)).isEqualTo(transactions);
    }

}*/
