package com.example.wealthtech_ledger;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.coyote.Response;
import org.json.JSONString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WealthtechLedgerApplicationTests {

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void shouldReturnATransactionWhenDataIsSaved() {

		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/transactions/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(response.getBody());

		Number id = documentContext.read("$.id");
		assertThat(id).isEqualTo(99);

	}

	@Test
	void shouldNotReturnATransactionWithUnknownId(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/transactions/1000", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
		assertThat(response.getBody()).isBlank();
	}

	@Test
	@DirtiesContext
	void shouldCreateATransaction(){
		Transaction newTransaction = new Transaction(null, "create test", new BigDecimal("100"), "sarah1");

		ResponseEntity<Void> createResponse = restTemplate.withBasicAuth("sarah1", "abc123").postForEntity("/transactions", newTransaction, Void.class );
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewTranscation = createResponse.getHeaders().getLocation();

		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity(locationOfNewTranscation, String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());

		Number id = documentContext.read("@.id");
		assertThat(id).isNotNull();

		Number amt = documentContext.read("$.amount");
		assertThat(amt).isEqualTo(100);
	}

	@Test
	void shouldReturnAllTransactionsWhenListIsRequested(){
		ResponseEntity <String> responseEntity = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/transactions", String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());

		int transactions = documentContext.read("$.length()");
		assertThat(transactions).isEqualTo(3);

		JSONArray ids = documentContext.read("$..id");
		assertThat(ids).containsExactlyInAnyOrder(99,100,101);
	}

	@Test
	void shouldReturnASortedPageOfTransaction(){
		ResponseEntity <String> response = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/transactions?page=0&size=1&sort=amount,desc", String.class);

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(page.size()).isEqualTo(1);

		Number amount = documentContext.read("$[0].amount");
		assertThat(new BigDecimal(amount.toString())).isEqualByComparingTo("150.00");
	}

	@Test
	void shouldNotReturnTransactionWhenCredentialsBad(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("bad1", "abc123").getForEntity("/transactions/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

		response = restTemplate.withBasicAuth("sarah1", "badpass").getForEntity("/transasctions/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldNotReturnTransactionWhenNotOwner(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("hank","dwa").getForEntity("/transactions/99", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void noAccessToTranasactionsTheyDontOwn(){
		ResponseEntity<String> response = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("transactions/102", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

	}

	@Test
	@DirtiesContext
	void shouldDeleteAExistingTransaction(){
		ResponseEntity<Void> response = restTemplate.withBasicAuth("sarah1", "abc123").exchange("/transactions/99", HttpMethod.DELETE, null, void.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/transactions/99", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteTransactionThatDoesntExist(){
		ResponseEntity<Void> deleteResponse = restTemplate.withBasicAuth("sarah1", "abc123").exchange("/transactions/9999", HttpMethod.DELETE, null, Void.class);

		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/transactions/9999", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotAllowDeletionOfTransactionsTheyDontOwn(){
		ResponseEntity<Void> deleteResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.exchange("/transactions/102", HttpMethod.DELETE, null, Void.class);
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

		// Add this code at the end of the test method:
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("kumar2", "xyz789")
				.getForEntity("/cashcards/102", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

	}
}
