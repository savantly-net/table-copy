package net.savantly.data.table.copy;

import java.util.concurrent.ThreadFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication(exclude= {
		DataSourceAutoConfiguration.class,
		DataSourceTransactionManagerAutoConfiguration.class})
@EnableAsync
public class TableCopyApplication {


	public static void main(String[] args) {
		SpringApplication.run(TableCopyApplication.class, args);
	}
	
	@Bean
	public ThreadPoolTaskExecutor taskExecutor() {
	    final ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
	    taskExecutor.setThreadFactory(threadFactory());
	    return taskExecutor;
	}

	public ThreadFactory threadFactory() {
	    return new CustomizableThreadFactory("sql-task-");
	}
}
