package io.github.lockzyness.ledger;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
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

@SpringBootTest(properties = "spring.sql.init.data-locations=classpath:test-data.sql", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LedgerIntegrationTests {

	@Autowired
	TestRestTemplate restTemplate;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionRepository transactionRepository;

	@Test @DirtiesContext
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
		Account savedSarah = accountRepository.save(new Account("sarah1", new BigDecimal("1000")));
		Account savedJohn = accountRepository.save(new Account("john", new BigDecimal("200")));

		NewTransactionRequest newTransactionRequest = new NewTransactionRequest("test", new BigDecimal("100"), savedSarah.getId(), savedJohn.getId());
		ResponseEntity<Void> createResponse = restTemplate.withBasicAuth("sarah1", "abc123").postForEntity("/transactions", newTransactionRequest, Void.class );
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewTranscation = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity(locationOfNewTranscation, String.class);
		Account updatedSarah = accountRepository.findById(savedSarah.getId()).orElseThrow();
		Account updatedJohn = accountRepository.findById(savedJohn.getId()).orElseThrow();
		assertThat(updatedSarah.getBalance()).isEqualByComparingTo("900.00");
		assertThat(updatedJohn.getBalance()).isEqualByComparingTo("300.00");
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number amt = documentContext.read("$.amount");
		assertThat(amt).isEqualTo(100);
	}

	@Test
	@DirtiesContext
	void shouldRejectIfInsufficientFund(){
		Account savedSarah = accountRepository.save(new Account("sarah1", new BigDecimal("50")));
		Account savedJohn = accountRepository.save(new Account("john", new BigDecimal("200")));

		NewTransactionRequest newRequest = new NewTransactionRequest("should fail", new BigDecimal("1000"), savedSarah.getId(), savedJohn.getId());
		ResponseEntity<Void> createResponse = restTemplate.withBasicAuth("sarah1", "abc123").postForEntity("/transactions", newRequest, Void.class);

		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);

	}

	@Test
	void shouldReturnAllTransactionsWhenListIsRequested(){
		ResponseEntity <String> responseEntity = restTemplate.withBasicAuth("sarah1", "abc123").getForEntity("/transactions", String.class);

		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

		DocumentContext documentContext = JsonPath.parse(responseEntity.getBody());

		int transactions = documentContext.read("$.length()");
		assertThat(transactions).isEqualTo(3);

		JSONArray ids = documentContext.read("$[*].id");
		//assertThat(ids).containsExactlyInAnyOrder(99,100,101);
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
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("bad1", "abc123")
				.getForEntity("/transactions/99", String.class);
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
	void recipientCanViewTransactionSentToThem(){
		// transaction 103: john (account 2) -> kumar2 (account 3)
		ResponseEntity<String> response = restTemplate.withBasicAuth("kumar2", "qwe").getForEntity("/transactions/103", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

		Number id = JsonPath.parse(response.getBody()).read("$.id");
		assertThat(id).isEqualTo(103);
	}

	@Test
	void recipientCannotViewOtherPeoplesTransactions(){
		// kumar2 receives transaction 103, but is not a party to 99 (sarah1 -> john)
		ResponseEntity<String> response = restTemplate.withBasicAuth("kumar2", "qwe").getForEntity("/transactions/99", String.class);
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
		ResponseEntity<Void> deleteResponse = restTemplate.withBasicAuth("sarah1", "abc123")
				.exchange("/transactions/9999", HttpMethod.DELETE, null, Void.class);

		ResponseEntity<String> getResponse = restTemplate.withBasicAuth("sarah1", "abc123")
				.getForEntity("/transactions/9999", String.class);
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
				.withBasicAuth("kumar2", "qwe")
				.getForEntity("/transactions/102", String.class);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

	}

	@Test @DirtiesContext
	void shouldCreateAnAccount(){
		NewAccountRequest newAccountRequest = new NewAccountRequest("jie", "jie123");
		ResponseEntity<Void> createResponse = restTemplate.postForEntity("/accounts", newAccountRequest ,Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

		URI locationOfNewAccount = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getRespone = restTemplate.withBasicAuth(newAccountRequest.owner(),
				newAccountRequest.password()).getForEntity(locationOfNewAccount, String.class);
		assertThat(getRespone.getStatusCode()).isEqualTo(HttpStatus.OK);
		DocumentContext documentContext = JsonPath.parse(getRespone.getBody());
		String owner = documentContext.read("$.owner");
		Number bal = documentContext.read("$.balance");
		assertThat(owner).isNotNull();
		assertThat(bal.doubleValue()).isZero();
	}

	@Test @DirtiesContext
	void shouldNotTransferNegative(){
		NewTransactionRequest neg = new NewTransactionRequest("negative", new BigDecimal("-100"), 1L, 3L);
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("sarah1" , "abc123")
				.postForEntity("/transactions", neg ,Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/accounts/1", String.class);
		Number balance = JsonPath.parse(getResponse.getBody()).read("$.balance");
		assertThat(new BigDecimal(balance.toString())).isEqualByComparingTo("1000.00");
	}

	@Test @DirtiesContext
	void shouldRejectZeroAmount(){
		NewTransactionRequest emp = new NewTransactionRequest("empty", new BigDecimal("0"), 1L, 2L);

		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("sarah1" , "abc123")
				.postForEntity("/transactions", emp ,Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/accounts/1", String.class);
		Number balance = JsonPath.parse(getResponse.getBody()).read("$.balance");
		assertThat(new BigDecimal(balance.toString())).isEqualByComparingTo("1000.00");
	}

	@Test @DirtiesContext
	void shouldRejectNullAmount(){
		NewTransactionRequest emp = new NewTransactionRequest("empty", null, 1L, 2L);

		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("sarah1" , "abc123")
				.postForEntity("/transactions", emp ,Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/accounts/1", String.class);
		Number balance = JsonPath.parse(getResponse.getBody()).read("$.balance");
		assertThat(new BigDecimal(balance.toString())).isEqualByComparingTo("1000.00");
	}

	@Test @DirtiesContext
	void shouldRejectTransferToOwnAccount(){
		NewTransactionRequest own = new NewTransactionRequest("own",
				new BigDecimal("100"), 1L, 1L);
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("sarah1" , "abc123")
				.postForEntity("/transactions", own ,Void.class);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("sarah1", "abc123")
				.getForEntity("/accounts/1", String.class);
		Number balance = JsonPath.parse(getResponse.getBody()).read("$.balance");
		assertThat(new BigDecimal(balance.toString())).isEqualByComparingTo("1000.00");
	}

}
