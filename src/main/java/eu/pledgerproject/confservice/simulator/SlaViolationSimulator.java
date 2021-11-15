package eu.pledgerproject.confservice.simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class SlaViolationSimulator {
	
	
	//args 1 Serious
	public static void main(String[] args) {
		System.out.println("ResourceUsage simulator started");
		String description = "simulator description";
		String violation_name = "simulator";
		int slaId = Integer.parseInt(args[0]);
		String severity = args[1];
		
		long time = new Date().getTime();
		System.out.println("Last record will be on " + new Date(time));
		try {
			try(Connection connDrop = getDataSource().getConnection()){
				try{

					String insertSQL = "insert into sla_violation (description, severity_type, timestamp, violation_name, sla_id) values (?, ?, ?, ?, ?)";
					try(PreparedStatement ps = connDrop.prepareStatement(insertSQL)){
							
						ps.setString(1, description);
						ps.setString(2, severity);
						ps.setTimestamp(3, new java.sql.Timestamp(time));
						ps.setString(4, violation_name);
						ps.setInt(5, slaId);
						
						ps.execute();
							
					}catch(Exception e) {
						System.out.println(e);
					}
					
				}catch(Exception e) {
					System.out.println(e);
				}
			}catch(Exception e){
				System.out.println(e);
			}
		}catch(Exception e){
			System.out.println(e);
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
