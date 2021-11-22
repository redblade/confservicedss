package eu.pledgerproject.confservice.test;

import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.sql.DataSource;

import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;

public class JPAQuery {

	public static void main(String[] args) throws Exception {
		
		
		DataSource datasource = getDataSource();

		LocalSessionFactoryBean localSessionFactoryBean = getLocalSessionFactoryBean(datasource);
		localSessionFactoryBean.afterPropertiesSet();
		
		EntityManager entityManager = localSessionFactoryBean.getObject().createEntityManager();

		
		String queryJPA = "select benchmarkReport.node, max(benchmarkReport.mean) from BenchmarkReport benchmarkReport where benchmarkReport.metric = :metric and benchmarkReport.benchmark.category like :category group by benchmarkReport.node";
		Query query = entityManager.createQuery(queryJPA);
		entityManager.getEntityManagerFactory().addNamedQuery("testQuery", query);

		/*
		Query namedQuery = entityManager.createNamedQuery("testQuery");
		namedQuery.setParameter("metric", "performance_index");
		namedQuery.setParameter("category", "%cpu-intensive%");
		List result = namedQuery.getResultList();
		
		System.out.println(result);
		*/
	}
	
	
	private static LocalSessionFactoryBean getLocalSessionFactoryBean(DataSource dataSource) {
        final LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        sessionFactory.setPackagesToScan(new String[] { "eu.pledgerproject.confservice.domain" });
        sessionFactory.setPhysicalNamingStrategy(new SpringPhysicalNamingStrategy());
        sessionFactory.setImplicitNamingStrategy(new SpringImplicitNamingStrategy());
        sessionFactory.setHibernateProperties(getHibernateProperties());

        return sessionFactory;
    }
	
    private static DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/confservice?serverTimezone=UTC");
        dataSource.setUsername("root");
        dataSource.setPassword("root");

        return dataSource;
    }
    
    private static Properties getHibernateProperties() {
        return new Properties() {
			private static final long serialVersionUID = 1L;

			{
                this.put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
                this.put("hibernate.hbm2ddl.auto", "validate");
            }
        };

    }

}
