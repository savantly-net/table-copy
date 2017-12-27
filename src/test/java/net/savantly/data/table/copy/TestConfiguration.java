package net.savantly.data.table.copy;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.CustomScopeConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
@Import(DataSourceConfiguration.class)
public class TestConfiguration {

	
	@Bean
	public static CustomScopeConfigurer customScopeConfigurer() {
		CustomScopeConfigurer configurer = new CustomScopeConfigurer();
		Map<String, Object> scopes = new HashMap<String, Object>();
		scopes.put("session", new SimpleThreadScope());
		configurer.setScopes(scopes );
		return configurer;
	}
	
	@Bean
	DataTransferExecutor dataTransferExecutor(
			@Qualifier("sourceJdbc") NamedParameterJdbcTemplate sourceJdbc, 
			@Qualifier("targetJdbc") NamedParameterJdbcTemplate targetJdbc) {
		return new DataTransferExecutor(10, sourceJdbc, targetJdbc);
	}
	
	@Bean SourceFixture sourceFixture(@Qualifier("sourceJdbc") NamedParameterJdbcTemplate sourceJdbc) {
		return new SourceFixture(sourceJdbc);
	}
	
	@Bean TargetFixture targetFixture(@Qualifier("targetJdbc") NamedParameterJdbcTemplate targetJdbc) {
		return new TargetFixture(targetJdbc);
	}
	
}
