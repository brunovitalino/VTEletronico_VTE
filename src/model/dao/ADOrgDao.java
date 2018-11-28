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

public class ADOrgDao
{
	private String nomeDao = "ADOrgDao";
	private Connection conexao;
	
	public ADOrgDao()
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
		List<ADOrg> aDOrgs = new ArrayList<ADOrg>();
		
		comandoSQL = ""
				+ "SELECT "
				+ "ad_org_id, "
				+ "lbr_cnpj, "
				+ "name, "
				+ "password, "
				+ "matriculation, "
				+ "qtd "
				+ "FROM AD_ORG "
				+ "WHERE 1=1 "
				+ "and qtd IS NOT null "//CORRIGIR CNPJ E DEPOIS APAGAR ESSA LINHA (matriculation 527839)
//				+ "and ad_org_id > 2000002 "
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
				aDOrgs.add(aDOrgAux);
			}
			pst.close();
			rs.close();
			return aDOrgs;
		}catch (SQLException e) {
			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	//PESQUISAR TODOS AD_Org
		public List<ADOrg> findAllADOrgConstrutoras()	{
			String comandoSQL = "";
			PreparedStatement pst = null;
			ResultSet rs = null;
			ADOrg aDOrgAux = null;
			List<ADOrg> aDOrgs = new ArrayList<ADOrg>();
			
			comandoSQL = ""
					+ "SELECT "
					+ "ad_org_id, "
					+ "lbr_cnpj, "
					+ "name, "
					+ "password, "
					+ "matriculation, "
					+ "qtd "
					+ "FROM AD_ORG "
					+ "WHERE 1=1 "
					+ "AND matriculation IS NOT NULL "
					+ "AND ad_org_id IN (2000009, 2000014, 2000015, 2000017) " //Construtoras
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
					aDOrgs.add(aDOrgAux);
				}
				pst.close();
				rs.close();
				return aDOrgs;
			}catch (SQLException e) {
				e.printStackTrace();
				System.out.println(nomeDao+" erro!");
				return null;
			}
		}

}
