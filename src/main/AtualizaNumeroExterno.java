package main;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import model.dao.ValeDao;
import model.entity.ADOrg;
import model.entity.Vale;

public class AtualizaNumeroExterno {
	private String urlPgInicial;
	private WebDriver driver;
	private ValeDao cartaoDao;

	@BeforeTest
	public void beforeTest() {
		System.setProperty("webdriver.gecko.driver", "/home/vitalino/bin/geckodriver");
		driver = new FirefoxDriver();
		urlPgInicial = "https://www.vtefortaleza.com.br/site/forms/login/lateral.aspx";
	}
  
	@Test(priority=1)
	public void abrirPaginaInicial() {  	
		driver.get(urlPgInicial);
	}
  
	@Test(priority=2)
	public void updateAllVTNumeroExterno() {
		cartaoDao = new ValeDao();
		int x = 2;
		List<ADOrg> adOrgs = cartaoDao.findAllADOrg();
		cartaoDao.close();
		
		String cnpj = "";
	     
		for (ADOrg adOrg : adOrgs) {
			driver.findElement(By.id("TXT_CGC")).clear();
			
			cnpj = adOrg.getLbrCnpj();
			cnpj = cnpj.replaceAll("[^0-9]", "");
			System.out.println("CNPJ: " + cnpj + " - Obtendo número externo de funcionários...");
			driver.findElement(By.id("TXT_CGC")).sendKeys(cnpj);
			
			driver.findElement(By.id("TXT_Senha")).clear();
			driver.findElement(By.id("TXT_Senha")).sendKeys(adOrg.getPassword()); 
			
			driver.findElement(By.id("CMD_Gravar")).click();
	
			//buscar numero do cartao de todos os funcionarios dessa empresa
			updateAllVTNumeroExternoByCnpj(adOrg.getLbrCnpj());
			driver.close();
			driver = new FirefoxDriver();
			driver.get("urlPgInicial");
		  }
		cartaoDao.close();
	}
	
	public void updateAllVTNumeroExternoByCnpj(String cnpj) {
		cartaoDao = new ValeDao();
		List<Vale> cartoes = cartaoDao.findAllValeSemNumeroExternoByCnpj(cnpj);
		Long adOrgId;
		String matSimples;
		String matPesquisada = "00000000";
		String nExterno;
		
		for (Vale cartao : cartoes) {///todos os cartoes da empresa
			adOrgId		 =	null;
			matSimples   =  null;
			matPesquisada= "00000000";
			nExterno     =  null;
			
			adOrgId = cartao.getAdOrgId();
			matSimples = cartao.getMatricula();
			matPesquisada = matPesquisada.substring(0, matPesquisada.length()-matSimples.length()) + matSimples;	
			System.out.print("     matPesquisada: " + matPesquisada + " ");
			
			String url =  null;
			try {
				url = "https://www.vtefortaleza.com.br/Forms/Relatorios/RelatorioMovimentacaoUsuarioResultado.aspx?matricula=" + matPesquisada;
				driver.get(url);
								
				nExterno = driver.findElement(By.id("externo")).getText();			
				System.out.println("nExterno obtido: " + nExterno);
				
				cartaoDao.updateNumeroExternoByMatricula(nExterno, adOrgId, matSimples);
				
			}catch (Exception ee) {
				System.out.println("erro: " + url);

			}
			
			try { Thread.sleep(500); } catch (Exception es) { es.printStackTrace(); }
		}
		
		cartaoDao.close();
		
	}
  
	public int recarga() {
		// valor_vale, qtd_vale, dias_uteis, valor_recarga
		  
		return 1;
	}

	@AfterTest
	public void afterTest() {
//		driver.close();
	}

}
