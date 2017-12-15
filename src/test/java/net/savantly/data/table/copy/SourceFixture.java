package net.savantly.data.table.copy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SourceFixture {
	
	private static final Logger log = LoggerFactory.getLogger(SourceFixture.class);
	
	@Autowired
	@Qualifier("sourceJdbc")
	private JdbcTemplate jdbc;
	
	public void createData() {
	       log.info("Creating tables in source db");

	        jdbc.execute("DROP TABLE customers IF EXISTS");
	        jdbc.execute("CREATE TABLE customers(" +
	                "id INT, first_name VARCHAR(255), last_name VARCHAR(255))");

	        // Split up the array of whole names into an array of first/last names
	        List<Object[]> splitUpNames = Arrays.asList("1 John Woo", "2 Jeff Dean", "3 Josh Bloch", "4 Josh Long").stream()
	                .map(name -> name.split(" "))
	                .collect(Collectors.toList());

	        // Use a Java 8 stream to print out each tuple of the list
	        splitUpNames.forEach(name -> log.info(String.format("Inserting customer record for %s %s %s", name[0], name[1], name[2])));

	        // Uses JdbcTemplate's batchUpdate operation to bulk load data
	        jdbc.batchUpdate("INSERT INTO customers(id, first_name, last_name) VALUES (?,?,?)", splitUpNames);
	}
}
