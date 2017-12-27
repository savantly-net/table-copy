package net.savantly.data.table.copy;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.context.annotation.SessionScope;

@SessionScope
@Configuration
public class DataSourceConfiguration implements InitializingBean {
	
	public final static String SOURCE_DATASOURCE_BEAN_NAME = "sourceDataSource";
	public final static String TARGET_DATASOURCE_BEAN_NAME = "targetDataSource";
	public final static String SOURCE_JDBC_TEMPLATE_BEAN_NAME = "sourceJdbc";
	public final static String TARGET_JDBC_TEMPLATE_BEAN_NAME = "targetJdbc";

	@Autowired
	private  ApplicationContext context;
	
	private DataTransferExecutor executor;
	
	@ConfigurationProperties(prefix = "source.datasource")
	public class SourceDataSourceProperties extends DataSourceProperties {
		
		@Primary
		@Bean(SOURCE_DATASOURCE_BEAN_NAME)
		public DataSource sourceDataSource() {
			return this.initializeDataSourceBuilder().build();
		}
		
		@Bean(SOURCE_JDBC_TEMPLATE_BEAN_NAME)
		public NamedParameterJdbcTemplate sourceJdbc(@Qualifier(SOURCE_DATASOURCE_BEAN_NAME) DataSource dataSource) {
			return new NamedParameterJdbcTemplate(dataSource);
		}
	}
	
	@ConfigurationProperties(prefix = "target.datasource")
	public class TargetDataSourceProperties extends DataSourceProperties {
		
		@Primary
		@Bean(TARGET_DATASOURCE_BEAN_NAME)
		public DataSource sourceDataSource() {
			return this.initializeDataSourceBuilder().build();
		}
		
		@Bean(TARGET_JDBC_TEMPLATE_BEAN_NAME)
		public NamedParameterJdbcTemplate sourceJdbc(@Qualifier(TARGET_DATASOURCE_BEAN_NAME) DataSource dataSource) {
			return new NamedParameterJdbcTemplate(dataSource);
		}
	}

	public DataTransferExecutor getExecutor() {
		return executor;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		NamedParameterJdbcTemplate sourceJdbc = (NamedParameterJdbcTemplate) context.getBean(SOURCE_JDBC_TEMPLATE_BEAN_NAME);
		NamedParameterJdbcTemplate targetJdbc = (NamedParameterJdbcTemplate) context.getBean(TARGET_JDBC_TEMPLATE_BEAN_NAME);
		this.executor = new DataTransferExecutor(10, sourceJdbc, targetJdbc);
	}

}
