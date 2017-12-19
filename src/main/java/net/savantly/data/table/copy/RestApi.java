package net.savantly.data.table.copy;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest")
public class RestApi {
	
	@Autowired
	private DataTransferExecutor executor;
	
	@Value("${source.datasource.url}")
	private String source;
	@Value("${target.datasource.url}")
	private String target;
	
	@RequestMapping("/execute")
	public void executeTransfer(@RequestBody TransferOptions options) {
		executor.execute(options.getSelectStatement(), options.getInsertStatement(), options.getMappings());
	}
	
	@RequestMapping("/info")
	public Map<String, String> dbInfo(){
		Map<String, String> response = new HashMap<String, String>();
		response.put("sourceDb", source);
		response.put("targetDb", target);
		return response;
	}

}
