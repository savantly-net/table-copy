package net.savantly.data.table.copy;

import java.sql.Types;

public class ColumnMapping {

	private String sourceColumnName;
	private String targetVariableName;
	private ColumnType sourceColumnType;
	
	public ColumnMapping() {}

	/**
	 * Uses the provided source column name as the target column name
	 * and the default column type of STRING
	 * @param sourceColumnName
	 */
	public ColumnMapping(String sourceColumnName) {
		this(sourceColumnName, ColumnType.STRING, sourceColumnName);
	}
	
	/**
	 * Uses the provided source column name as the target column name
	 * and the provided column type
	 * @param sourceColumnName
	 */
	public ColumnMapping(String sourceColumnName, ColumnType sourceColumnType) {
		this(sourceColumnName, sourceColumnType, sourceColumnName);
	}

	public ColumnMapping(String sourceColumnName, ColumnType sourceColumnType, String targetVariableName) {
		this.sourceColumnName = sourceColumnName;
		this.targetVariableName = targetVariableName;
		this.sourceColumnType = sourceColumnType;
	}
	
	public int getSqlType () {
		switch (this.sourceColumnType) {
		case BLOB:
			return Types.BLOB;
		case CLOB:
			return Types.CLOB;
		case INT:
			return Types.INTEGER;
		case LONG:
			return Types.BIGINT;
		case STRING:
			return Types.VARCHAR;
		default:
			return Types.VARCHAR;
		
		}
	}

	public String getSourceColumnName() {
		return sourceColumnName;
	}
	public void setSourceColumnName(String sourceColumnName) {
		this.sourceColumnName = sourceColumnName;
	}
	public String getTargetVariableName() {
		return targetVariableName;
	}
	public void setTargetVariableName(String targetVariableName) {
		this.targetVariableName = targetVariableName;
	}
	public ColumnType getSourceColumnType() {
		return sourceColumnType;
	}
	public void setSourceColumnType(ColumnType sourceColumnType) {
		this.sourceColumnType = sourceColumnType;
	}
}
