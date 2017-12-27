package net.savantly.data.table.copy.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;

import net.savantly.data.table.copy.DataSourceConfiguration;
import net.savantly.data.table.copy.DataTransferExecutor;
import net.savantly.data.table.copy.TransferOptions;

@SessionScope
@RestController
@RequestMapping("/rest")
public class TaskController {
	private static final Logger log = LoggerFactory.getLogger(TaskController.class);

	private static final String DATABASE_PROPERTIES_MISSING = "Database properties are not set";
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;
	@Autowired
	private ApplicationContext context;

	private List<CompletableFuture> futures = Collections.synchronizedList(new ArrayList<>());
	private Map<Integer, String> exceptions = Collections.synchronizedMap(new HashMap<Integer, String>());

	@RequestMapping("/execute")
	public Map<String, String> executeTransfer(@RequestBody TransferOptions options) {
		try {
			CompletableFuture<List<Integer>> future = getDataTransferExecutor().execute(options.getSelectStatement(),
					options.getInsertStatement(), options.getMappings());
			future.exceptionally((e) -> {
				exceptions.put(future.hashCode(), e.getLocalizedMessage());
				return Collections.emptyList();
			});
			futures.add(future);
			HashMap<String, String> response = new HashMap<String, String>();
			response.put("message", "Added task to queue");
			response.put("id", String.format("%s", future.hashCode()));
			return response;
		} catch (Exception ex) {
			log.error("", ex);
			throw new RuntimeException(ex);
		}
	}

	@RequestMapping("/info")
	public Map<String, Object> dbInfo() {
		try {
			DataSourceConfiguration dbConfig = getDataSourceConfiguration();
			Map<String, Object> response = new HashMap<String, Object>();
			response.put("sourceDb", hasSourceDataSource());
			response.put("targetDb", hasSourceDataSource());
			return response;
		} catch (Exception ex) {
			log.error("", ex);
			throw new RuntimeException(DATABASE_PROPERTIES_MISSING);
		}
	}

	@RequestMapping("/tasks")
	public Map<String, Object> tasks() {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("activeCount", taskExecutor.getActiveCount());
		response.put("poolSize", taskExecutor.getPoolSize());
		response.put("keepAliveSeconds", taskExecutor.getKeepAliveSeconds());

		List<Map<String, Object>> tasks = this.futures.stream().map(f -> {
			Map<String, Object> threadInfo = new HashMap<String, Object>();
			threadInfo.put("id", f.hashCode());
			threadInfo.put("done", f.isDone());
			threadInfo.put("cancelled", f.isCancelled());
			threadInfo.put("completedExceptionally", f.isCompletedExceptionally());
			threadInfo.put("message", exceptions.get(f.hashCode()));
			return threadInfo;
		}).collect(Collectors.toList());

		response.put("tasks", tasks);
		return response;
	}

	@RequestMapping("/tasks/clear")
	public Map<String, Object> clearCompletedTasks() {
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("message", "completed");

		List<CompletableFuture> done = this.futures.stream().filter(f -> f.isDone()).collect(Collectors.toList());
		this.futures.removeAll(done);
		done.forEach(f -> {
			this.exceptions.remove(f.hashCode());
		});

		return response;
	}

	private DataTransferExecutor getDataTransferExecutor() {
		DataSourceConfiguration config = getDataSourceConfiguration();
		return config.getExecutor();
	}

	private DataSourceConfiguration getDataSourceConfiguration() {
		return context.getBean(DataSourceConfiguration.class);
	}
	
	private boolean hasSourceDataSource() {
		return context.containsBean(DataSourceConfiguration.SOURCE_DATASOURCE_BEAN_NAME);
	}

	private boolean hasTargetDataSource() {
		return context.containsBean(DataSourceConfiguration.TARGET_DATASOURCE_BEAN_NAME);
	}
}
