package net.savantly.data.table.copy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TargetFixture {
	
	private static final Logger log = LoggerFactory.getLogger(TargetFixture.class);
	
	private NamedParameterJdbcTemplate jdbc;
	
	public TargetFixture(NamedParameterJdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}
	
	public void createData() {
	       log.info("Creating tables in target db");

	        jdbc.execute("DROP TABLE customers IF EXISTS", (ps)->{return ps.execute();});
	        jdbc.execute("CREATE TABLE customers(" +
	                "id INT, first_name VARCHAR(255), last_name VARCHAR(255))", (ps)->{return ps.execute();});

	        // Split up the array of whole names into an array of first/last names
	        Map<String, Object>[] splitUpNames = (Map<String, Object>[])new Map[] {};
	        Arrays.asList("11 John Woo", "12 Jeff Dean").stream()
	                .map(name -> {
	                	String[] parts = name.split(" ");
	                	HashMap<String, Object> map = new HashMap<String, Object>();
	                	map.put("id", parts[0]);
	                	map.put("first_name", parts[1]);
	                	map.put("last_name", parts[2]);
	                	return map;
	                }).collect(Collectors.toList()).toArray(splitUpNames);

	        // Use a Java 8 stream to print out each tuple of the list
	        Arrays.stream(splitUpNames).forEach(name -> log.info(String.format("Inserting customer record for %s %s %s", name.get("id"), name.get("first_name"), name.get("last_name"))));

	        
	        // Uses JdbcTemplate's batchUpdate operation to bulk load data
	        jdbc.batchUpdate("INSERT INTO customers(id, first_name, last_name) VALUES (?,?,?)", splitUpNames);
	}
}
