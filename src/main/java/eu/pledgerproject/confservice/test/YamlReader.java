package eu.pledgerproject.confservice.test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import eu.pledgerproject.confservice.scheduler.DescriptorParserKubernetes;
import io.kubernetes.client.util.Yaml;

public class YamlReader {

	public static void main(String[] args) throws Exception{
		System.out.println("YamlReader started");
		
		try {
			try(Connection conn = getDataSource().getConnection()){
				try(Statement st = conn.createStatement()){
					try(ResultSet rs = st.executeQuery("select deploy_descriptor from service where id=3")){
						rs.next();
						String deploymentDescriptor = rs.getString(1);
						String namespace = "test";
						String requestCpu = "110";
						String requestMem = "120";
						String nodeStringCSV = "testnode1,testnode2";
						String replicas = "7";
						deploymentDescriptor = DescriptorParserKubernetes.parseDeploymentDescriptor(deploymentDescriptor, namespace, requestCpu, requestMem, nodeStringCSV, replicas);

						System.out.println(deploymentDescriptor);
						Yaml.load(deploymentDescriptor);
						System.out.println("Test Kubernetes ok");
					}

				}
			}
				
		}catch(Exception e){
			throw e;
		}

	}
	
	private static DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://confservice-mysql:3306/confservice?serverTimezone=UTC");
        dataSource.setUsername("root");
        dataSource.setPassword("root");

        return dataSource;
    }
    

}
