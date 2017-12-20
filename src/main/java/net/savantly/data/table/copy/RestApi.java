package net.savantly.data.table.copy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest")
public class RestApi {
	
	@Autowired
	private DataTransferExecutor transferExecutor;
	@Autowired
	private ThreadPoolTaskExecutor taskExecutor;
	
	@Value("${source.datasource.url}")
	private String source;
	@Value("${target.datasource.url}")
	private String target;
	private List<CompletableFuture> futures = Collections.synchronizedList(new ArrayList<>());
	private Map<Integer, String> exceptions = Collections.synchronizedMap(new HashMap<Integer, String>());
	
	
	@RequestMapping("/execute")
	public Map<String, String> executeTransfer(@RequestBody TransferOptions options) {
		CompletableFuture<List<Integer>> future = transferExecutor.execute(options.getSelectStatement(), options.getInsertStatement(), options.getMappings());
		future.exceptionally((e) -> {
			exceptions.put(future.hashCode(), e.getLocalizedMessage());
			return Collections.emptyList();
		});
		futures.add(future);
		HashMap<String, String> response = new HashMap<String, String>();
		response.put("message", "Added task to queue");
		response.put("id", String.format("%s", future.hashCode()));
		return response;
	}
	
	@RequestMapping("/info")
	public Map<String, String> dbInfo(){
		Map<String, String> response = new HashMap<String, String>();
		response.put("sourceDb", source);
		response.put("targetDb", target);
		return response;
	}
	
	@RequestMapping("/tasks")
	public Map<String, Object> tasks(){
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
	public Map<String, Object> clearCompletedTasks(){
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("message", "completed");
		
		List<CompletableFuture> done = this.futures.stream().filter(f -> f.isDone()).collect(Collectors.toList());
		this.futures.removeAll(done);
		done.forEach(f -> {
			this.exceptions.remove(f.hashCode());
		});
		
		return response;
	}

}
