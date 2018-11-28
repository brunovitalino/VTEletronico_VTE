package main;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import model.dao.ValeDao;
import model.entity.Vale;

public class AtualizaSaldo {
	private WebDriver driver;
	private FirefoxOptions cd;
	private String baseUrl;
	private ValeDao cartaoDao;

  @BeforeTest
  public void beforeTest() {	  
	  String firefoxBinaryPath = "resource/firefox/firefox";
	  String geckoDriverPath = "resource/geckodriver";
	  baseUrl = "https://www4.libercard.com.br";
	  
	  // GECKO DRIVER 
	  //Necessario alterar antes o tipo de execucao do geckodriver: 
	  //sudo chmod +x resource/geckodriver
	  System.setProperty("webdriver.gecko.driver", geckoDriverPath);	  
	  
	  // FIREFOX DRIVER	  
	  //Configura profile
//	  ProfilesIni profileIni = new ProfilesIni();
	  FirefoxProfile firefoxProfile = new FirefoxProfile();
	  firefoxProfile.setAcceptUntrustedCertificates(true);
	  firefoxProfile.setAssumeUntrustedCertificateIssuer(true);	  
	  //Configura as capacidades desejadas com o profile
	  cd = new FirefoxOptions();
	  cd.setCapability(FirefoxDriver.PROFILE, firefoxProfile);
	  cd.setCapability(FirefoxDriver.BINARY, firefoxBinaryPath);
	  cd.setCapability("marionette", true);		
  }
  
  public void abrirFirefox() {
	  //Configura o driver firefox com as capacidades desejadas
	  driver = new FirefoxDriver(cd);
	  driver.manage().deleteAllCookies();
	  driver.manage().window().maximize();	  
  }
  
  public void fecharFirefox() {
	  driver.close();
  }
  
  public void loginPaginaInicial() {
	abrirFirefox();
//	driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
//	driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
  	driver.get(baseUrl + "/apex/f?p=150:20::::::");
//  driver.manage().window().setSize(new Dimension(1920, 1080));
//  driver.manage().window().maximize();
    driver.findElement(By.id("P20_CPF")).clear();
    driver.findElement(By.id("P20_CPF")).sendKeys("17095336334");
    driver.findElement(By.id("P20_SENHA")).clear();
    driver.findElement(By.id("P20_SENHA")).sendKeys("senha123"); 
	driver.findElement(By.id("P20_LOGIN")).click();
  }

  @Test(priority=1)
  public void updateAllVTSaldo() {  	
  	cartaoDao = new ValeDao();
  	List<Vale> cartoes = null;
  	int nConsultas = 0;
  	
  	cartoes = cartaoDao.findAllVale();

  	loginPaginaInicial();
  	
  	for(Vale cartao : cartoes){  		
		if (nConsultas >= 100) {
			fecharFirefox();
			loginPaginaInicial();
			nConsultas = 0;
		}
		
		atualizarVTSaldo(cartao, nConsultas);
		try { Thread.sleep(500); } catch (Exception es) { es.printStackTrace(); }
		nConsultas++;
	}
	
	cartaoDao.close();
  }  
  
  // CADASTRAR CARTAO
  public void atualizarVTSaldo(Vale cartao, int nConsultas) {
	    String numeroExternoDoSite = "";
	  
	  	try {
	  		driver.findElement(By.id("P2_INCLUIR_CARTAO")).click();
	  	}catch (NoSuchElementException e) {
	  		System.out.println("------ P2_INCLUIR_CARTAO - cartão c/ numero externo errado ------\n");
//	  		ee.printStackTrace();
	  	}
	  	// 
	  	try {
			driver.findElement(By.id("P8_NRO_CARTAO")).clear();
	  	}catch (Exception e) {
	  		System.out.println("------ P8_NRO_CARTAO - erro inesperado ------\n");
	  		try {
	  			excluirCartao();
	  		}catch (NoSuchElementException eic) {
		  		System.out.println("------ P8_NRO_CARTAO - talvez remocao do cartao anterior nao foi concluida ------\n");
		  	}
	  	}
		driver.findElement(By.id("P8_NRO_CARTAO")).sendKeys(cartao.getNumeroExterno());
		driver.findElement(By.id("P8_NRO_CARTAO_CONFIRMAR")).clear();
		driver.findElement(By.id("P8_NRO_CARTAO_CONFIRMAR")).sendKeys(cartao.getNumeroExterno());
		driver.findElement(By.id("P8_DESCRICAO")).clear();
		driver.findElement(By.id("P8_DESCRICAO")).sendKeys(cartao.getNumeroExterno());
		driver.findElement(By.id("P8_INCLUIR_CARTAO")).click();
		
		try {
			// talvez o delay tenha que ser colocado aqui, 
			// vamos aguardar o proximo erro
			driver.findElement(By.id("P19_CONFIRMAR")).click();			
			System.out.println(
				((nConsultas<10) ? "0"+nConsultas : nConsultas) + " OK "
				+ "NOME: " + cartao.getNome() + ", CARTÃO: " + cartao.getNumeroExterno()
			);
			
			int cartoesJaCadastrados = 3; // Maximo de cartoes que ja podem estar cadastrados, evitar loop infinito
			// Pega o saldo, se o cartao no topo da lista tiver mesmo num externo
			do {
				try {
					driver.findElement(By.cssSelector("p.item-lista")).click();
				}catch (Exception ei) {
					System.out.println("Lista de itens vazia. ");	
				}
				
				numeroExternoDoSite = driver.findElement(By.id("P10_DESCRICAO_CARTAO_DISPLAY")).getText();
				if (cartao.getNumeroExterno().equals(numeroExternoDoSite))	{	
					System.out.println("numeroExternoDoSite IGUAL: " + numeroExternoDoSite);	
					obterInfoCartao(cartao);
				}
				else {
					System.out.println("numeroExternoDoSite DIFERENTE " + numeroExternoDoSite);
					excluirCartao();
				}
				
				cartoesJaCadastrados--;
			} while(!cartao.getNumeroExterno().equals(numeroExternoDoSite) || cartoesJaCadastrados <= 0);
			
			excluirCartao();
			
		} catch (Exception e) {			
			System.out.println(
			"\n" + ((nConsultas<10) ? "0"+nConsultas : nConsultas) + " INVÁLIDO \n"
					+ "  Empresa:   " + cartao.getAdOrg().getName() + "\n"
					+ "  Cartao:    " + cartao.getNumeroExterno() + "\n"
					+ "  Matricula: " + cartao.getMatricula() + "\n"
					+ "  Nome:      " + cartao.getNome() + "\n"
			);			
//			driver.findElement(By.id("P0_VOLTAR_PAG")).click();			
		}
  }

  // OBTER INFORMACOES DE CARTAO
  public void obterInfoCartao(Vale cartao) {
		String ksaldo = driver.findElement(By.id("P10_SALDO_DISPONIVEL_DISPLAY")).getText();
		String kdata = driver.findElement(By.id("P10_DATA_DISPLAY")).getText();
		String dia, mes, ano = "";

		cartao.setSaldo(ksaldo.substring(2, ksaldo.length()));
		System.out.print("     SALDO: " + cartao.getSaldo() + " ");
				
		kdata = kdata.substring( kdata.indexOf('/')-2, kdata.length() );
		dia = kdata.substring( kdata.indexOf('/')-2, kdata.indexOf('/') );
		mes = kdata.substring( kdata.indexOf('/')+1, kdata.indexOf('/', kdata.indexOf('/')+1) );
		ano = kdata.substring( kdata.indexOf('/', kdata.indexOf('/')+1)+1, kdata.indexOf('/', kdata.indexOf('/')+1)+5 );
		System.out.print(", DATA: " + dia + "/" + mes + "/" + ano + " ");
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//	formatter = formatter.withLocale( putAppropriateLocaleHere );  // Locale specifies human language for translating, and cultural norms for lowercase/uppercase and abbreviations and such. Example: Locale.US or Locale.CANADA_FRENCH
		LocalDate fdata = LocalDate.parse((ano+"-"+mes+"-"+dia), formatter);
		
		if (cartaoDao.updateSaldoData(String.valueOf(cartao.getSaldo()), fdata, 
		cartao.getNumeroExterno(), cartao.getAdOrgId()))
			System.out.println("Salvou!");
		else
			System.out.println("Não salvou!");
  }

  // EXCLUIR CARTAO
  public void excluirCartao() {
		
		driver.findElement(By.id("P10_REMOVER_CARTAO")).click();
		driver.findElement(By.id("P15_CONFIRMAR")).click();
  }

  @AfterTest
  public void afterTest() {
//  	fecharFirefox();
  }

}
