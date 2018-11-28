package main;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import model.dao.ADOrgDao;
import model.dao.RechargeDao;
import model.dao.ValeDao;
import model.dao.TrackingDao;
import model.entity.ADOrg;
import model.entity.Recharge;
import model.entity.Tracking;
import model.entity.Vale;

import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class _AtualizaRastreamento {
	private WebDriver driver;
	private LoggingPreferences logPreferences;
	private FirefoxOptions cd;
	private String firefoxBinaryPath;
	private String geckoDriverPath;
	private String urlPgInicial;
	private ADOrgDao aDOrgDao;
	private ValeDao valeDao;
	private TrackingDao trackingDao;
	private RechargeDao rechargeDao;
	
	@BeforeTest
	public void beforeTest() 
	{
		firefoxBinaryPath = "resource/firefox/firefox";
		geckoDriverPath = "resource/geckodriver";
		urlPgInicial = "https://www.vtefortaleza.com.br/site/forms/login/lateral.aspx";
		
		// GECKO DRIVER 
		System.setProperty("webdriver.gecko.driver", geckoDriverPath);	
		
		// FIREFOX DRIVER
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		firefoxProfile.setAcceptUntrustedCertificates(true);
		firefoxProfile.setAssumeUntrustedCertificateIssuer(true);		
		//Configura as capacidades desejadas com o profile
		cd = new FirefoxOptions();	
		cd.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
		cd.setCapability(FirefoxDriver.BINARY, firefoxBinaryPath);
		cd.setCapability("marionette", true);
		cd.setHeadless(true);
		// Desabilitar os textos vermelhos
		FirefoxDriverLogLevel logLevel = FirefoxDriverLogLevel.ERROR;
		cd.setLogLevel(logLevel);
		//Desabilitar Javascript
		DesiredCapabilities dc = new DesiredCapabilities();
        dc.setJavascriptEnabled(false);
		cd.merge(dc);	
	}
	  
	public void abrirFirefox() 
	{		
		//Configura o driver firefox com as capacidades desejadas
		driver = new FirefoxDriver(cd);
		driver.manage().deleteAllCookies();
		driver.manage().window().maximize();	  
	}
	  
	public void fecharFirefox() 
	{		
		driver.close();
	}
	  
	public void fecharFirefox(WebDriver driver) 
	{		
		driver.close();
	}
	
	public void logarPaginaInicial(String cnpj, String senha) 
	{
		driver.get(urlPgInicial);		
		cnpj = cnpj.replaceAll("[^0-9]", "");
		driver.findElement(By.id("TXT_CGC")).clear();	
		driver.findElement(By.id("TXT_CGC")).sendKeys(cnpj);		
		driver.findElement(By.id("TXT_Senha")).clear();
		driver.findElement(By.id("TXT_Senha")).sendKeys(senha); 		
		driver.findElement(By.id("CMD_Gravar")).click();		
		System.out.println("CNPJ: " + cnpj + " - Obtendo movimentações de funcionários...");
	}
	
	@Test(priority=1)
	public void consultar()
	{
		aDOrgDao = new ADOrgDao();
		valeDao = new ValeDao();
//		rechargeDao = new RechargeDao();
		trackingDao = new TrackingDao();
		
		//LIMPEZA DE RASTREAMENTOS C/ RECARGAS > 90 DIAS
		// Rastreamentos com recargas maiores que 90 dias serao removidos
		if ( trackingDao.deleteAllWithRechargeBiggerThanNinetyDays() )
			System.out.print("Rastreamentos sem recargas a mais de 90 dias foram removidos.");

		// Obtem os funcionarios que terao seus rastreamentos atualizados
//		recharges = rechargeDao.findFiveFirstUpdatedAndRechargeLessThenNinetyDays(aDOrg);
		
		for (ADOrg aDOrg : aDOrgDao.findAllADOrg())
		{
			System.out.println("Obtendo rastreamentos no site para empresa "+aDOrg.getMatriculation()+" - "
			+aDOrg.getName()+":");
			abrirFirefox();
			logarPaginaInicial(aDOrg.getLbrCnpj(), aDOrg.getPassword());
			
			int consultasPermitidas = 5; // Definicao do limite de consultas a pagina de rastreamento
			boolean isNovo = true;		 // Informa se e um novo rastreamento que sera inserido
	
			// Consulta primeiro os vales que nunca foram consultados
			consultasPermitidas = atualizaTracking(consultasPermitidas, isNovo, trackingDao.findAllWithDateTimeNull(aDOrg));
//			trackingDao.findAllWithDateTimeNull(aDOrg);
			
			if (consultasPermitidas > 0) {
				isNovo = false;
				// Agora consulta os vales que ja foram consultados e tem data de consulta antiga
				consultasPermitidas = atualizaTracking(consultasPermitidas, isNovo, trackingDao.findAllWithDateTimeNotNull(aDOrg));
			}
			
			fecharFirefox();
		}
		System.out.println();
		
		trackingDao.close();
//		rechargeDao.close();
		aDOrgDao = new ADOrgDao();
	}
	// FIM PROGRAMA	

	// REALIZA AS CONSULTAS PRINCIPAIS
	public int atualizaTracking(int consultasPermitidas, boolean isNovo, List<Tracking> trackings) {
		if (consultasPermitidas > 0)
			if (trackings.size()>0)					
//				for (int i=(trackings.size()<=consultasPermitidas)?(trackings.size()-1)
//				:(consultasPermitidas-1); i>0; i--)
//				{
//					consultasPermitidas = persisteTracking(trackings.get(i), consultasPermitidas);					
//					if (consultasPermitidas<=0)
//						break;
//				}
				if (trackings.size()<=consultasPermitidas)
					for (int i=0; i<trackings.size(); i++) {
						consultasPermitidas = persisteTracking(trackings.get(i), consultasPermitidas);					
						if (consultasPermitidas<=0)
							break;						
					}
				else
					for (int i=0; i<consultasPermitidas; i++) {
						consultasPermitidas = persisteTracking(trackings.get(i), consultasPermitidas);					
						if (consultasPermitidas<=0)
							break;						
					}
			else
				if (isNovo)
					System.out.println("Não há histórico de ratreamentos (trackings) à serem adicionados, pois não há recarga nos últimos dias.");
				else
					System.out.println("Não há histórico de ratreamentos (trackings) à serem atualizados, pois não há recarga nos últimos dias.");
		else
			System.out.println("Excedido o numero permitido de consultas.");
		
		return consultasPermitidas;
	}
	
	public int persisteTracking(Tracking tracking, int consultasPermitidas) {
		int categoria;
		String matEmpresaPesquisada = "00000", matFuncionarioPesquisada = "00000000", tableName;
		Date dataInicio, dataFim;
		Calendar calendar;		
		WebElement table;
		String url, cell;
		List<WebElement> trs;
		// Quantidade total de linhas na tabela do site
		int trs_total;
		// Pega a posicao da ultima linha onde houve informacao na coluna data da recarga
		int lastRechargeDateSiteRow = -1;
		
		Tracking lastTrackingSite=null, lastTrackingSiteAnterior=null, lastTrackingLocal;
		
		if ( tracking==null || tracking.getVale().getAdOrg().getMatriculation()==null 
		|| tracking.getRecharge().getOrderCategory()==null || tracking.getVale().getMatricula()==null )
			return consultasPermitidas;
		else
			switch (tracking.getRecharge().getOrderCategory().toLowerCase()) {
				case "urbano":
					categoria = 1;
					break;
				case "metropolitano":
					categoria = 2;
					break;
				default:
					categoria = -1;
					break;
			}
		if (categoria==-1)
			return consultasPermitidas;
		
		matEmpresaPesquisada = matEmpresaPesquisada.substring(0, matEmpresaPesquisada.length()-tracking.getVale().getAdOrg().getMatriculation().length()) + tracking.getVale().getAdOrg().getMatriculation();	
		matFuncionarioPesquisada = matFuncionarioPesquisada.substring(0, matFuncionarioPesquisada.length()-tracking.getVale().getMatricula().length()) + tracking.getVale().getMatricula();	
		
//		// Checa se o ultimo
//		lastTrackingLocal = trackingDao.findTrackingLastByMatricula(tracking.getVale().getAdOrg().getMatriculation(), tracking.getVale().getMatricula());
//		
//		if (lastTrackingLocal==null)
//			return;
		
		dataFim = new Date();
		
		calendar = new GregorianCalendar();
		calendar.setTime(dataFim);
		calendar.add(Calendar.DAY_OF_MONTH, -90);		
		dataInicio = calendar.getTime();
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");		    
		
		System.out.println("     matEmpresa: "+matEmpresaPesquisada+"  matFuncionario: " + matFuncionarioPesquisada + " ");
		url = "https://www.vtefortaleza.com.br/Forms/Relatorios/RasteamentoResultado.aspx?matricula=5"
		+matEmpresaPesquisada+matFuncionarioPesquisada+"&dtinicial="+sdf.format(dataInicio)+"&dtfinal="
		+sdf.format(dataFim)+"&categoria="+categoria;
		
		Path urlRastreamentoLocal = Paths.get("resource/testes/rastreamento/::VTE Fortaleza::..html");
//		url = urlRastreamentoLocal.toUri().toString(); //apenas para testes local, comentar

//		if (matFuncionarioPesquisada.substring(0, 7).equals("0000004")) //debug, o func 46 tinha recarga mas 
//			System.out.println(" UM MOMENTO ");							//nao tinha rastreamento
		
		try {
			try {
				consultasPermitidas--;
				driver.get(url);
				
				boolean fecharPopup = true;
				try {
					driver.switchTo().alert().accept();
				}catch (Exception ejs) {
					fecharPopup = false;
				}finally {
					if (fecharPopup) {
						System.out.println("Popup fechado. Não existe rastreio para matrícula "+tracking.getVale().getMatricula());
						trackingDao.saveTracking(tracking);
						return consultasPermitidas;
					}
				}
				
				try {
					//Empresa excedeu a quantidade máxima de 5 permitida por dia
					if (driver.findElement(By.id("Label1")).getText().substring(0, 15).equals("Empresa excedeu")) {
						System.out.println(tracking.getVale().getAdOrg().getName()+" excedeu a quantidade máxima de 5 consultas por dia.");
						consultasPermitidas = 0; //essa foi a ultima consulta, pois ja excedeu o limite
						return consultasPermitidas;
					}
				}catch (Exception el1){
//					el1.printStackTrace();
					System.out.println("Label1 não existe."); //label1 tambem existe quando da certo, entao
				}											  //essa exception nunca sera disparada
			}catch (Exception esg){
				esg.printStackTrace();
				System.out.println("Exception geral GET!");
			}
			
			tableName = "Label2";
			table = driver.findElement(By.id(tableName));
			trs = table.findElements(By.tagName("tr"));
			trs_total = trs.size();

			System.out.println("Obtendo dados de rastreamento no site:");	
			if (trs_total >= 3) {
				// Pega a ultima linha do site onde haja informacao na coluna data_hora no nosso database
				for (int i=trs_total; i>=3; i--)
				{
					cell = table.findElement(By.xpath("table/tbody/tr["+i+"]/td[1]")).getText().trim();					
					if ( cell.length()>=19
					&& !table.findElement(By.xpath("table/tbody/tr["+i+"]/td[4]")).getText().trim().equals("RECARGA")
					) { // inserir uma logica if para percorrer da primeira pos
						lastRechargeDateSiteRow = i;
						lastTrackingSite = new Tracking();
						lastTrackingSite.setDateTime(Timestamp.valueOf( formatarDataParaPersistir( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[1]")).getText().trim().substring(0, 19) ) ));
						lastTrackingSite.setValue( new BigDecimal( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[2]")).getText().trim().replace(",", ".") ) );
						lastTrackingSite.setLine( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[3]")).getText().trim().toLowerCase() );
						lastTrackingSite.setDebitType( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[4]")).getText().trim() );
						lastTrackingSite.setValeId(tracking.getVale().getId());
						lastTrackingSite.setVale(tracking.getVale());
						// Ele continuara no laco ate chegar ao primeiro rastreamento ou entao um
						//dos rastreamentos pelo caminho for igual/menor ao ultimo do database local e assim
						//pega a posicao da linha seguinte para percorrer a persistencia a partir dali
//						if (lastTrackingSite.getDateTime()!=null) {
//							if (lastTrackingSite.getDateTime().equals(tracking.getDateTime()))
//								break;
//							else if (lastTrackingSite.getDateTime().before(tracking.getDateTime())) {
//								lastTrackingSite = lastTrackingSiteAnterior;
//								break;
//							}
//						}
//						lastTrackingSiteAnterior = new Tracking();
//						lastTrackingSiteAnterior = lastTrackingSite;
//						if (lastTrackingSite.getDateTime().toString().substring(0, 10).equals("2018-01-08"))
//							System.out.print("um momento");
						if (lastTrackingSite.getDateTime()!=null && tracking.getDateTime()!=null
						&& ( 
						      lastTrackingSite.getDateTime().equals(tracking.getDateTime())
						      || lastTrackingSite.getDateTime().before(tracking.getDateTime())
						   )
						) {
							lastRechargeDateSiteRow++;
							break;
						}
					}
				}				
			}else {	
				System.out.println("Não há rastreamentos para esse período.");		
				return consultasPermitidas;
			}
			// fim tentativa
			
			
			if (lastTrackingSite == null) {
				System.out.println("Site não tem histórico de recargas. Não há dados de recarga do usuário para serem atualizados.");
				if (tracking.getDateTime()!=null) { //OPCIONAL
//					// Deleta todas os restreamentos referentes a esse vale
//		opcional=>	if ( trackingDao.deleteAll(tracking.getVale()) )
//						System.out.println("Remoção de rastreamentos realizada!");
//					else
//						System.out.println("Não foi possível remover os dados de rastreamento.");
				}
			}else {
				// Se o proximo elemento a ser buscado extrapolar a ultima linha, entao nao ha registros novos
				if (lastRechargeDateSiteRow > trs_total) {
					System.out.println("Não há novos rastreamentos desse período à serem atualizados para o usuário.");
					return consultasPermitidas;
				}else {
					// Caso o rastreamento ja exista no banco, comece no rastreamento seguinte (se houver)
					for (int i=lastRechargeDateSiteRow; i<=trs_total; i++)
					{
						lastTrackingSite = new Tracking();
						lastTrackingSite.setDateTime(Timestamp.valueOf( formatarDataParaPersistir( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[1]")).getText().trim().substring(0, 19) ) ));
						lastTrackingSite.setValue( new BigDecimal( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[2]")).getText().trim().replace(",", ".") ) );
						lastTrackingSite.setLine( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[3]")).getText().trim().toLowerCase() );
						lastTrackingSite.setDebitType( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[4]")).getText().trim() );
						lastTrackingSite.setValeId(tracking.getVale().getId());
						lastTrackingSite.setVale(tracking.getVale());
						
						// Se o rastreio for do tipo de debito RECARGA, pule para o proximo registro
						if (lastTrackingSite.getDebitType().equals("RECARGA") 
						|| lastTrackingSite.getDebitType().equals("INTEGRAÇÃO"))
							continue;
						
						// Inicio da persistencia, caso haja dados novos
						if (lastTrackingSite.getDateTime().toString().length() >= 19) {
							// Se a table Recharge tiver vazia
							if (tracking.getDateTime() == null) {
								// Persiste os dados do site diretamente no database
								if ( trackingDao.saveTracking(lastTrackingSite) )
									System.out.println("Inserção de recarga para DataHora "+lastTrackingSite.getDateTime()+" realizada!");
								else
									System.out.println("Não foi possível inserir os dados de recarga.");
							}else{
								// Caso a  data de recarga no database nao seja igual ao do site, atualize
								// Esse primeiro if nunca sera executado, pois a iteracao sempre comecara no
								//registro posterior, caso seja igual. Isso foi tratado anteriormente. Mas
								//sera deixado ai para caso haja alteracoes futuras que possam impactar.
								if ( tracking.getDateTime().equals(lastTrackingSite.getDateTime())
//									 && (tracking.getVale().getTipoVale().equals(categoria)) 
								) {
									System.out.println("Os dados de rastreio do usuário já estavam atualizados.");
								}else{
									// Comeca a atualizar a tabela Tracking
									if ( trackingDao.saveTracking(lastTrackingSite) )
										System.out.println("Inserção de rastreio ("
										+lastTrackingSite.getDateTime()+") realizada!");
									else
										System.out.println("Não foi possível inserir dados de rastreio.");
								}
							}
						}
						// fim da persistencia
					}
				}
			}			
		}catch (Exception ee) {
			ee.printStackTrace();
			fecharFirefox();
			System.out.println("CATCH: ERRO!!: ");
			trackingDao.close();
			rechargeDao.close();
			valeDao.close();
			aDOrgDao.close();
		}
		return consultasPermitidas;
	}
	
	private String formatarDataParaPersistir(String data) {
		String dia, mes, ano;
		try {
//			data = data.substring(0, 10);
			dia = data.substring( data.indexOf('/')-2, data.indexOf('/') );
			mes = data.substring( data.indexOf('/')+1, data.indexOf('/', data.indexOf('/')+1) );
			ano = data.substring( data.indexOf('/', data.indexOf('/')+1)+1, data.indexOf('/', data.indexOf('/')+1)+5 );		
			data = ano+"-"+mes+"-"+dia+(data.length()>10?data.substring(10):" 00:00:00.0");
		}catch(Exception e) {
			e.printStackTrace();
			System.out.println("Erro na formatação de datas.");
		}
		return data;
	}

}