package model.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Recharge
{
	private Long id;
	private BigDecimal orderNumber;
	private BigDecimal value;
	private Timestamp orderDate;
	private String orderCategory;
	private Timestamp rechargeDate;
	private String carLineWay;
	private Long valeId;
	private Vale vale;
	private Timestamp updated;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public BigDecimal getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(BigDecimal orderNumber) {
		this.orderNumber = orderNumber;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public Timestamp getOrderDate() {
		return orderDate;
	}
	public void setOrderDate(Timestamp orderDate) {
		this.orderDate = orderDate;
	}
	public String getOrderCategory() {
		return orderCategory;
	}
	public void setOrderCategory(String orderCategory) {
		this.orderCategory = orderCategory;
	}
	public Timestamp getRechargeDate() {
		return rechargeDate;
	}
	public void setRechargeDate(Timestamp rechargeDate) {
		this.rechargeDate = rechargeDate;
	}
	public String getCarLineWay() {
		return carLineWay;
	}
	public void setCarLineWay(String carLineWay) {
		this.carLineWay = carLineWay;
	}
	public Long getValeId() {
		return valeId;
	}
	public void setValeId(Long valeId) {
		this.valeId = valeId;
	}
	public Vale getVale() {
		return vale;
	}
	public void setVale(Vale vale) {
		this.vale = vale;
	}
	public Timestamp getUpdated() {
		return updated;
	}
	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}
	
}
