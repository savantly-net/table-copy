package net.savantly.data.table.copy;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DataTransferExecutor {
	
	private final static Logger log = LoggerFactory.getLogger(DataTransferExecutor.class);
	
	@Autowired
	@Qualifier("sourceJdbc")
	private NamedParameterJdbcTemplate sourceJdbc;
	
	@Autowired
	@Qualifier("targetJdbc")
	private NamedParameterJdbcTemplate targetJdbc;

	@Async
	public CompletableFuture<List<Integer>> execute(String selectStatement, String insertStatement, ColumnMapping[] mappings) {
		Map<String, ?> sourceParameters = new HashMap<>();
        List<Integer> results = sourceJdbc.query(selectStatement, sourceParameters, (rs, rowNum) -> {
        	Map<String, Object> parameters = new HashMap<String, Object>(mappings.length);
        	for (int i = 0; i < mappings.length; i++) {
        		parameters.put(mappings[i].getTargetVariableName(), extractValue(rs, mappings[i]));
			}
        	return parameters;
        }).stream().map(record -> {
        	log.debug("inserting record into target DB: {}", record);
        	try {
				return targetJdbc.update(insertStatement, record);
			} catch (IllegalArgumentException e) {
				log.error("Failed to insert record in target DB: {}", record, e);
				return 0;
			}
        }).collect(Collectors.toList());
        return CompletableFuture.completedFuture(results);
	}
	
	
	private Object extractValue(ResultSet rs, ColumnMapping mapping) throws SQLException {
		switch (mapping.getSourceColumnType()) {
		case BLOB:
			return rs.getBlob(mapping.getSourceColumnName());
		case CLOB:
			return rs.getClob(mapping.getSourceColumnName());
		case INT:
			return rs.getInt(mapping.getSourceColumnName());
		case LONG:
			return rs.getLong(mapping.getSourceColumnName());
		case STRING:
			return rs.getString(mapping.getSourceColumnName());
		default:
			return rs.getString(mapping.getSourceColumnName());
		}
	}
}
