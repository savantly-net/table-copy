package net.savantly.data.table.copy;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DataSourceConfiguration {

	@ConfigurationProperties(prefix = "source.datasource")
	class SourceDataSourceProperties extends DataSourceProperties {
		
		@Primary
		@Bean("sourceDataSource")
		public DataSource sourceDataSource() {
			return this.initializeDataSourceBuilder().build();
		}
		
		@Bean("sourceJdbc")
		public JdbcTemplate sourceJdbc(@Qualifier("sourceDataSource") DataSource dataSource) {
			return new JdbcTemplate(dataSource);
		}
	}
	
	@ConfigurationProperties(prefix = "target.datasource")
	class TargetDataSourceProperties extends DataSourceProperties {
		
		@Primary
		@Bean("targetDataSource")
		public DataSource sourceDataSource() {
			return this.initializeDataSourceBuilder().build();
		}
		
		@Bean("targetJdbc")
		public JdbcTemplate sourceJdbc(@Qualifier("targetDataSource") DataSource dataSource) {
			return new JdbcTemplate(dataSource);
		}
	}
}
