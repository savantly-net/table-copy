package net.savantly.data.table.copy;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataTransferExecutor {
	
	private final static Logger log = LoggerFactory.getLogger(DataTransferExecutor.class);
	
	private int batchSize = 10;
	
	@Autowired
	@Qualifier("sourceJdbc")
	private NamedParameterJdbcTemplate sourceJdbc;
	
	@Autowired
	@Qualifier("targetJdbc")
	private NamedParameterJdbcTemplate targetJdbc;

	@Async
	public CompletableFuture<List<Integer>> execute(String selectStatement, String insertStatement, ColumnMapping[] mappings) {
		CompletableFuture<List<Integer>> future = new CompletableFuture<>();
		Map<String, ?> sourceParameters = new HashMap<>();
        List<Integer> sqlResult = sourceJdbc.query(selectStatement, sourceParameters, (rs, rowNum) -> {
        	int total = 0;
        	List<Map<String, Object>> batch = new ArrayList<Map<String, Object>>();
        	boolean hasMore = createParameters(batch, mappings, rs);

        	do {
        		while(hasMore && batch.size() < batchSize) {
        			hasMore = createParameters(batch, mappings, rs);
        		}
        		try {
        		int[] batchResults = sendBatch(batch, insertStatement);
        		for (int i : batchResults) {
					total += i;
				}
        		} catch (Exception e) {
        			future.completeExceptionally(e);
        			// rethrow exception to exit
        			throw e;
        		}
        	}
        	while (hasMore);
        	
        	return total;
        }).stream().collect(Collectors.toList());
        future.complete(sqlResult);
        return future;
	}
	
	private boolean createParameters(List<Map<String, Object>> batch, ColumnMapping[] mappings, ResultSet rs) throws SQLException {
    	Map<String, Object> parameters = new HashMap<String, Object>(mappings.length);
    	for (int i = 0; i < mappings.length; i++) {
    		try {
				parameters.put(mappings[i].getTargetVariableName(), extractValue(rs, mappings[i]));
			} catch (IOException e) {
				log.error("", e);
			}
		}
    	batch.add(parameters);
    	return rs.next();
	}
	
	@SuppressWarnings("finally")
	@Transactional
	private int[] sendBatch(List<Map<String, Object>> batch, String sql) {
    	log.debug("inserting batch into target DB: {}", batch);
    	int[] result = new int[] {};
    	try {
        	Map<String, Object>[] array = (Map<String, Object>[])new Map[batch.size()];
        	batch.toArray(array);
			result = targetJdbc.batchUpdate(sql, array);
		} catch (Exception e) {
			log.error("Failed to perform batch update in target DB: {}", batch, e);
			// rethrow exception
			throw new RuntimeException(e);
		} finally {
			batch.clear();
			return result;
		}
	}

	private Object getSqlParameterValue(int sqlType, Object value) throws IOException {
		if (InputStream.class.isInstance(value)) {
			byte[] bytes = IOUtils.toByteArray(((InputStream)value)); 
			SqlLobValue lobValue = new SqlLobValue(bytes);
			return new SqlParameterValue(sqlType, lobValue);
		}
		return new SqlParameterValue(sqlType, value);
	}

	private Object extractValue(ResultSet rs, ColumnMapping mapping) throws SQLException, IOException {
		switch (mapping.getSourceColumnType()) {
		case BLOB:
			return getSqlParameterValue(mapping.getSqlType(), rs.getBlob(mapping.getSourceColumnName()).getBinaryStream());
		case CLOB:
			return getSqlParameterValue(mapping.getSqlType(), rs.getClob(mapping.getSourceColumnName()).getCharacterStream());
		case INT:
			return getSqlParameterValue(mapping.getSqlType(), rs.getInt(mapping.getSourceColumnName()));
		case LONG:
			return getSqlParameterValue(mapping.getSqlType(), rs.getLong(mapping.getSourceColumnName()));
		case STRING:
			return getSqlParameterValue(mapping.getSqlType(), rs.getString(mapping.getSourceColumnName()));
		default:
			return rs.getString(mapping.getSourceColumnName());
		}
	}
}
