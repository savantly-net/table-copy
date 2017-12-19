package net.savantly.data.table.copy;

public class TransferOptions {

	private String selectStatement;
	private String insertStatement;
	private ColumnMapping[] mappings;
	
	public String getSelectStatement() {
		return selectStatement;
	}
	public void setSelectStatement(String selectStatement) {
		this.selectStatement = selectStatement;
	}
	public String getInsertStatement() {
		return insertStatement;
	}
	public void setInsertStatement(String insertStatement) {
		this.insertStatement = insertStatement;
	}
	public ColumnMapping[] getMappings() {
		return mappings;
	}
	public void setMappings(ColumnMapping[] mappings) {
		this.mappings = mappings;
	}
	
}
