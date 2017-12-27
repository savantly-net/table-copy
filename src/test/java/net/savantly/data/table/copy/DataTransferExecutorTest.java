package net.savantly.data.table.copy;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(webEnvironment=WebEnvironment.MOCK)
@TestPropertySource("classpath:/application-test.properties")
@RunWith(SpringRunner.class)
public class DataTransferExecutorTest {
	
	private final static Logger log = LoggerFactory.getLogger(DataTransferExecutorTest.class);
	
	@Autowired
	DataTransferExecutor executor;
	@Autowired
	SourceFixture sourceFixture;
	@Autowired
	TargetFixture targetFixture;

	@Before
	public void before() {
		//restTemplate.postForObject("/rest/db", new HashMap<String,  String>(), Map.class);
	}
	
	@Test
	public void test() throws NoSuchMethodException, SecurityException, InterruptedException, ExecutionException, TimeoutException {

		final String selectStatement = "SELECT id, first_name, last_name FROM customers WHERE first_name = 'Josh'";
		final String insertStatement = "INSERT INTO customers(id, first_name, last_name) VALUES (:id,:first_name,:last_name)";
		
		sourceFixture.createData();
		targetFixture.createData();
		
		ColumnMapping[] mappings = new ColumnMapping[3];
		mappings[0] = new ColumnMapping("id", ColumnType.INT);
		mappings[1] = new ColumnMapping("first_name");
		mappings[2] = new ColumnMapping("last_name");
		
        log.info("Querying for customer records where first_name = 'Josh':");
        CompletableFuture<List<Integer>> future = executor.execute(selectStatement, insertStatement, mappings);
        List<Integer> results = future.get(90, TimeUnit.SECONDS);

        Assert.assertTrue("Two records shouldhave been inserted", results.stream().allMatch(i -> i.intValue() == 2));
	}

	@Configuration
	@Import(TestConfiguration.class)
	static class TestContext  {


	}

}
