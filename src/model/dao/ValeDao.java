package model.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.entity.ADOrg;
import model.entity.Vale;

public class ValeDao
{
	private String nomeDao = "ValeDao";
	private Connection conexao;
	
	public ValeDao()
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
	
	//PESQUISAR TODOS AD_Org
	public List<ADOrg> findAllADOrg()	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		ADOrg aDOrgAux = null;
		List<ADOrg> adOrgs = new ArrayList<ADOrg>();
		
		comandoSQL = ""
				+ "SELECT "
				+ "ad_org_id, "
				+ "lbr_cnpj, "
				+ "name, "
				+ "password "
				+ "FROM AD_ORG "
				+ "WHERE 1=1 "
				+ "AND ad_org_id <> 2000008 " // ipade com cnpj invalido
				+ "ORDER BY ad_org_id; ";

		try {
			pst = conexao.prepareStatement(comandoSQL);
			rs = pst.executeQuery();
			while (rs.next()) {
				aDOrgAux = new ADOrg();
				aDOrgAux.setAdOrgId(rs.getLong("ad_org_id"));
				aDOrgAux.setLbrCnpj(rs.getString("lbr_cnpj"));
				aDOrgAux.setName(rs.getString("name"));
				aDOrgAux.setPassword(rs.getString("password"));
				aDOrgAux.setMatriculation(rs.getString("matriculation"));
				aDOrgAux.setQtd(rs.getInt("qtd"));
				adOrgs.add(aDOrgAux);
			}
			pst.close();
			rs.close();
			return adOrgs;
		}catch (SQLException e) {
//			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	//PESQUISAR TODOS VALES
	public List<Vale>  findAllVale()	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		Vale vale = null;
		ADOrg aDOrgAux = null;
		List<Vale> vales = new ArrayList<Vale>();
		
		comandoSQL =	""
				+ "SELECT "
				+ "v.*, "
				+ "o.lbr_cnpj, "
				+ "o.name, "
				+ "o.password, "
				+ "o.matriculation, "
				+ "o.qtd "
				+ "FROM vale v "
				+ "INNER JOIN AD_ORG o "
				+ "ON (v.ad_org_id = o.ad_org_id) "
				+ "where 1=1 "
				+ "AND numero_externo IS NOT NULL "
			//	+ "AND v.ad_org_id in  (2000019) "//(2000020, 2000021, 2000022) " //2000008, 2000020 2000019
				// Data em que a aplicacao foi executada para atualizar SALDO, 
				// dessa forma, o mesmo registro nao sera verificado varias vezes no mesmo dia.
				+ "AND ( (v.updated IS NULL) OR (v.updated < NOW()::date::timestamp) ) "
				// Filtro para atualizar apenas as construtoras
			//	+ "AND (o.ad_org_id in  (2000020, 2000015, 2000017, 2000012) "
//				+ "and v.saldo IS NULL "
				+ "ORDER BY v.id; ";
		
		try	{
			pst = conexao.prepareStatement(comandoSQL);
			rs = pst.executeQuery();
			while (rs.next()) {
				vale = new Vale();
				vale.setId(rs.getLong("id"));
				
				vale.setAdOrgId(rs.getLong("ad_org_id")); //pai cartao					
				aDOrgAux = new ADOrg();
				aDOrgAux.setAdOrgId(rs.getLong("ad_org_id"));  //filho adOrg
				aDOrgAux.setLbrCnpj(rs.getString("lbr_cnpj"));
				aDOrgAux.setName(rs.getString("name"));
				aDOrgAux.setPassword(rs.getString("password"));
				aDOrgAux.setMatriculation(rs.getString("matriculation"));
				aDOrgAux.setQtd(rs.getInt("qtd"));
				vale.setAdOrg(aDOrgAux);
				
				vale.setMatricula(rs.getString("matricula"));
				vale.setNome(rs.getString("nome"));
				vale.setNumeroExterno(rs.getString("numero_externo"));
				vales.add(vale);
			}
			pst.close();
			rs.close();
			return vales;
		}catch (SQLException e) {
//			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	//PESQUISAR TODOS VALES SEM NUMERO EXTERNO POR CNPJ
	public List<Vale>  findAllValeSemNumeroExternoByCnpj(String cnpj) {
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		Vale valeAux = null;
		ADOrg aDOrgAux = null;
		List<Vale> vales = new ArrayList<Vale>();
		
		comandoSQL = ""
				+ "SELECT "
				+ "v.*, "
				+ "o.lbr_cnpj, "
				+ "o.name, "
				+ "o.password, "
				+ "o.matriculation, "
				+ "o.qtd "
				+ "FROM vale v "
				+ "LEFT JOIN AD_ORG o "
				+ "ON (v.ad_org_id = o.ad_org_id) "
				+ "WHERE 1=1 "
				+ "AND v.numero_externo IS null "	
				+ "AND (o.lbr_cnpj LIKE ?); ";		
		try	{
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, cnpj);
			rs = pst.executeQuery();
			while (rs.next()) {
				valeAux = new Vale();
				valeAux.setId(rs.getLong("id"));

				valeAux.setAdOrgId(rs.getLong("ad_org_id")); //pai cartao					
				aDOrgAux = new ADOrg();
				aDOrgAux.setAdOrgId(rs.getLong("ad_org_id"));  //filho adOrg
				aDOrgAux.setLbrCnpj(rs.getString("lbr_cnpj"));
				aDOrgAux.setName(rs.getString("name"));
				aDOrgAux.setPassword(rs.getString("password"));
				aDOrgAux.setMatriculation(rs.getString("matriculation"));
				aDOrgAux.setQtd(rs.getInt("qtd"));
				valeAux.setAdOrg(aDOrgAux);
				
				valeAux.setMatricula(rs.getString("matricula"));
				valeAux.setNome(rs.getString("nome"));
				valeAux.setNumeroExterno(rs.getString("numero_externo"));
				vales.add(valeAux);
			}
			pst.close();
			rs.close();
			return vales;
		}catch (SQLException e) {
//			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	//PESQUISAR TODOS VALES POR CNPJ
	public List<Vale>  findAllValeByCnpj(String cnpj) {
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		Vale valeAux = null;
		ADOrg aDOrgAux = null;
		List<Vale> vales = new ArrayList<Vale>();
		
		comandoSQL = ""
				+ "SELECT "
				+ "v.*, "
				+ "o.lbr_cnpj, "
				+ "o.name, "
				+ "o.password, "
				+ "o.matriculation, "
				+ "o.qtd "
				+ "FROM vale v "
				+ "LEFT JOIN AD_ORG o "
				+ "ON (v.ad_org_id = o.ad_org_id) "
				+ "WHERE 1=1 "	
				+ "AND (o.lbr_cnpj LIKE ?); ";		
		try	{
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, cnpj);
			rs = pst.executeQuery();
			while (rs.next()) {
				valeAux = new Vale();
				valeAux.setId(rs.getLong("id"));

				valeAux.setAdOrgId(rs.getLong("ad_org_id")); //pai cartao					
				aDOrgAux = new ADOrg();
				aDOrgAux.setAdOrgId(rs.getLong("ad_org_id"));  //filho adOrg
				aDOrgAux.setLbrCnpj(rs.getString("lbr_cnpj"));
				aDOrgAux.setName(rs.getString("name"));
				aDOrgAux.setPassword(rs.getString("password"));
				aDOrgAux.setMatriculation(rs.getString("matriculation"));
				aDOrgAux.setQtd(rs.getInt("qtd"));
				valeAux.setAdOrg(aDOrgAux);
				
				valeAux.setMatricula(rs.getString("matricula"));
				valeAux.setNome(rs.getString("nome"));
				valeAux.setNumeroExterno(rs.getString("numero_externo"));
				valeAux.setTipoVale(rs.getString("tipo_vale"));
				vales.add(valeAux);
			}
			pst.close();
			rs.close();
			return vales;
		}catch (SQLException e) {
//			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	public Vale findOneVale(ADOrg aDOrg, String matricula)	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		Vale valeAux = null;
		ADOrg aDOrgAux = null;
		
		comandoSQL =	""
				+ "SELECT "
				+ "v.*, "
				+ "o.lbr_cnpj, "
				+ "o.name, "
				+ "o.password, "
				+ "o.matriculation, "
				+ "o.qtd "
				+ "FROM vale v "
				+ "INNER JOIN AD_ORG o "
				+ "ON (v.ad_org_id = o.ad_org_id) "
				+ "where 1=1 "
				+ "AND (o.lbr_cnpj like ?) "
				+ "AND (v.matricula like ?) "
				+ "ORDER BY v.id "
				+ "LIMIT 1 "
				+ "; ";
		
		try	{
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, aDOrg.getLbrCnpj());
			pst.setString(2, matricula);
			rs = pst.executeQuery();
			while (rs.next()) {
				valeAux = new Vale();
				valeAux.setId(rs.getLong("id"));
				
				valeAux.setAdOrgId(rs.getLong("ad_org_id")); //pai cartao					
				aDOrgAux = new ADOrg();
				aDOrgAux.setAdOrgId(rs.getLong("ad_org_id"));  //filho adOrg
				aDOrgAux.setLbrCnpj(rs.getString("lbr_cnpj"));
				aDOrgAux.setName(rs.getString("name"));
				aDOrgAux.setPassword(rs.getString("password"));
				aDOrgAux.setMatriculation(rs.getString("matriculation"));
				aDOrgAux.setQtd(rs.getInt("qtd"));
				valeAux.setAdOrg(aDOrgAux);
				
				valeAux.setMatricula(rs.getString("matricula"));
				valeAux.setNome(rs.getString("nome"));
				valeAux.setNumeroExterno(rs.getString("numero_externo"));
				break;
			}
			pst.close();
			rs.close();
			return valeAux;
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	//PESQUISAR TODOS VALES POR CNPJ
	public List<Vale>  findFiveLastUpdatedTrackingByOrgMatriculation(String cnpj) {
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		Vale valeAux = null;
		ADOrg aDOrgAux = null;
		List<Vale> vales = new ArrayList<Vale>();
		
		comandoSQL = ""
				+ "SELECT "
				+ "v.*, "
				+ "o.lbr_cnpj, "
				+ "o.name, "
				+ "o.password, "
				+ "o.matriculation, "
				+ "o.qtd "
				+ "FROM vale v "
				+ "LEFT JOIN AD_ORG o "
				+ "ON (v.ad_org_id = o.ad_org_id) "
				+ "WHERE 1=1 "	
				+ "AND (o.lbr_cnpj LIKE ?); ";		
		try	{
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, cnpj);
			rs = pst.executeQuery();
			while (rs.next()) {
				valeAux = new Vale();
				valeAux.setId(rs.getLong("id"));

				valeAux.setAdOrgId(rs.getLong("ad_org_id")); //pai cartao					
				aDOrgAux = new ADOrg();
				aDOrgAux.setAdOrgId(rs.getLong("ad_org_id"));  //filho adOrg
				aDOrgAux.setLbrCnpj(rs.getString("lbr_cnpj"));
				aDOrgAux.setName(rs.getString("name"));
				aDOrgAux.setPassword(rs.getString("password"));
				aDOrgAux.setMatriculation(rs.getString("matriculation"));
				aDOrgAux.setQtd(rs.getInt("qtd"));
				valeAux.setAdOrg(aDOrgAux);
				
				valeAux.setMatricula(rs.getString("matricula"));
				valeAux.setNome(rs.getString("nome"));
				valeAux.setNumeroExterno(rs.getString("numero_externo"));
				valeAux.setTipoVale(rs.getString("tipo_vale"));
				vales.add(valeAux);
			}
			pst.close();
			rs.close();
			return vales;
		}catch (SQLException e) {
//			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	// ATUALIZAR NUMERO EXTERNO PELA MATRICULA
	public boolean updateNumeroExternoByMatricula(String numeroExterno, Long adOrgId, 
	String matricula) {
		String comandoSQL = "";
		PreparedStatement pst = null;
			
		comandoSQL = ""
				+ "UPDATE vale "
				+ "SET numero_externo = ? "
				+ "WHERE 1=1 "
				+ "AND ad_org_id = ? "
				+ "AND matricula like ? "
				+ "; ";
		
		try	{			
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, numeroExterno);
			pst.setLong(2, adOrgId);
			pst.setString(3, matricula);
			boolean resposta = true;
			pst.execute();
			pst.close();
			return resposta;
		}catch (SQLException e)	{
//			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return false;
		}		
	}
	
	//ALTERAR
	public boolean updateSaldoData(String saldo, LocalDate dataSaldo, String numeroExterno, Long adOrgId)
	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		
		comandoSQL = ""
				+ "UPDATE vale "
				+ "set saldo = ?, "
				+ "data_saldo = ?, "
				+ "updated = NOW() "
				+ "WHERE 1=1 "
				+ "AND ad_org_id = ? "
				+ "AND numero_externo like ? "
				+ ";";
		try
		{			
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, saldo);
			pst.setDate(2, Date.valueOf(dataSaldo));
			pst.setLong(3, adOrgId);
			pst.setString(4, numeroExterno);
			boolean resposta = true;
			pst.execute();
			pst.close();
			return resposta;
		}
		catch (SQLException e)
		{
//			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return false;
		}
		
	}
	
	public boolean saveVale(Long adOrgId, String matricula, String nome, String nExterno) {
		String comandoSQL = "";
		PreparedStatement pst = null;
		
		// ALGORITMO QUE APLICAVA FORMULA DO ID CADASTRADO ACIMA DE 2000000
		/*Long valeId = null;
		String adOrgIdToString = "";
		int lastSum = 0, sum = 0;
		boolean firstIteration = true;
		
		adOrgIdToString = String.valueOf(adOrgId);
		
		if ( !(adOrgIdToString.length() > 0) )
			
			return false;
		
		else {
		
			for (int i = adOrgIdToString.length()-1; i > 0; i--) {
				
				if (firstIteration) {
					
					if ( adOrgIdToString.charAt(i) == '0' )						
						return false;
					
					firstIteration = false;
				}
				
				if (adOrgIdToString.charAt(i) == '0') {
					
					adOrgIdToString = adOrgIdToString.substring(i+1, adOrgIdToString.length());
					break;		
					
				}

			}
		}
		
		valeId = (Long.valueOf(adOrgIdToString) * 1000000);
		valeId += Long.valueOf(matricula); */
			
		comandoSQL = ""
//				+ "INSERT INTO vale(matricula, nome, numero_externo, ad_org_id, updated, id) "
				+ "INSERT INTO vale(matricula, nome, numero_externo, ad_org_id, updated) "
				+ "VALUES (?, ?, ?, ?, NOW()) "
				+ "; ";
		
		try	{			
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, matricula);
			pst.setString(2, nome);
			pst.setString(3, nExterno);
			pst.setLong(4, adOrgId);
//			pst.setLong(5, valeId);
			boolean resposta = true;
			pst.executeUpdate();
			pst.close();
			return resposta;
		}catch (SQLException e)	{
					e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return false;
		}
		
	}
		
}
