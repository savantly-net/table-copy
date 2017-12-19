package net.savantly.data.table.copy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DataTransferExecutor {
	
	private final static Logger log = LoggerFactory.getLogger(DataTransferExecutor.class);
	private final Method insertMethod;
	
	@Autowired
	@Qualifier("sourceJdbc")
	private JdbcTemplate sourceJdbc;
	
	@Autowired
	@Qualifier("targetJdbc")
	private JdbcTemplate targetJdbc;
	
	public DataTransferExecutor() throws NoSuchMethodException, SecurityException {
		insertMethod = JdbcTemplate.class.getMethod("update", new Class[]{String.class, Object[].class});
	}
	
	public void execute(String selectStatement, String insertStatement, ColumnMapping[] mappings) {
        sourceJdbc.query(selectStatement, new Object[0], (rs, rowNum) -> {
        	Object[] rsValues = new Object[mappings.length];
        	for (int i = 0; i < mappings.length; i++) {
				rsValues[i] = extractValue(rs, mappings[i]);
			}
        	return new DynamicRecord(rsValues);
        }).stream().forEach(record -> {
        	log.debug("inserting record into target DB: {}", record);
        	try {
        		Object[] args = new Object[record.size()];
        		for (int i = 0; i<record.size(); i++) {
					args[i] = record.get(i);
				}
        		log.debug("invoking insertion method via reflection: {} \t{}",  insertStatement, args);
				insertMethod.invoke(targetJdbc, insertStatement, args);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				log.error("Failed to insert record in target DB: {}", record, e);
			}
        });
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
