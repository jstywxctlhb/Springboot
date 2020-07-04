package com.zjt.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.atomikos.icatch.jta.UserTransactionImp;
import com.atomikos.icatch.jta.UserTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.transaction.UserTransaction;
import java.util.Properties;

@Configuration
public class DruidConfig {

    @Primary
    @Autowired
    @Bean(name = "systemDataSource")
    public AtomikosDataSourceBean systemDataSource(Environment environment) {
        AtomikosDataSourceBean dataSourceBean = new AtomikosDataSourceBean();
        Properties properties = build(environment, "spring.datasource.druid.systemDB.");
        dataSourceBean.setXaDataSourceClassName("com.alibaba.druid.pool.xa.DruidXADataSource");
        dataSourceBean.setUniqueResourceName("systemDB");
        dataSourceBean.setPoolSize(5);
        dataSourceBean.setXaProperties(properties);
        return dataSourceBean;
    }

    @Autowired
    @Bean(name = "businessDataSource")
    public AtomikosDataSourceBean businessDataSource(Environment environment) {
        AtomikosDataSourceBean dataSourceBean = new AtomikosDataSourceBean();
        Properties properties = build(environment, "spring.datasource.druid.businessDB.");
        dataSourceBean.setXaDataSourceClassName("com.alibaba.druid.pool.xa.DruidXADataSource");
        dataSourceBean.setUniqueResourceName("businessDB");
        dataSourceBean.setPoolSize(5);
        dataSourceBean.setXaProperties(properties);
        return dataSourceBean;
    }


    @Bean(name = "xatx")
    public JtaTransactionManager regTransactionManager() {
        UserTransactionManager userTransactionManager = new UserTransactionManager();
        UserTransaction userTransaction = new UserTransactionImp();
        return new JtaTransactionManager(userTransaction, userTransactionManager);
    }

    private Properties build(Environment environment, String prefix) {
        Properties properties = new Properties();
        properties.put("url", environment.getProperty(prefix + "url"));
        properties.put("username", environment.getProperty(prefix + "username"));
        properties.put("password", environment.getProperty(prefix + "password"));
        properties.put("driverClassName", environment.getProperty(prefix + "driverClassName", ""));
        properties.put("initialSize", environment.getProperty(prefix + "initialSize", Integer.class));
        properties.put("maxActive", environment.getProperty(prefix + "maxActive", Integer.class));
        properties.put("minIdle", environment.getProperty(prefix + "minIdle", Integer.class));
        properties.put("maxWait", environment.getProperty(prefix + "maxWait", Integer.class));
        properties.put("poolPreparedStatements",
                environment.getProperty(prefix + "poolPreparedStatements",
                        Boolean.class));
        properties.put("maxPoolPreparedStatementPerConnectionSize",
                environment.getProperty(prefix + "maxPoolPreparedStatementPerConnectionSize",
                        Integer.class));
        properties.put("maxPoolPreparedStatementPerConnectionSize",
                environment.getProperty(prefix + "maxPoolPreparedStatementPerConnectionSize",
                        Integer.class));
        properties.put("validationQuery", environment.getProperty(prefix + "validationQuery"));
        properties.put("validationQueryTimeout",
                environment.getProperty(prefix + "validationQueryTimeout",
                        Integer.class));
        properties.put("testOnBorrow", environment.getProperty(prefix + "testOnBorrow", Boolean.class));
        properties.put("testOnReturn", environment.getProperty(prefix + "testOnReturn", Boolean.class));
        properties.put("testWhileIdle", environment.getProperty(prefix + "testWhileIdle", Boolean.class));
        properties.put("timeBetweenEvictionRunsMillis",
                environment.getProperty(prefix + "timeBetweenEvictionRunsMillis",
                        Integer.class));
        properties.put("minEvictableIdleTimeMillis",
                environment.getProperty(prefix + "minEvictableIdleTimeMillis",
                        Integer.class));
        properties.put("filters", environment.getProperty(prefix + "filters"));
        return properties;
    }

    @Bean
    public ServletRegistrationBean druidServlet() {
        return new ServletRegistrationBean(new StatViewServlet(), "/druid/*");
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new WebStatFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        filterRegistrationBean.addInitParameter("profileEnable", "true");
        return filterRegistrationBean;
    }

    @Bean
    public StatFilter statFilter() {
        StatFilter statFilter = new StatFilter();
        statFilter.setLogSlowSql(true);
        statFilter.setMergeSql(true);
        statFilter.setSlowSqlMillis(1000);
        return statFilter;
    }

    @Bean
    public WallFilter wallFilter() {
        WallFilter wallFilter = new WallFilter();
        WallConfig config = new WallConfig();
        config.setMultiStatementAllow(true);
        wallFilter.setConfig(config);
        return wallFilter;
    }
}