package model.entity;

public class ADOrg {
	private Long adOrgId;
//	private BigDecimal id;
	private String lbrCnpj;
	private String name;
	private String password;
	private String matriculation;
	private Integer qtd;
	
	public Long getAdOrgId() {
		return adOrgId;
	}
	public void setAdOrgId(Long adOrgId) {
		this.adOrgId = adOrgId;
	}
	public String getLbrCnpj() {
		return lbrCnpj;
	}
	public void setLbrCnpj(String lbrCnpj) {
		this.lbrCnpj = lbrCnpj;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getMatriculation() {
		return matriculation;
	}
	public void setMatriculation(String matriculation) {
		this.matriculation = matriculation;
	}
	public Integer getQtd() {
		return qtd;
	}
	public void setQtd(Integer qtd) {
		this.qtd = qtd;
	}
}
