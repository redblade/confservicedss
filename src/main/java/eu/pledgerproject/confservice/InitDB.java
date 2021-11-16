package eu.pledgerproject.confservice;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.springframework.boot.orm.jpa.hibernate.SpringImplicitNamingStrategy;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.util.StreamUtils;


public class InitDB {

	public static void main(String[] args) throws Exception {
		System.out.println("InitDB started");
		String dml = args.length > 0 ? args[0] : null;
		
		DataSource datasource = getDataSource();
		
		boolean isDbLocked = false;
		try {
			try(Connection connDrop = getDataSource().getConnection()){
				try{
					String updateTimestamp = "update db_lock set id=0";
					connDrop.createStatement().executeUpdate(updateTimestamp);
					isDbLocked = true;
				}catch(Exception e) {
				}
				
			}
			System.out.println("1/4-CHECK done, DB is locked: " + isDbLocked);
		}catch(Exception e){
			System.out.println("1/4-CHECK failed " + e.getMessage());
			throw e;
		}
		
		if(!isDbLocked) {
			try {
				try(Connection connDrop = getDataSource().getConnection()){
	
					ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();    
		    		rdp.addScript(new ClassPathResource("config/sql/mysql_drop_all.sql")); 
		    		rdp.populate(connDrop);
				}
				System.out.println("2/4-DROP done");
			}catch(Exception e){
				System.out.println("2/4-DROP failed " + e.getMessage());
				throw e;
			}
			
			LocalSessionFactoryBean localSessionFactoryBean = getLocalSessionFactoryBean(datasource);
			localSessionFactoryBean.afterPropertiesSet();
	        try (SessionFactory sessionFactory = localSessionFactoryBean.getObject()){
	            sessionFactory.createEntityManager().close();
	            System.out.println("3/4-CREATE done");
	        }catch(Exception e) {
	        	System.out.println("3/4-CREATE failed " + e.getMessage());
	        	throw e;
	        }
	        
	        
	        try(Connection connLoad = getDataSource().getConnection()){
				ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
				
				String dump = 
					dml == null ? 
							StreamUtils.copyToString(new ClassPathResource("config/sql/dump.sql").getInputStream(), Charset.defaultCharset()) 
							:
							StreamUtils.copyToString(new FileInputStream(dml), Charset.defaultCharset())
					;
				
				dump = loadFilesInScript(dump);

				rdp.addScript(new ByteArrayResource(dump.getBytes())); 
	    		rdp.populate(connLoad);
				String createVersionTable = "CREATE TABLE confservice.db_lock (id INT);";

				connLoad.createStatement().executeUpdate(createVersionTable);
				System.out.println("4/4-LOAD done");
	        }catch(Exception e) {
	        	System.out.println("4/4-LOAD failed " + e.getMessage());
	        	throw e;
	        }
	        
		}
		System.out.println("InitDB done");
	}
	
	private static String loadFilesInScript(String sql) throws java.io.IOException {
		while(sql.contains("LOAD_FILE")) {
			int indexSplit = sql.indexOf("LOAD_FILE");
			String preSQL = sql.substring(0, indexSplit);
			String postSQL = sql.substring(sql.indexOf(",", indexSplit));
			String fileToLoad = sql.substring(indexSplit + "LOAD_FILE".length() + "('".length(), sql.indexOf("')", indexSplit));
			String content = StreamUtils.copyToString(new ClassPathResource(fileToLoad).getInputStream(), Charset.defaultCharset());
			content = content.replace("\n", "\\n");
			content = content.replace("'", "\'");
			content = content.replace("\"", "\\\"");
			sql = preSQL + "'" + content + "'" + postSQL;
		}
		return sql;
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
        dataSource.setUrl("jdbc:mysql://confservice-mysql:3306/confservice?serverTimezone=UTC");
        dataSource.setUsername("root");
        dataSource.setPassword("root");

        return dataSource;
    }
    
    private static Properties getHibernateProperties() {
        return new Properties() {
			private static final long serialVersionUID = 1L;

			{
                this.put("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect");
                this.put("hibernate.hbm2ddl.auto", "create");
            }
        };

    }
	
}
