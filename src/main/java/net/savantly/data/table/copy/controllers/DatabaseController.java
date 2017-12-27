package net.savantly.data.table.copy.controllers;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.SessionScope;

import net.savantly.data.table.copy.DataSourceConfiguration;
import net.savantly.data.table.copy.DataTransferExecutor;

@SessionScope
@RestController
@RequestMapping("/rest/db")
public class DatabaseController {

	@Autowired
	private PropertySourcesPlaceholderConfigurer propertyConfigurer;
	@Autowired
	private ConfigurableListableBeanFactory beanFactory;
	
	@RequestMapping("/")
	public Map<String, Object> setConfiguration(@RequestBody Map<String, String> settings) {
		Map<String, Object> response = new HashMap<String, Object>();
		propertyConfigurer.setProperties(toProperties(settings));
		response.put("message", "Finished setting db properties");
		createDataBeans();
		return response;
	}
	
	@RequestMapping("/test")
	public Map<String, Object> testConfiguration(@RequestBody Map<String, String> settings) throws SQLException {
		DataSource ds = new DataSourceBuilder(this.getClass().getClassLoader())
				.url(settings.get("url"))
				.driverClassName(settings.get("driverClassName"))
				.username(settings.get("username"))
				.password(settings.get("password"))
				.build();
		ds.getConnection();
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("message", "connected to datasource");
		return response;
	}

	private void createDataBeans() {
		DataSourceConfiguration bean = beanFactory.createBean(DataSourceConfiguration.class);
	}

	private Properties toProperties(Map<String, String> settings) {
		Properties properties = new Properties();
		for (Entry<String, String> property : settings.entrySet()) {
			properties.setProperty(property.getKey(), property.getValue());
		}
		return properties;
	}

}
