package net.savantly.data.table.copy;

public class ColumnMapping {

	private String sourceColumnName;
	private String targetColumnName;
	private ColumnType sourceColumnType;

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

	public ColumnMapping(String sourceColumnName, ColumnType sourceColumnType, String targetColumnName) {
		this.sourceColumnName = sourceColumnName;
		this.targetColumnName = targetColumnName;
		this.sourceColumnType = sourceColumnType;
	}

	public String getSourceColumnName() {
		return sourceColumnName;
	}
	public void setSourceColumnName(String sourceColumnName) {
		this.sourceColumnName = sourceColumnName;
	}
	public String getTargetColumnName() {
		return targetColumnName;
	}
	public void setTargetColumnName(String targetColumnName) {
		this.targetColumnName = targetColumnName;
	}
	public ColumnType getSourceColumnType() {
		return sourceColumnType;
	}
	public void setSourceColumnType(ColumnType sourceColumnType) {
		this.sourceColumnType = sourceColumnType;
	}
}
