package net.savantly.data.table.copy;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SourceFixture {
	
	private static final Logger log = LoggerFactory.getLogger(SourceFixture.class);
	
	@Autowired
	@Qualifier("sourceJdbc")
	private NamedParameterJdbcTemplate jdbc;
	
	public void createData() {
	       log.info("Creating tables in source db");

	        jdbc.execute("DROP TABLE customers IF EXISTS", new PreparedStatementCallback<Boolean>() {

				@Override
				public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
					// TODO Auto-generated method stub
					return ps.execute();
				}
			});

	        jdbc.execute("CREATE TABLE customers(" +
	                "id INT, first_name VARCHAR(255), last_name VARCHAR(255))", new PreparedStatementCallback<Boolean>() {

				@Override
				public Boolean doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {
					// TODO Auto-generated method stub
					return ps.execute();
				}
			});

	        // Split up the array of whole names into an array of first/last names
	        List<HashMap<String, String>> splitUpNames = Arrays.asList("1 John Woo", "2 Jeff Dean", "3 Josh Bloch", "4 Josh Long").stream()
	                .map(name -> name.split(" "))
	                .map(r -> {
	                	HashMap<String, String> item = new HashMap<String, String>();
	                	item.put("id", r[0]);
	                	item.put("first_name", r[1]);
	                	item.put("last_name", r[2]);
	                	return item;
	                }).collect(Collectors.toList());
	        
	        HashMap[] mapArray = new HashMap[splitUpNames.size()];
	        splitUpNames.toArray(mapArray);

	        // Use a Java 8 stream to print out each tuple of the list
	        splitUpNames.forEach(name -> log.info("Inserting customer record for: {}", name));

	        // Uses JdbcTemplate's batchUpdate operation to bulk load data
	        jdbc.batchUpdate("INSERT INTO customers(id, first_name, last_name) VALUES (:id, :first_name, :last_name)", mapArray);
	}
}
