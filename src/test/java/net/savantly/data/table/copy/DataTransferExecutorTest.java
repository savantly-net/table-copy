package net.savantly.data.table.copy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.sql.DataSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DataTransferExecutorTest {
	
	private final static Logger log = LoggerFactory.getLogger(DataTransferExecutorTest.class);
	
	@Autowired
	@Qualifier("sourceJdbc")
	private JdbcTemplate sourceJdbc;
	
	@Autowired
	@Qualifier("targetJdbc")
	private JdbcTemplate targetJdbc;
	
	@Autowired
	SourceFixture sourceFixture;
	
	@Autowired
	TargetFixture targetFixture;
	
	@Test
	public void test() throws NoSuchMethodException, SecurityException {
		
		final String insertStatement = "INSERT INTO customers(id, first_name, last_name) VALUES (?,?,?)";

		sourceFixture.createData();
		targetFixture.createData();
		
		Method insertMethod = targetJdbc.getClass().getMethod("update", String.class, Object[].class);

        DynamicTable table = new DynamicTable();
        log.info("Querying for customer records where first_name = 'Josh':");
        sourceJdbc.query(
                "SELECT id, first_name, last_name FROM customers WHERE first_name = ?", new Object[] { "Josh" },
                (rs, rowNum) -> new DynamicRecord(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
        ).stream().forEach(record -> {
        	log.info("inserting records into target DB: {}", record);
        	Object[] args = new Object[record.size()+1];
        	args[0] = insertStatement;
        	for (int index = 0; index < record.size(); index++) {
				args[index+1] = record.get(index);
			}
        	try {
				insertMethod.invoke(targetJdbc, args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("{}", e);
			}
        });
	}
	
	@Configuration
	@ComponentScan
	static class TestContext {
		
		@Primary
		@Bean("sourceDataSource")
		public DataSource sourceDataSource() {
			return DataSourceBuilder.create()
					.driverClassName("org.hsqldb.jdbc.JDBCDriver")
					.url("jdbc:hsqldb:mem:testSource").username("sa").build();
		}
		
		@Bean("targetDataSource")
		public DataSource targetDataSource() {
			return DataSourceBuilder.create()
					.driverClassName("org.hsqldb.jdbc.JDBCDriver")
					.url("jdbc:hsqldb:mem:testTarget").username("sa").build();
		}
		
		@Bean("sourceJdbc")
		public JdbcTemplate sourceJdbc(@Qualifier("sourceDataSource") DataSource dataSource) {
			return new JdbcTemplate(dataSource);
		}
		
		@Bean("targetJdbc")
		public JdbcTemplate targetJdbc(@Qualifier("targetDataSource") DataSource dataSource) {
			return new JdbcTemplate(dataSource);
		}
		
		
	}

}
