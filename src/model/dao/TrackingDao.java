package model.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import model.entity.ADOrg;
import model.entity.Recharge;
import model.entity.Vale;
import model.entity.Tracking;

public class TrackingDao
{
	private String nomeDao = "TrackingDao";
	private Connection conexao;
	
	public TrackingDao()
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
	
	// LER ALGUMA OCORRENCIA PELA OrgMatriculation e ValeMatricula
	public Tracking findOneByOrgMatriculationByValeMatricula(String orgMatriculation, String valeMatricula)	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		Tracking tracking = null;
		Vale vale = null;
		ADOrg adOrg = null;
		
		comandoSQL = ""
				+ "SELECT v.ad_org_id, o.matriculation, t.vale_id, v.matricula, min(t.date_time) as date_time "
				+ "FROM tracking t LEFT JOIN vale v ON (t.vale_id = v.id) "
				+ "LEFT JOIN AD_ORG o ON (v.ad_org_id = o.ad_org_id) "
				+ "WHERE 1=1 "
				+ "AND o.matriculation like ? "
				+ "AND v.matricula like ? "
				+ "GROUP BY v.ad_org_id, o.matriculation, t.vale_id, v.matricula "
				+ "LIMIT 1 "
				+ ";";
		
		try	{
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, orgMatriculation);
			pst.setString(2, valeMatricula);
			rs = pst.executeQuery();
			while (rs.next()) {
				tracking = new Tracking();
				tracking.setId(rs.getLong("id"));
				tracking.setDateTime(rs.getTimestamp("date_time"));
				tracking.setValue(rs.getBigDecimal("value"));
				tracking.setLine(rs.getString("line"));
				tracking.setDebitType(rs.getString("debit_type"));
				tracking.setDateTime(rs.getTimestamp("updated"));
				
				tracking.setValeId(rs.getLong("vale_id"));
				vale = new Vale();
				vale.setId(rs.getLong("vale_id"));
				vale.setMatricula(rs.getString("matricula"));
				vale.setNome(rs.getString("nome"));
				vale.setNumeroExterno(rs.getString("numero_externo"));
				vale.setTipoVale(rs.getString("tipo_vale"));
				
				vale.setAdOrgId(rs.getLong("ad_org_id")); //pai cartao					
				adOrg = new ADOrg();
				adOrg.setAdOrgId(rs.getLong("ad_org_id"));  //filho adOrg
				adOrg.setLbrCnpj(rs.getString("lbr_cnpj"));
				adOrg.setName(rs.getString("name"));
				adOrg.setPassword(rs.getString("password"));
				adOrg.setMatriculation(rs.getString("matriculation"));
				
				vale.setAdOrg(adOrg);
				tracking.setVale(vale);
				
				break;
			}
			pst.close();
			rs.close();
			return tracking;
		}catch (SQLException e) {
//			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	// LER 5 ULTIMOS ATUAIS PELA COLUNA DATE_TIME
	public List<Tracking> findFiveFirstUpdated(ADOrg aDOrg)	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		Tracking tracking = null;
		Vale vale = null;
		ADOrg adOrg = null;
		List<Tracking> trackings = new ArrayList<Tracking>();
		
		comandoSQL = ""
				+ "SELECT "
				+ "t.id, t.date_time, t.value, t.line, t.debit_type, t.updated, t.vale_id, "
				+ "v.matricula, v.nome, v.numero_externo, v.tipo_vale, v.ad_org_id, "
				+ "o.lbr_cnpj, o.name, o.password, o.matriculation "
				+ "FROM tracking t "
				+ "LEFT JOIN vale v ON (t.vale_id = v.id) "
				+ "LEFT JOIN AD_ORG o ON (v.ad_org_id = o.ad_org_id) "
				+ "WHERE 1=1 "
				+ "AND o.matriculation like ? "
				+ "ORDER BY v.matricula ASC "
				+ "LIMIT 5 "
				+ ";";
		
		try	{
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, aDOrg.getMatriculation());
			rs = pst.executeQuery();
			while (rs.next()) {
				tracking = new Tracking();
				tracking.setId(rs.getLong("id"));
				tracking.setDateTime(rs.getTimestamp("date_time"));
				tracking.setValue(rs.getBigDecimal("value"));
				tracking.setLine(rs.getString("line"));
				tracking.setDebitType(rs.getString("debit_type"));
				tracking.setDateTime(rs.getTimestamp("updated"));
				
				tracking.setValeId(rs.getLong("vale_id"));
				vale = new Vale();
				vale.setId(rs.getLong("vale_id"));
				vale.setMatricula(rs.getString("matricula"));
				vale.setNome(rs.getString("nome"));
				vale.setNumeroExterno(rs.getString("numero_externo"));
				vale.setTipoVale(rs.getString("tipo_vale"));
				
				vale.setAdOrgId(rs.getLong("ad_org_id")); //pai cartao					
				adOrg = new ADOrg();
				adOrg.setAdOrgId(rs.getLong("ad_org_id"));  //filho adOrg
				adOrg.setLbrCnpj(rs.getString("lbr_cnpj"));
				adOrg.setName(rs.getString("name"));
				adOrg.setPassword(rs.getString("password"));
				adOrg.setMatriculation(rs.getString("matriculation"));
				
				vale.setAdOrg(adOrg);
				tracking.setVale(vale);
				
				trackings.add(tracking);
			}
			pst.close();
			rs.close();
			return trackings;
		}catch (SQLException e) {
//			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	// LER ULTIMO PELA COLUNA DATE_TIME
	public Tracking findTrackingLastByMatricula(String orgMatriculation, String valeMatricula)	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		Tracking tracking = null;
		Vale vale = null;
		ADOrg adOrg = null;
		List<Tracking> trackings = new ArrayList<Tracking>();
		
		comandoSQL = ""
				+ "SELECT v.ad_org_id, o.matriculation, t.vale_id, v.matricula, min(t.date_time) as date_time "
				+ "FROM tracking t LEFT JOIN vale v ON (t.vale_id = v.id) "
				+ "LEFT JOIN AD_ORG o ON (v.ad_org_id = o.ad_org_id) "
				+ "WHERE 1=1 "
				+ "AND o.matriculation like ? "
				+ "AND v.matricula like ? "
				+ "GROUP BY v.ad_org_id, o.matriculation, t.vale_id, v.matricula "
				+ "LIMIT 1 "
				+ ";";
		
		try	{
			pst = conexao.prepareStatement(comandoSQL);
			pst.setString(1, orgMatriculation);
			pst.setString(2, valeMatricula);
			rs = pst.executeQuery();
			while (rs.next()) {
				tracking = new Tracking();
//				tracking.setId(rs.getLong("id"));
				tracking.setDateTime(rs.getTimestamp("date_time"));
//				tracking.setValue(rs.getBigDecimal("value"));
//				tracking.setLine(rs.getString("line"));
//				tracking.setDebitType(rs.getString("debit_type"));
//				tracking.setDateTime(rs.getTimestamp("updated"));
				
				tracking.setValeId(rs.getLong("vale_id"));
				vale = new Vale();
				vale.setId(rs.getLong("vale_id"));
				vale.setMatricula(rs.getString("matricula"));
//				vale.setNome(rs.getString("nome"));
//				vale.setNumeroExterno(rs.getString("numero_externo"));
				
				vale.setAdOrgId(rs.getLong("ad_org_id")); //pai cartao					
				adOrg = new ADOrg();
				adOrg.setAdOrgId(rs.getLong("ad_org_id"));  //filho adOrg
//				adOrg.setLbrCnpj(rs.getString("lbr_cnpj"));
//				adOrg.setName(rs.getString("name"));
//				adOrg.setPassword(rs.getString("password"));
				adOrg.setMatriculation(rs.getString("matriculation"));
				vale.setAdOrg(adOrg);
				tracking.setVale(vale);
				
				trackings.add(tracking);
			}
			pst.close();
			rs.close();
			return trackings.get(0);
		}catch (SQLException e) {
//			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	// Retorna todos os registros com coluna date_time nulos
	public List<Tracking> findAllWithDateTimeNull(ADOrg aDOrg) {
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		Tracking tracking = null;
		Vale valeAux = null;
		ADOrg adOrgAux = null;
		Recharge rechargeAux = null;
		List<Tracking> trackings = new ArrayList<Tracking>();
		
		comandoSQL = ""
				+ "SELECT "
				+ "t.id AS tId, "
				+ "t.date_time AS tDateTime, "
				+ "t.value AS tValue, "
				+ "t.line AS tLine, "
				+ "t.debit_type AS tDebitType, "
				+ "t.updated AS tUpdated, "
				+ "t.vale_id AS tValeId, "
				+ "v.id AS vId, "
				+ "v.matricula AS vMatricula, "
				+ "v.nome AS vNome, "
				+ "v.numero_externo AS vNumeroExterno, "
				+ "v.tipo_vale AS vTipoVale, "
				+ "v.ad_org_id AS vADOrgId, "
				+ "o.lbr_cnpj AS oLbrCnpj, "
				+ "o.name AS oName, "
				+ "o.password AS oPassword, "
				+ "o.matriculation AS oMatriculation, "
				+ "r.vale_id AS rValeId, "
				+ "r.id AS rId, "
				+ "r.order_number AS rOrderNumber, "
				+ "r.value AS rValue, "
				+ "r.order_date AS rOrderDate, "
				+ "r.order_category AS rOrderCategory, "
				+ "r.recharge_date AS rRechargeDate, "
				+ "r.car_line_way AS rCarLineWay, "
				+ "r.updated AS rUpdated "
				+ "FROM recharge r "
				+ "LEFT JOIN tracking t ON (r.vale_id = t.vale_id) "
				+ "LEFT JOIN vale v ON (r.vale_id = v.id) "
				+ "LEFT JOIN ad_org o ON (v.ad_org_id = o.ad_org_id) "
				+ "WHERE 1=1 "
				+ "AND (v.ad_org_id = ?) "
				+ "AND (t.updated IS NULL) "
				+ "AND (DATE_PART('day', NOW()::TIMESTAMP WITHOUT TIME ZONE - r.recharge_date) <= 90) "
				+ "LIMIT 5 "
				+ ";";
		try
		{	
			pst = conexao.prepareStatement(comandoSQL);
			pst.setLong(1, aDOrg.getAdOrgId());
			rs = pst.executeQuery();
			while (rs.next()) {
				tracking = new Tracking();
//				tracking.setId(rs.getLong("tId"));
				tracking.setDateTime(rs.getTimestamp("tDateTime"));
				tracking.setValue(rs.getBigDecimal("tValue"));
				tracking.setLine(rs.getString("tLine"));
				tracking.setDebitType(rs.getString("tDebitType"));
				tracking.setUpdated(rs.getTimestamp("tUpdated"));
				
				tracking.setValeId(rs.getLong("vId"));
				valeAux = new Vale();
				valeAux.setId(rs.getLong("vId"));
				valeAux.setMatricula(rs.getString("vMatricula"));
				valeAux.setNome(rs.getString("vNome"));
				valeAux.setNumeroExterno(rs.getString("vNumeroExterno"));
				valeAux.setTipoVale(rs.getString("vTipoVale"));
				
				valeAux.setAdOrgId(rs.getLong("vADOrgId")); //pai cartao					
				adOrgAux = new ADOrg();
				adOrgAux.setAdOrgId(rs.getLong("vADOrgId"));  //filho adOrg
				adOrgAux.setLbrCnpj(rs.getString("oLbrCnpj"));
				adOrgAux.setName(rs.getString("oName"));
				adOrgAux.setPassword(rs.getString("oPassword"));
				adOrgAux.setMatriculation(rs.getString("oMatriculation"));
				
				valeAux.setAdOrg(adOrgAux);
				tracking.setVale(valeAux);
				
				rechargeAux = new Recharge();
				rechargeAux.setId(rs.getLong("rId"));
				rechargeAux.setOrderNumber(rs.getBigDecimal("rOrderNumber"));
				rechargeAux.setValue(rs.getBigDecimal("rValue"));
				rechargeAux.setOrderDate(rs.getTimestamp("rOrderDate"));
				rechargeAux.setOrderCategory(rs.getString("rOrderCategory"));
				rechargeAux.setRechargeDate(rs.getTimestamp("rRechargeDate"));
				rechargeAux.setCarLineWay(rs.getString("rCarLineWay"));
				rechargeAux.setValeId(rs.getLong("vId"));
				rechargeAux.setUpdated(rs.getTimestamp("rUpdated"));
				rechargeAux.setVale(valeAux);

				tracking.setRecharge(rechargeAux);
				
				trackings.add(tracking);
			}
			pst.close();
			rs.close();
			return trackings;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}
	
	// Retorna todos os registros com coluna date_time nao nulos
	public List<Tracking> findAllWithDateTimeNotNull(ADOrg aDOrg) {
		String comandoSQL = "";
		PreparedStatement pst = null;
		ResultSet rs = null;
		Tracking tracking = null;
		Vale valeAux = null;
		ADOrg adOrgAux = null;
		Recharge rechargeAux = null;
		List<Tracking> trackings = new ArrayList<Tracking>();
		
		comandoSQL = ""
				+ "SELECT "
				+ "t.id AS tId, "
				+ "t.date_time AS tDateTime, "
				+ "t.value AS tValue, "
				+ "t.line AS tLine, "
				+ "t.debit_type AS tDebitType, "
				+ "t.updated AS tUpdated, "
				+ "t.vale_id AS tValeId, "
				+ "v.id AS vId, "
				+ "v.matricula AS vMatricula, "
				+ "v.nome AS vNome, "
				+ "v.numero_externo AS vNumeroExterno, "
				+ "v.tipo_vale AS vTipoVale, "
				+ "v.ad_org_id AS vADOrgId, "
				+ "o.lbr_cnpj AS oLbrCnpj, "
				+ "o.name AS oName, "
				+ "o.password AS oPassword, "
				+ "o.matriculation AS oMatriculation, "
				+ "r.vale_id AS rValeId, "
				+ "r.id AS rId, "
				+ "r.order_number AS rOrderNumber, "
				+ "r.value AS rValue, "
				+ "r.order_date AS rOrderDate, "
				+ "r.order_category AS rOrderCategory, "
				+ "r.recharge_date AS rRechargeDate, "
				+ "r.car_line_way AS rCarLineWay, "
				+ "r.updated AS rUpdated "
				+ "FROM recharge r "
				+ "LEFT JOIN tracking t ON (r.vale_id = t.vale_id) "
				+ "LEFT JOIN vale v ON (r.vale_id = v.id) "
				+ "LEFT JOIN ad_org o ON (v.ad_org_id = o.ad_org_id) "
				+ "WHERE 1=1 "
				+ "AND (v.ad_org_id = ?) "
				+ "AND t.updated IS NOT NULL "
				+ "AND t.date_time = ( "
				+ "		select MAX(tt.date_time) "
				+ "		from tracking tt "
				+ "		where tt.vale_id = t.vale_id "
				+ ") "
				+ "AND (DATE_PART('day', NOW()::TIMESTAMP WITHOUT TIME ZONE - r.recharge_date) <= 90) "
//				+ "AND t.updated::date < NOW()::date "
				+ "ORDER BY t.updated " // opcional (evita que um rastreamento ja consultado no dia seja novamente)
				+ "LIMIT 5 "
				+ ";";
		try
		{	
			pst = conexao.prepareStatement(comandoSQL);
			pst.setLong(1, aDOrg.getAdOrgId());
			rs = pst.executeQuery();
			while (rs.next()) {
				tracking = new Tracking();
//				tracking.setId(rs.getLong("tId"));
				tracking.setDateTime(rs.getTimestamp("tDateTime"));
				tracking.setValue(rs.getBigDecimal("tValue"));
				tracking.setLine(rs.getString("tLine"));
				tracking.setDebitType(rs.getString("tDebitType"));
				tracking.setUpdated(rs.getTimestamp("tUpdated"));
				
				tracking.setValeId(rs.getLong("vId"));
				valeAux = new Vale();
				valeAux.setId(rs.getLong("vId"));
				valeAux.setMatricula(rs.getString("vMatricula"));
				valeAux.setNome(rs.getString("vNome"));
				valeAux.setNumeroExterno(rs.getString("vNumeroExterno"));
				valeAux.setTipoVale(rs.getString("vTipoVale"));
				
				valeAux.setAdOrgId(rs.getLong("vADOrgId")); //pai cartao					
				adOrgAux = new ADOrg();
				adOrgAux.setAdOrgId(rs.getLong("vADOrgId"));  //filho adOrg
				adOrgAux.setLbrCnpj(rs.getString("oLbrCnpj"));
				adOrgAux.setName(rs.getString("oName"));
				adOrgAux.setPassword(rs.getString("oPassword"));
				adOrgAux.setMatriculation(rs.getString("oMatriculation"));
				
				valeAux.setAdOrg(adOrgAux);
				tracking.setVale(valeAux);
				
				rechargeAux = new Recharge();
				rechargeAux.setId(rs.getLong("rId"));
				rechargeAux.setOrderNumber(rs.getBigDecimal("rOrderNumber"));
				rechargeAux.setValue(rs.getBigDecimal("rValue"));
				rechargeAux.setOrderDate(rs.getTimestamp("rOrderDate"));
				rechargeAux.setOrderCategory(rs.getString("rOrderCategory"));
				rechargeAux.setRechargeDate(rs.getTimestamp("rRechargeDate"));
				rechargeAux.setCarLineWay(rs.getString("rCarLineWay"));
				rechargeAux.setValeId(rs.getLong("vId"));
				rechargeAux.setUpdated(rs.getTimestamp("rUpdated"));
				rechargeAux.setVale(valeAux);

				tracking.setRecharge(rechargeAux);
				
				trackings.add(tracking);
			}
			pst.close();
			rs.close();
			return trackings;
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			System.out.println(nomeDao+" erro!");
			return null;
		}
	}

	// SAVE
	public boolean saveTracking(Tracking tracking) {
		String comandoSQL = "";
		PreparedStatement pst = null;
		
		comandoSQL = ""
				+ "INSERT INTO tracking(date_time, value, line, debit_type, vale_id, updated) "
				+ "VALUES (?, ?, ?, ?, ?, NOW()) "
				+ ";";
		try
		{
			pst = conexao.prepareStatement(comandoSQL);
			pst.setTimestamp(1, tracking.getDateTime());
			pst.setBigDecimal(2, tracking.getValue());
			pst.setString(3, tracking.getLine());
			pst.setString(4, tracking.getDebitType());
			pst.setLong(5, tracking.getValeId());
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
	
	public boolean saveTracking(Timestamp dataHora, BigDecimal valor, String linha, 
	String tipoDebito, Long valeId)
	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		
		comandoSQL = ""
				+ "INSERT INTO tracking(date_time, value, line, debit_type, vale_id, updated) "
				+ "VALUES (?, ?, ?, ?, ?, NOW()) "
				+ ";";		
		try
		{			
			pst = conexao.prepareStatement(comandoSQL);
			pst.setTimestamp(1, dataHora);
			pst.setBigDecimal(2, valor);
			pst.setString(3, linha);
			pst.setString(4, tipoDebito);
			pst.setLong(5, valeId);
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
	
	// UPDATE
	public boolean updateTracking(Tracking tracking)
	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		
		comandoSQL = ""
				+ "UPDATE vale set " // TERMINAR, SUBST. VARIAVEIS
				+ "date_time 	= ?, "
				+ "value 		= ?, "
				+ "line 		= ?, "
				+ "debit_type 	= ?, "
				+ "vale_id 		= ?, "
				+ "updated 		= NOW() "
				+ "WHERE 1=1 "
				+ "AND (vale_id = ?) "
				+ ";";
		try
		{			
			pst = conexao.prepareStatement(comandoSQL);
			pst.setTimestamp(1, tracking.getDateTime());
			pst.setBigDecimal(2, tracking.getValue());
			pst.setString(3, tracking.getLine());
			pst.setString(4, tracking.getDebitType());
			pst.setLong(5, tracking.getValeId());
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
	
	// Remove os trackings que possuem tempo de recarga maior que 90 dias
	public boolean deleteAllWithRechargeBiggerThanNinetyDays() {
		String comandoSQL = "";
		PreparedStatement pst = null;
		
		comandoSQL = ""
				+ "DELETE FROM tracking t "
				+ "WHERE t.vale_id IN ("
				+ "		SELECT r.vale_id "
				+ "		FROM vale v "
				+ "		LEFT JOIN AD_ORG o ON (v.ad_org_id = o.ad_org_id) "
				+ "		RIGHT JOIN Recharge r ON (v.id = r.vale_id) "
				+ "		WHERE 1=1 "
				+ "		AND (DATE_PART('day', NOW()::TIMESTAMP WITHOUT TIME ZONE - r.recharge_date) > 90) "
				+ "		ORDER BY o.ad_org_id, r.vale_id "
				+ ");";
		try
		{			
			pst = conexao.prepareStatement(comandoSQL);
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
	public boolean deleteAll(Vale vale)
	{
		String comandoSQL = "";
		PreparedStatement pst = null;
		
		comandoSQL = ""
				+ "DELETE "
				+ "FROM Tracking "
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
