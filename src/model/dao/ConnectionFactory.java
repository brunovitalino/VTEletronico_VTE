package model.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory
{	
	String daoName = "DAO";
	
	public ConnectionFactory() {
	}
	
	public ConnectionFactory(String daoName) {
		this.daoName = daoName;
	}
	
	public Connection getConnection()
	{
		try
		{
			System.out.print("\n"+daoName+" acessando database Postgres... ");
//			try { Thread.sleep(500); } catch (Exception es) { es.printStackTrace(); }
			Connection conexao = DriverManager.getConnection("jdbc:postgresql://192.168.3.113:5432/vtcard", "vtcardadmin", "vitalino");
			System.out.println(daoName+" conectado.\n");
//			try { Thread.sleep(500); } catch (Exception es) { es.printStackTrace(); }
			return conexao;
		}
		catch (SQLException e)
		{
			System.out.println("FALHA "+daoName+". Database offline!\n");
//			try { Thread.sleep(1000); } catch (Exception es) { es.printStackTrace(); }
			throw new RuntimeException(e);
		}
	}
	
}
