package eu.pledgerproject.confservice.simulator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import eu.pledgerproject.confservice.monitoring.SteadyServiceOptimiser;

public class ResourceUsage {
	public static String CPU = "cpu";
	public static String CPU_LABEL = "cpu_millicore";
	public static String MEMORY = "memory";
	public static String MEMORY_LABEL = "memory_mb";
	public static String NUMBER = "number";
	
	//args 3 10 60 40 100 10
	public static void main(String[] args) {
		System.out.println("ResourceUsage simulator started");
		String group = "simulator"; 
		int service_id = Integer.parseInt(args[0]);
		int count = Integer.parseInt(args[1]);
		int stepSec = Integer.parseInt(args[2]);
		int cpuMean = Integer.parseInt(args[3]);
		int memoryMean = Integer.parseInt(args[4]);
		int variationPerc = Integer.parseInt(args[5]);
		
		long time = new Date().getTime();
		System.out.println("Last record will be on " + new Date(time));
		try {
			try(Connection connDrop = getDataSource().getConnection()){
				try{
					String insertSQL = "insert into service_report (category, jhi_group, jhi_key, timestamp, value, service_id) values (?, ?, ?, ?, ?, ?)";
					try(PreparedStatement ps = connDrop.prepareStatement(insertSQL)){
						for(int i= 0; i<count; i++) {
							
							ps.setString(1, SteadyServiceOptimiser.RESOURCE_USAGE_CATEGORY);
							ps.setString(2, group);
							ps.setTimestamp(4, new java.sql.Timestamp(time - (count -i - 1) * stepSec * 1000));
							ps.setLong(6, service_id);
							
							ps.setString(3, CPU_LABEL);
							ps.setDouble(5, Math.round(cpuMean + (Math.random()-0.5)* (cpuMean * variationPerc / 100)));
							ps.execute();
							
							ps.setString(3, MEMORY_LABEL);
							ps.setDouble(5, Math.round(memoryMean + (Math.random()-0.5)* (memoryMean * variationPerc / 100)));
							ps.execute();
						}
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
