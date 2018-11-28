package model.entity;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Tracking
{
	private Long id;
	private Timestamp dateTime;
	private BigDecimal value;
	private String line;
	private String debitType;
	private Timestamp updated;
	private Long valeId;
	private Vale vale;
	private Recharge recharge;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Timestamp getDateTime() {
		return dateTime;
	}
	public void setDateTime(Timestamp dateTime) {
		this.dateTime = dateTime;
	}
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public String getDebitType() {
		return debitType;
	}
	public void setDebitType(String debitType) {
		this.debitType = debitType;
	}
	public Timestamp getUpdated() {
		return updated;
	}
	public void setUpdated(Timestamp updated) {
		this.updated = updated;
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
	public void setVale(Vale card) {
		this.vale = card;
	}
	public Recharge getRecharge() {
		return recharge;
	}
	public void setRecharge(Recharge recharge) {
		this.recharge = recharge;
	}
}
