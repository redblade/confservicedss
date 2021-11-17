package eu.pledgerproject.confservice;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StreamUtils;


public class InitDB {
    private final static Logger log = LoggerFactory.getLogger(InitDB.class);

	public static void main(String[] args) throws Exception {
		log.info("InitDB started");
		
		String host = "confservice-mysql.core.svc.cluster.local";
		String port = "3306";
		String user = "root";
		String pass = "root";
		String dumpFile = null;
		
		if(args.length == 0) {
			dumpFile = "config/sql/dump_base.sql";
		}
		
		else if(args.length == 1) {
			dumpFile = args[0];
		}
		
		else if(args.length == 5) {
			host = args[0];
			port = args[1];
			user = args[2];
			pass = args[3];
			dumpFile = args[4];
		}
		
		boolean isDBLocked = false;
		try(Connection connCheck = getDataSource(host, port, user, pass).getConnection()){
			try(Statement stat = connCheck.createStatement()){
				String updateTimestamp = "update db_lock set id=0;";
				stat.executeUpdate(updateTimestamp);
				isDBLocked = true;
			}
			catch(Exception e) {
				//if table is not found, it is ok
			}
			
			
			log.info("1/4-CHECK done, DB is " + (isDBLocked ? "locked, the DB data will not be changed, please drop db_lock first" : "unlocked, the DB data will be changed"));
		}catch(Exception e){
			log.info("1/4-CHECK failed", e);
			throw e;
		}
		
		if(!isDBLocked) {

			try(Connection connClean = getDataSource(host, port, user, pass).getConnection()){
				ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
				
				try (FileInputStream fis = new FileInputStream("config/sql/mysql_clean_all.sql")){
					String cleanSQL = StreamUtils.copyToString(fis, Charset.defaultCharset()); 
					
					rdp.addScript(new ByteArrayResource(cleanSQL.getBytes())); 
		    		rdp.populate(connClean);
				}
	        }catch(Exception e) {
	        	log.info("2/4-CLEAN " + e.getMessage());
	        	throw e;
	        }
			
			try(Connection connLoad = getDataSource(host, port, user, pass).getConnection()){
				ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();

				try (FileInputStream fis = new FileInputStream(dumpFile)){
					String loadSQL = StreamUtils.copyToString(fis, Charset.defaultCharset()); 
					loadSQL = loadFilesInScript(loadSQL);
	
					rdp.addScript(new ByteArrayResource(loadSQL.getBytes())); 
		    		rdp.populate(connLoad);
					log.info("3/4-LOAD done");
				}
	        }catch(Exception e) {
	        	log.info("3/4-LOAD failed", e);
	        	throw e;
	        }
	        
			try(Connection connUnlock = getDataSource(host, port, user, pass).getConnection()){
				try(Statement stat = connUnlock.createStatement()){
					String updateTimestamp = "create table db_lock(id INT);";
					stat.executeUpdate(updateTimestamp);
				}
				
				log.info("4/4-LOCK done");
			}catch(Exception e){
				log.info("4/4-LOCK failed", e);
				throw e;
			}
	        
		}
		
		log.info("InitDB done");
	}
	
	private static String loadFilesInScript(String sql) throws java.io.IOException {
		while(sql.contains("LOAD_FILE_YAML")) {
			int indexSplit = sql.indexOf("LOAD_FILE_YAML");
			String preSQL = sql.substring(0, indexSplit);
			String postSQL = sql.substring(sql.indexOf(",", indexSplit));
			String fileToLoad = sql.substring(indexSplit + "LOAD_FILE_YAML".length() + "('".length(), sql.indexOf("')", indexSplit));
			String content = StreamUtils.copyToString(new ClassPathResource(fileToLoad).getInputStream(), Charset.defaultCharset());
			content = content.replace("\n", "\\n");
			content = content.replace("'", "\'");
			content = content.replace("\"", "\\\"");
			sql = preSQL + "'" + content + "'" + postSQL;
		}
		return sql;
	}
	
    private static DataSource getDataSource(String host, String port, String user, String pass) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://"+host+":"+port+"/confservice?serverTimezone=UTC");
        dataSource.setUsername(user);
        dataSource.setPassword(pass);

        return dataSource;
    }
    
	
}
