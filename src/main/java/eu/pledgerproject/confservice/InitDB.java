package eu.pledgerproject.confservice;

import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.StreamUtils;


public class InitDB {
    private final static Logger log = LoggerFactory.getLogger(InitDB.class);

	public static void main(String[] args) throws Exception {
		log.info("InitDB started");

		String dumpFilePath = "/app/resources/config/sql/dump_base.sql";

		String host = "confservice-mysql.core.svc.cluster.local";
		String port = "3306";
		String user = "root";
		String pass = "root";
		
		if(args.length == 1) {
			dumpFilePath = args[0];
		}
		
		else if(args.length == 5) {
			dumpFilePath = args[0];
			host = args[1];
			port = args[2];
			user = args[3];
			pass = args[4];
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
			String basePath = dumpFilePath.substring(0, dumpFilePath.lastIndexOf("/"));
			String dumpFile = dumpFilePath.substring(dumpFilePath.lastIndexOf("/") + 1);
			try(Connection connClean = getDataSource(host, port, user, pass).getConnection()){
				ResourceDatabasePopulator rdp = new ResourceDatabasePopulator();
				
				try (FileInputStream fis = new FileInputStream(basePath + "/" + "mysql_clean_all.sql")){
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

				try (FileInputStream fis = new FileInputStream(basePath + "/" + dumpFile)){
					String loadSQL = StreamUtils.copyToString(fis, Charset.defaultCharset()); 
					loadSQL = loadFilesInScript(basePath, loadSQL);
	
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
	
	private static String loadFilesInScript(String basePath, String sql) throws java.io.IOException {
		while(sql.contains("LOAD_FILE_YAML")) {
			int indexSplit = sql.indexOf("LOAD_FILE_YAML");
			String preSQL = sql.substring(0, indexSplit);
			String postSQL = sql.substring(sql.indexOf(")", indexSplit) + 1);
			String fileToLoad = sql.substring(indexSplit + "LOAD_FILE_YAML".length() + "('".length(), sql.indexOf("')", indexSplit));
			try(FileInputStream fis = new FileInputStream(basePath + "/" + fileToLoad)){
				String content = StreamUtils.copyToString(fis, Charset.defaultCharset());
				content = content.replace("\n", "\\n");
				content = content.replace("'", "\'");
				content = content.replace("\"", "\\\"");
				sql = preSQL + "'" + content + "'" + postSQL;
			}
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
