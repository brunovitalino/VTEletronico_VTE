package model.entity;

import java.sql.Timestamp;
import java.time.LocalDate;

public class Vale
{
	private Long id;
//	private BigDecimal id;

	private Long adOrgId;
	private ADOrg adOrg;
	
	private String matricula;
	private String nome;
	private LocalDate dataNascimento;
	private String departamento;
	private String centroDeCusto;
	private String statusCartao;
	private LocalDate dataPreparacao;
	private String numeroPadrao;
	private String numeroExterno;
	private String categoriaCartao;
	private LocalDate dataCartaoAtivo;
	private LocalDate dataSaldo;
	private String saldo;
	private String tipoVale;
	private String valorVale;
	private String qtdVale;
	private String diasUteis;
	private String valorRecarga;
	private Timestamp updated;
	private Timestamp updatedTracking;
  
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getAdOrgId() {
		return adOrgId;
	}
	public void setAdOrgId(Long adOrgId) {
		this.adOrgId = adOrgId;
	}
	public ADOrg getAdOrg() {
		return adOrg;
	}
	public void setAdOrg(ADOrg adOrg) {
		this.adOrg = adOrg;
	}
	public String getMatricula() {
		return matricula;
	}
	public void setMatricula(String matricula) {
		this.matricula = matricula;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public LocalDate getDataNascimento() {
		return dataNascimento;
	}
	public void setDataNascimento(LocalDate dataNascimento) {
		this.dataNascimento = dataNascimento;
	}
	public String getDepartamento() {
		return departamento;
	}
	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}
	public String getCentroDeCusto() {
		return centroDeCusto;
	}
	public void setCentroDeCusto(String centroDeCusto) {
		this.centroDeCusto = centroDeCusto;
	}
	public String getStatusCartao() {
		return statusCartao;
	}
	public void setStatusCartao(String statusCartao) {
		this.statusCartao = statusCartao;
	}
	public LocalDate getDataPreparacao() {
		return dataPreparacao;
	}
	public void setDataPreparacao(LocalDate dataPreparacao) {
		this.dataPreparacao = dataPreparacao;
	}
	public String getNumeroPadrao() {
		return numeroPadrao;
	}
	public void setNumeroPadrao(String numeroPadrao) {
		this.numeroPadrao = numeroPadrao;
	}
	public String getNumeroExterno() {
		return numeroExterno;
	}
	public void setNumeroExterno(String numeroExterno) {
		this.numeroExterno = numeroExterno;
	}
	public String getCategoriaCartao() {
		return categoriaCartao;
	}
	public void setCategoriaCartao(String categoriaCartao) {
		this.categoriaCartao = categoriaCartao;
	}
	public LocalDate getDataCartaoAtivo() {
		return dataCartaoAtivo;
	}
	public void setDataCartaoAtivo(LocalDate dataCartaoAtivo) {
		this.dataCartaoAtivo = dataCartaoAtivo;
	}
	public LocalDate getDataSaldo() {
		return dataSaldo;
	}
	public void setDataSaldo(LocalDate dataSaldo) {
		this.dataSaldo = dataSaldo;
	}
	public String getSaldo() {
		return saldo;
	}
	public void setSaldo(String saldo) {
		this.saldo = saldo;
	}
	public String getTipoVale() {
		return tipoVale;
	}
	public void setTipoVale(String tipoVale) {
		this.tipoVale = tipoVale;
	}
	public String getValorVale() {
		return valorVale;
	}
	public void setValorVale(String valorVale) {
		this.valorVale = valorVale;
	}
	public String getQtdVale() {
		return qtdVale;
	}
	public void setQtdVale(String qtdVale) {
		this.qtdVale = qtdVale;
	}
	public String getDiasUteis() {
		return diasUteis;
	}
	public void setDiasUteis(String diasUteis) {
		this.diasUteis = diasUteis;
	}
	public String getValorRecarga() {
		return valorRecarga;
	}
	public void setValorRecarga(String valorRecarga) {
		this.valorRecarga = valorRecarga;
	}
	public Timestamp getUpdated() {
		return updated;
	}
	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}
	public Timestamp getUpdatedTracking() {
		return updatedTracking;
	}
	public void setUpdatedTracking(Timestamp updatedTracking) {
		this.updatedTracking = updatedTracking;
	}
	
}
