package com.example.wealthtech_ledger;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class WealthtechLedgerApplication {

	private static final Logger log = LoggerFactory.getLogger(WealthtechLedgerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(WealthtechLedgerApplication.class, args);
	}

	/*@Bean            seeds test data on startup
	public CommandLineRunner demo (TransactionRepository repository){
		return (args) -> {
			// saves a few
			repository.save(new Transaction("run1", new BigDecimal("100"), "ali"));
			repository.save(new Transaction("run2", new BigDecimal("200"), "ben"));
			repository.save(new Transaction("run3", new BigDecimal("300"), "chou"));

			//fetch all
			log.info("Transactions found with findAll(): ");
			log.info("----------------------");
			for (Transaction transaction : repository.findAll()){
				log.info(transaction.toString());
			}

			// fetch by id
			Transaction transaction = repository.findById(1L);
			log.info("Transaction found with findById(1L):");
			log.info("--------------------------------");
			log.info(transaction.toString());
			log.info("");

			//fetch by name
			log.info("Transaction found with findByOwner('ali') ");
			repository.findByOwner("ali").forEach(ali -> {
				log.info(ali.toString());
				// for (Customer bauer : repository.findByLastName("Bauer")) {
				// 	log.info(bauer.toString());
				// }
				log.info("");
			});


		};
	}*/


}
