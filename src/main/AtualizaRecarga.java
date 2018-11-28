package main;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
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

public class AtualizaRecarga {
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
		
		// Desabilitar os textos vermelhos
		FirefoxDriverLogLevel logLevel = FirefoxDriverLogLevel.ERROR;	
		
		// Desabilitar Javascript
		DesiredCapabilities dc = new DesiredCapabilities();
        dc.setJavascriptEnabled(false);
		
		// FIREFOX DRIVER
		FirefoxProfile firefoxProfile = new FirefoxProfile();
		firefoxProfile.setAcceptUntrustedCertificates(true);
		firefoxProfile.setAssumeUntrustedCertificateIssuer(true);
		//Configura as capacidades desejadas com o profile
		cd = new FirefoxOptions();
		cd.merge(dc);
//		cd.setCapability(FirefoxDriver.SystemProperty.BROWSER_LOGFILE, "/dev/null"); nao usado
		cd.setLogLevel(logLevel);
		cd.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
		cd.setCapability(FirefoxDriver.BINARY, firefoxBinaryPath);
		cd.setCapability("marionette", true);
		cd.setHeadless(true);
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
		rechargeDao = new RechargeDao();
		trackingDao = new TrackingDao();
		
		for (ADOrg aDOrg : aDOrgDao.findAllADOrg()) 
		{
			abrirFirefox();
			logarPaginaInicial(aDOrg.getLbrCnpj(), aDOrg.getPassword());
			
			if ( (aDOrg.getAdOrgId()==null) || (aDOrg.getMatriculation()==null) )
				break;
			
			if (aDOrg.getQtd()>0)
			{
				System.out.println("Obtendo recargas no site para empresa "+aDOrg.getMatriculation()+" - "
				+aDOrg.getName()+":");
				for (int matFuncionarioSimples=aDOrg.getQtd(); matFuncionarioSimples>0; matFuncionarioSimples--) 
				{
					atualizaRecarga(aDOrg, String.valueOf(matFuncionarioSimples));
				}				
			}else{
				System.out.print("Não existem funcionários que usem vale.");
			}
			fecharFirefox();
		}
		
		System.out.print("TODAS RECARGAS ATUALIZADAS!");
		
		trackingDao.close();
		rechargeDao.close();
		valeDao.close();
		aDOrgDao.close();
	}
	// FIM PROGRAMA
	
	public void atualizaRecarga(ADOrg aDOrg, String matFuncionarioSimples) {
		Vale vale;
		String matFuncionarioPesquisada = "00000000", tableName;
		String nomeFuncionario, nExterno;
		boolean isCadastrado = false;
		
		WebElement table = null;
		String url, cell;
		List<WebElement> trs;
		// Quantidade total de linhas na tabela do site
		int trs_total = 0;
		// Pega a posicao da ultima linha onde houve informacao na coluna data da recarga
		int lastRechargeDateSiteRow = -1;
		
		Recharge lastRechargeSite=null, lastRechargeLocal;
		
		matFuncionarioPesquisada = matFuncionarioPesquisada.substring(0, matFuncionarioPesquisada.length()-matFuncionarioSimples.length()) + matFuncionarioSimples;
		url = "https://www.vtefortaleza.com.br/Forms/Relatorios/RelatorioMovimentacaoUsuarioResultado.aspx?matricula="+matFuncionarioPesquisada;
		
		try {
//			driver.manage().timeouts().implicitlyWait(200, TimeUnit.MILLISECONDS);
			driver.get(url);
//			new WebDriverWait(driver, pageLoadTimeout).until(
//			          webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete"));
			
			boolean fecharPopup = true;
			try {
				driver.switchTo().alert().accept();
			}catch (Exception ejs) {
				fecharPopup = false;
			}finally {
				if (fecharPopup) {
					System.out.println("Popup fechado. Não existe matrícula "+matFuncionarioSimples);
					return;
				}
			}			
		}catch (Exception esg){
			esg.printStackTrace();
			System.out.println("Exception geral GET!");
		}
		
		try {
			tableName = "TXT_SpanPedido";
			table = driver.findElement(By.id(tableName));
//			table = new WebDriverWait(driver, 1).until(ExpectedConditions.presenceOfElementLocated(By.id(tableName)));
			
			if (table==null) {
				System.out.println("Não foi possível obter tabela");
				return;
			}
			trs = table.findElements(By.tagName("tr"));
			trs_total = trs.size();

			if (trs_total >= 2) {	
				
				vale = valeDao.findOneVale(aDOrg, matFuncionarioSimples);
				
				if (vale==null) {
					System.out.println("Matrícula "+matFuncionarioSimples+" não existe na tabela VALE.");

					try {
						nomeFuncionario = driver.findElement(By.id("nome")).getText().trim();
						nExterno = driver.findElement(By.id("externo")).getText().trim();
						
						isCadastrado = valeDao.saveVale(aDOrg.getAdOrgId(), matFuncionarioSimples, nomeFuncionario, nExterno);

						if (isCadastrado) {
							System.out.println("Matrícula "+matFuncionarioSimples+" foi cadastrada na tabela VALE!");
							vale = valeDao.findOneVale(aDOrg, matFuncionarioSimples);
						}else
							System.out.println("Matrícula "+matFuncionarioSimples+" não foi cadastrada na tabela VALE!");
//						return;
						
					}catch(Exception ed) {
						ed.printStackTrace();
						System.out.println("Espera ex ed");
					}
					
				}else {
					System.out.print("Matrícula "+matFuncionarioSimples+" ("+vale.getNome()+"). ");
				}
				
//				return; //TEMPORARIO 1 
				// Pega a ultima linha onde haja informacao na coluna data da recarga
				for (int i=trs_total; i>=2; i--)
				{
					cell = table.findElement(By.xpath("table/tbody/tr["+i+"]/td[6]")).getText().trim();
					// Checa se a coluna data de recarga contem dia, mes e ano
					if (cell.length()>=10) {
						lastRechargeSite = new Recharge();
						lastRechargeSite.setOrderNumber(new BigDecimal( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[1]")).getText().trim() ));
						lastRechargeSite.setValue( new BigDecimal( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[2]")).getText().trim().replace(",", ".") ) );
						lastRechargeSite.setOrderDate(Timestamp.valueOf( formatarDataParaPersistir( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[3]")).getText().trim().substring(0, 10) ) ));
						lastRechargeSite.setOrderCategory( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[5]")).getText().trim().toLowerCase() );
						lastRechargeSite.setRechargeDate(Timestamp.valueOf( formatarDataParaPersistir( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[6]")).getText().trim().substring(0, 10) ) ));
						lastRechargeSite.setCarLineWay( table.findElement(By.xpath("table/tbody/tr["+i+"]/td[7]")).getText().trim() );
						lastRechargeSite.setValeId(vale.getId());
						lastRechargeSite.setVale(vale);
						break;
					}
				}
				
			}else {
				System.out.println("Não há recarga para a matrícula "+matFuncionarioSimples+".");		
				return;
			}
			
			//TEMPORARIO 2
			lastRechargeLocal = rechargeDao.findLastRecharge(aDOrg, vale);
			
			if (lastRechargeSite == null) {
				System.out.println("Site sem histórico de recarga.");
				if (lastRechargeLocal!=null) {
					// Deleta todas as recargas referentes a esse vale
					rechargeDao.deleteRecharge(vale);
				}
				return;
			}else {
				// Inicio da persistencia, caso haja dados novos
				if (lastRechargeSite.getRechargeDate().toString().length() >= 10) {
					// Se a table Recharge tiver vazia
					if (lastRechargeLocal == null) {
						// Persiste os dados do site diretamente no database
//						persistRechargeTable(vale, lastRechargeLocal, tableName, 2, lastRechargeDateSiteRow);
						if ( rechargeDao.saveRecharge(lastRechargeSite) )
							System.out.println("Inserção de recarga realizada!");
						else
							System.out.println("Não foi possível inserir os dados de recarga.");						
					}else{
						// Caso a ultima data de recarga no database nao seja igual ao do site, atualize
						if ( lastRechargeLocal.getRechargeDate().toString().substring(0, 10).equals(
							 lastRechargeSite.getRechargeDate().toString().substring(0, 10))
//							 && (lastRechargeLocal.getOrderCategory().equals(lastRechargeSite.getOrderCategory())) 
						) {
							System.out.println("Dados de recarga já atualizados.");
							return;
						}else{
							// Comeca a atualizar a tabela Recharge a partir do 
							//registro de recarga no site que ainda nao foi persistido
//							persistRechargeTable(vale, lastRechargeLocal, tableName, 2, lastRechargeDateSiteRow);
							if ( rechargeDao.updateRecharge(lastRechargeSite) )
								System.out.println("Atualização de recarga realizada!");
							else
								System.out.println("Não foi possível inserir os dados de recarga.");
						}
					}
				}
				// fim da persistencia
			}
		}catch (Exception ee) {
			ee.printStackTrace();
			fecharFirefox();
			System.out.println("atualizaRecarga() ERRO!!: ");
			trackingDao.close();
			rechargeDao.close();
			valeDao.close();
			aDOrgDao.close();
		}		
//		try { Thread.sleep(500); } catch (Exception es) { es.printStackTrace(); }
	}
	
	private String formatarDataParaPersistir(String data) {
		String dia, mes, ano;
		try {
		data = data.substring(0, 10);
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
