package ru.bvn13.jircbot;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Created by bvn13 on 30.01.2018.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackageClasses = MainApp.class)
public class JpaConfig {

    @Value("${spring.dataSource.driverClassName}")
    private String driver;
    @Value("${spring.dataSource.url}")
    private String url;
    @Value("${spring.dataSource.username}")
    private String username;
    @Value("${spring.dataSource.password}")
    private String password;
    @Value("${spring.hibernate.dialect}")
    private String dialect;
    @Value("${spring.hibernate.hbm2ddl.auto}")
    private String hbm2ddlAuto;
    @Value("${spring.hibernate.show_sql}")
    private Boolean showSql;

    @Value("${spring.hibernate.use_sql_comments}")
    private Boolean useSqlComments;
    @Value("${spring.hibernate.format_sql}")
    private Boolean formatSql;


    @Bean
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driver);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        config.addDataSourceProperty("useUnicode", "true");
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(config);
    }


    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan("ru.bvn13.jircbot");
        entityManagerFactoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put(org.hibernate.cfg.Environment.DIALECT, dialect);
        jpaProperties.put(org.hibernate.cfg.Environment.HBM2DDL_AUTO, hbm2ddlAuto);
        jpaProperties.put(org.hibernate.cfg.Environment.SHOW_SQL, showSql);
        //jpaProperties.put(org.hibernate.cfg.Environment.USE_SQL_COMMENTS, useSqlComments);
        jpaProperties.put(org.hibernate.cfg.Environment.FORMAT_SQL, formatSql);
        entityManagerFactoryBean.setJpaProperties(jpaProperties);

        return entityManagerFactoryBean;
    }

    @Bean(name = "transactionManager")
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return new JpaTransactionManager();
    }

}