package com.tmv;

import com.tmv.inbound.RequestHandler;
import com.tmv.inbound.TcpServer;
import com.tmv.inbound.teltonika.TcpRequestHandler;
import com.tmv.position.PositionDataEventListener;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories("com.tmv.*")
public class AppConfig {

    @Bean(name="RequestHandler")
    RequestHandler getRequestHandler(ApplicationEventPublisher publisher) {
        return new TcpRequestHandler(publisher);
    }

    @Bean(name="TcpServer")
    TcpServer getTcpServer(RequestHandler requestHandler) {
        return new TcpServer(requestHandler);
    }

    @Bean(name="PositionDataEventListener")
    PositionDataEventListener getPositionDataEventListener() {
        return new PositionDataEventListener();
    }

    @Bean
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
    }

    @Bean
    public JpaTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        HibernateJpaVendorAdapter jpaVendorAdapter = new HibernateJpaVendorAdapter();
        jpaVendorAdapter.setDatabase(Database.H2);
        jpaVendorAdapter.setGenerateDdl(true);
        return jpaVendorAdapter;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean lemfb = new LocalContainerEntityManagerFactoryBean();
        lemfb.setDataSource(dataSource());
        lemfb.setJpaVendorAdapter(jpaVendorAdapter());
        lemfb.setPackagesToScan("com.tmv");
        return lemfb;
    }
}
