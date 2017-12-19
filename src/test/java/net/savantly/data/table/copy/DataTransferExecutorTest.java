package net.savantly.data.table.copy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class DataTransferExecutorTest {
	
	private final static Logger log = LoggerFactory.getLogger(DataTransferExecutorTest.class);
	
	@Autowired
	DataTransferExecutor executor;
	@Autowired
	SourceFixture sourceFixture;
	@Autowired
	TargetFixture targetFixture;
	
	
	@Test
	public void test() throws NoSuchMethodException, SecurityException {

		final String selectStatement = "SELECT id, first_name, last_name FROM customers WHERE first_name = 'Josh'";
		final String insertStatement = "INSERT INTO customers(id, first_name, last_name) VALUES (?,?,?)";
		
		sourceFixture.createData();
		targetFixture.createData();
		
		ColumnMapping[] mappings = new ColumnMapping[3];
		mappings[0] = new ColumnMapping("id");
		mappings[1] = new ColumnMapping("first_name");
		mappings[2] = new ColumnMapping("last_name");
		
        log.info("Querying for customer records where first_name = 'Josh':");
        executor.execute(selectStatement, insertStatement, mappings);
	}

	@Configuration
	@ComponentScan
	static class TestContext { }

}
