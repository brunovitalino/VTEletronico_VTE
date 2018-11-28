package model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import model.entity.ADOrg;
import model.entity.Recharge;
import model.entity.Vale;

public class RechargeDao
{
	private String nomeDao = "RechargeDao";
	private Connection conexao;
	
	public RechargeDao()
	{
		this.conexao = new ConnectionFactory(nomeDao).getConnection();
	}
	
	//Nossa camada de controle usara para fechar a conexao
	public void close()
	{
		try
		{
			this.conexao.close();
			System.out.println("\n"+nomeDao+" desconectado!\n");
		}
		catch (SQLException e)
		{
			System.out.println("\nNao ha conexao "+nomeDao+" a ser fechada!\n");
		}
	}
	
	// LER ULTIMA OCORRENCIA PELA OrgMatriculation e ValeMatricula
	public Recharge findLastRecharge(ADOrg aDOrg, Vale vale)	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		Recharge recharge = null;
		Vale valeAux = null;
		ADOrg adOrgAux = null;
		
		comandoSQL = ""
				+ "SELECT "
				+ "v.ad_org_id, "
				+ "o.matriculation, "
				+ "r.vale_id, "
				+ "v.matricula, "
				+ "r.id AS recharge_id, "
				+ "r.order_number, "
				+ "r.value, "
				+ "r.order_date, "
				+ "r.order_category, "
				+ "r.recharge_date, "
				+ "r.car_line_way "				
				+ "FROM vale v "
				+ "LEFT JOIN AD_ORG o ON (v.ad_org_id = o.ad_org_id) "
				+ "RIGHT JOIN Recharge r ON (v.id = r.vale_id) "
				+ "WHERE 1=1 "
				+ "AND (o.matriculation like ?) "
				+ "AND (v.matricula like ?) "
//					+ "AND (r.order_category like ?) "
				+ "ORDER BY recharge_date DESC "
				+ "LIMIT 1 "
				+ ";";
		
		try	{
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, aDOrg.getMatriculation());
			pst.setString(2, vale.getMatricula());
//				pst.setString(3, vale.getTipoVale());
			rs = pst.executeQuery();
			while (rs.next()) {
				recharge = new Recharge();
				recharge.setId(rs.getLong("recharge_id"));
				recharge.setOrderNumber(rs.getBigDecimal("order_number"));
				recharge.setValue(rs.getBigDecimal("value"));
				recharge.setOrderDate(rs.getTimestamp("order_date"));
				recharge.setOrderCategory(rs.getString("order_category"));
				recharge.setRechargeDate(rs.getTimestamp("recharge_date"));
				recharge.setCarLineWay(rs.getString("car_line_way"));
				
				recharge.setValeId(rs.getLong("vale_id"));
				valeAux = new Vale();
				valeAux.setId(rs.getLong("vale_id"));
				valeAux.setMatricula(rs.getString("matricula"));
				
				valeAux.setAdOrgId(rs.getLong("ad_org_id"));
				adOrgAux = new ADOrg();
				adOrgAux.setAdOrgId(rs.getLong("ad_org_id"));
				adOrgAux.setMatriculation(rs.getString("matriculation"));
				
				valeAux.setAdOrg(adOrgAux);
				recharge.setVale(valeAux);
				
				break;
			}
			pst.close();
			rs.close();
			return recharge;
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	// LER ALGUMA OCORRENCIA PELA OrgMatriculation e ValeMatricula menor ou igual a 90 dias
	//Metodo feito atraves de ordenacao, cada funcionario so pode conter 1 registro de recarga
	public List<Recharge> findFiveFirstUpdatedAndRechargeLessThenNinetyDays(ADOrg aDOrg)	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		List<Recharge> recharges = new ArrayList<Recharge>();
		Recharge recharge = null;
		Vale valeAux = null;
		ADOrg aDOrgAux = null;
		
		comandoSQL = ""
				+ "SELECT "
				+ "v.ad_org_id, "
				+ "o.matriculation, "
				+ "r.vale_id, "
				+ "v.matricula, "
				+ "r.id AS recharge_id, "
				+ "r.order_number, "
				+ "r.value, "
				+ "r.order_date, "
				+ "r.order_category, "
				+ "r.recharge_date, "
				+ "r.car_line_way, "	
				+ "r.updated "				
				+ "FROM vale v "
				+ "LEFT JOIN AD_ORG o ON (v.ad_org_id = o.ad_org_id) "
				+ "RIGHT JOIN Recharge r ON (v.id = r.vale_id) "
				+ "WHERE 1=1 "
				+ "AND (o.matriculation like ?) "
//				+ "AND (v.matricula like ?) "
				+ "AND (DATE_PART('day', NOW()::timestamp without time zone - r.recharge_date) <= 90) "
				+ "ORDER BY r.updated, o.matriculation, r.vale_id "
				+ "LIMIT 5 "
				+ ";";
		
		try	{
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, aDOrg.getMatriculation());
//			pst.setString(2, vale.getMatricula());
			rs = pst.executeQuery();
			while (rs.next()) {
				recharge = new Recharge();
				recharge.setId(rs.getLong("recharge_id"));
				recharge.setOrderNumber(rs.getBigDecimal("order_number"));
				recharge.setValue(rs.getBigDecimal("value"));
				recharge.setOrderDate(rs.getTimestamp("order_date"));
				recharge.setOrderCategory(rs.getString("order_category"));
				recharge.setRechargeDate(rs.getTimestamp("recharge_date"));
				recharge.setCarLineWay(rs.getString("car_line_way"));
				recharge.setUpdated(rs.getTimestamp("updated"));
				
				recharge.setValeId(rs.getLong("vale_id"));
				valeAux = new Vale();
				valeAux.setId(rs.getLong("vale_id"));
				valeAux.setMatricula(rs.getString("matricula"));
				
				valeAux.setAdOrgId(rs.getLong("ad_org_id"));
				aDOrgAux = new ADOrg();
				aDOrgAux.setAdOrgId(rs.getLong("ad_org_id"));
				aDOrgAux.setMatriculation(rs.getString("matriculation"));
				
				valeAux.setAdOrg(aDOrgAux);
				recharge.setVale(valeAux);
				
				recharges.add(recharge);
			}
			pst.close();
			rs.close();
			return recharges;
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	// LER ALGUMA OCORRENCIA PELA OrgMatriculation e ValeMatricula menor ou igual a 90 dias - Agrupando
	//Metodo feito atraves de agrupamento e pegando Min(data), cada funcionario pode ter varios registros de recarga
	public List<Recharge> findFiveLastRechargeLessThenNinetyDaysGrouped(String orgMatriculation, String valeMatricula) {
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		List<Recharge> recharges = new ArrayList<Recharge>();
		Recharge recharge = null;
		Vale vale = null;
		ADOrg adOrg = null;
		
		comandoSQL = ""
				+ "SELECT "
				+ "v.ad_org_id, "
				+ "o.matriculation, "
				+ "r.vale_id, "
				+ "v.matricula, "
				+ "r.id AS recharge_id, "
				+ "r.order_number, "
//				+ "r.value, "
//				+ "r.order_date, "
				+ "r.order_category "
//				+ "r.recharge_date, "
//				+ "r.car_line_way "				
				+ "FROM vale v "
				+ "LEFT JOIN AD_ORG o ON (v.ad_org_id = o.ad_org_id) "
				+ "RIGHT JOIN Recharge r ON (v.id = r.vale_id) "
				+ "WHERE 1=1 "
				+ "AND (o.matriculation like ?) "
				+ "AND (v.matricula like ?) "
				+ "AND (DATE_PART('day', NOW()::timestamp without time zone - r.recharge_date) <= 90) "
				+ "GROUP BY v.ad_org_id, o.matriculation, r.vale_id, v.matricula, r.id, "
				+ "r.order_number, r.order_category "
				+ "ORDER BY r.recharge_date DESC, r.updated "
				+ "LIMIT 5 "
				+ ";";
		
		try	{
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, orgMatriculation);
			pst.setString(2, valeMatricula);
			rs = pst.executeQuery();
			while (rs.next()) {
				recharge = new Recharge();
				recharge.setId(rs.getLong("recharge_id"));
				recharge.setOrderNumber(rs.getBigDecimal("order_number"));
//				recharge.setValue(rs.getBigDecimal("value"));
//				recharge.setOrderDate(rs.getTimestamp("order_date"));
				recharge.setOrderCategory(rs.getString("order_category"));
//				recharge.setRechargeDate(rs.getTimestamp("recharge_date"));
//				recharge.setCarLineWay(rs.getString("car_line_way"));
				
				recharge.setValeId(rs.getLong("vale_id"));
				vale = new Vale();
				vale.setId(rs.getLong("vale_id"));
				vale.setMatricula(rs.getString("matricula"));
				
				vale.setAdOrgId(rs.getLong("ad_org_id"));
				adOrg = new ADOrg();
				adOrg.setAdOrgId(rs.getLong("ad_org_id"));
				adOrg.setMatriculation(rs.getString("matriculation"));
				
				vale.setAdOrg(adOrg);
				recharge.setVale(vale);
				
				recharges.add(recharge);
			}
			pst.close();
			rs.close();
			return recharges;
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	// SAVE
	public boolean saveRecharge(Recharge recharge)
	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		
		comandoSQL = ""
				+ "INSERT INTO recharge(order_number, value, order_date, order_category, "
				+ "recharge_date, car_line_way, vale_id) " //, updated)
				+ "VALUES (?, ?, ?, ?, ?, ?, ?) " //, NOW()) "
				+ ";";		
		try
		{			
			pst = conexao.prepareStatement(comandoSQL);
			pst.setBigDecimal(1, recharge.getOrderNumber());
			pst.setBigDecimal(2, recharge.getValue());
			pst.setTimestamp(3, recharge.getOrderDate());
			pst.setString(4, recharge.getOrderCategory());
			pst.setTimestamp(5, recharge.getRechargeDate());
			pst.setString(6, recharge.getCarLineWay());
			pst.setLong(7, recharge.getValeId());
			boolean resposta = true;
			pst.execute();
			pst.close();
			return resposta;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return false;
		}		
	}
	
	// UPDATE
	public boolean updateRecharge(Recharge recharge)
	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		
		comandoSQL = ""
				+ "UPDATE recharge SET "
				+ "order_number		= ?, "
				+ "value			= ?, "
				+ "order_date		= ?, "
				+ "order_category	= ?, "					
				+ "recharge_date	= ?, "
				+ "car_line_way		= ? "
//				+ "updated			= NOW() "
				+ "WHERE 1=1 "
				+ "AND (vale_id = ?) "
//				+ "AND (order_category like ?) "
				+ ";";		
		try
		{			
			pst = conexao.prepareStatement(comandoSQL);
			pst.setBigDecimal(1, recharge.getOrderNumber());
			pst.setBigDecimal(2, recharge.getValue());
			pst.setTimestamp(3, recharge.getOrderDate());
			pst.setString(4, recharge.getOrderCategory());
			pst.setTimestamp(5, recharge.getRechargeDate());
			pst.setString(6, recharge.getCarLineWay());
			pst.setLong(7, recharge.getValeId());
			boolean resposta = true;
			pst.execute();
			pst.close();
			return resposta;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return false;
		}		
	}
	
	// DELETE
	public boolean deleteRecharge(Vale vale)
	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		
		comandoSQL = ""
				+ "DELETE "
				+ "FROM Recharge "
				+ "WHERE 1=1 "
				+ "AND (vale_id = ?) "
				+ ";";		
		try
		{			
			pst = conexao.prepareStatement(comandoSQL);
			pst.setLong(1, vale.getId());
			boolean resposta = true;
			pst.execute();
			pst.close();
			return resposta;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return false;
		}		
	}
	
	
		
}
