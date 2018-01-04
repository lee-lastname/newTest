package com.isoftstone.pcis.policy.dm.bo;

import java.util.Date;

import com.isoftstone.pcis.policy.vo.AbstractBaseVO;

/**
 * EdrBase.
 *
 * @author MyEclipse Persistence Tools
 */
public class BaseVO implements java.io.Serializable {
	private static final long serialVersionUID = 4937436920234183750L;
	private String CAppNo;
	private String CPlyNo;
	private String CEdrNo;
	private String CReceiHs;// 是否回收发票
	private String CPkgNo;
	private String CDptCde;
	private String CProdNo;
	private Long NEdrPrjNo;
	private Long NBefEdrPrjNo;
	private String CAppTyp;
	private Double NAmtVar;
	private Double NCalcPrmVar;
	private Double NPrmVar;
	private Double NIndemLmtVar;
	private String CAppPrsnCde;
	private String CAppPrsnNme;
	private String CEdrCtnt;
	private Date TEdrAppTm;
	private Date TEdrBgnTm;
	private Date TEdrEndTm;
	private String CEdrMrk;
	private String CEdrType;
	private String CEdrRsnBundleCde;
	private Double NBefEdrAmt;
	private Double NBefEdrPrm;
	private Double NAmt;
	private Double NPrm;
	private String CerrRes;// 失败原因
	private String Flag;// 是否成功0:否1:是
	private Double NVat;// 增值税
	private Double NPrice;// 保费收入
	private Double NVatVar;// 增值税变化值
	private Double NPriceVar;// 保费收入变化值

	public String getCerrRes() {
		return CerrRes;
	}

	public void setCerrRes(String cerrRes) {
		CerrRes = cerrRes;
	}

	public String getFlag() {
		return Flag;
	}

	public void setFlag(String flag) {
		Flag = flag;
	}

	public Double getNVat() {
		return NVat;
	}

	public void setNVat(Double nVat) {
		NVat = nVat;
	}

	public Double getNPrice() {
		return NPrice;
	}

	public void setNPrice(Double nPrice) {
		NPrice = nPrice;
	}

	public Double getNVatVar() {
		return NVatVar;
	}

	public void setNVatVar(Double nVatVar) {
		NVatVar = nVatVar;
	}

	public Double getNPriceVar() {
		return NPriceVar;
	}

	public void setNPriceVar(Double nPriceVar) {
		NPriceVar = nPriceVar;
	}

	// 增加储金字段 20090518
	private Double NSavingAmt;
	private Double NBefEdrSaving;
	private Double NSavingVar;

	// add 最低核保机构和最低核保级别 yaos
	private String CMinUndrCls;
	private String CMinUndrDpt;
	// add 显示平台退保公式liuq
	private String CSurFormula;
	private String CSusBusiness;

	// add 报停展期 zhanghj
	private Date TRepStopExtBgnTm;
	private Date TRepStopExtEndTm;
	private String CRepStopExtRleAppNo;

	public String getCRepStopExtRleAppNo() {
		return CRepStopExtRleAppNo;
	}

	public void setCRepStopExtRleAppNo(String cRepStopExtRleAppNo) {
		CRepStopExtRleAppNo = cRepStopExtRleAppNo;
	}

	// 税款变化值 jthuang
	private Double NAggTaxVar;

	private AbstractBaseVO newPlyBase;
	private String CAppNme;

	public AbstractBaseVO getNewPlyBase() {
		return this.newPlyBase;
	}

	public void attachToNewPlyBaseUsingEdrBaseData(AbstractBaseVO newPlyBase) {
		this.newPlyBase = newPlyBase;
		this.newPlyBase.setNAmtVar(this.NAmtVar);
		this.newPlyBase.setNCalcPrmVar(this.NCalcPrmVar);
		this.newPlyBase.setNPrmVar(this.NPrmVar);
		this.newPlyBase.setNIndemLmtVar(this.NIndemLmtVar);
		this.newPlyBase.setCAppPrsnCde(this.CAppPrsnCde);
		this.newPlyBase.setCAppPrsnNme(this.CAppPrsnNme);
		this.newPlyBase.setCEdrCtnt(this.CEdrCtnt);
		this.newPlyBase.setTEdrAppTm(this.TEdrAppTm);
		this.newPlyBase.setTEdrBgnTm(this.TEdrBgnTm);
		this.newPlyBase.setTEdrEndTm(this.TEdrEndTm);
		this.newPlyBase.setCEdrMrk(this.CEdrMrk);
		this.newPlyBase.setCEdrType(this.CEdrType);
		this.newPlyBase.setCAppNo(this.CAppNo);
		this.newPlyBase.setCPlyNo(this.CPlyNo);
		this.newPlyBase.setCPkgNo(this.CPkgNo);
		this.newPlyBase.setCEdrNo(this.CEdrNo);
		this.newPlyBase.setNBefEdrAmt(this.NBefEdrAmt);
		this.newPlyBase.setNBefEdrPrm(this.NBefEdrPrm);
		this.newPlyBase.setNAmt(this.NAmt);
		this.newPlyBase.setNPrm(this.NPrm);
		this.newPlyBase.setCDptCde(this.CDptCde);
		this.newPlyBase.setCProdNo(this.CProdNo);
		this.newPlyBase.setCEdrRsnBundleCde(this.CEdrRsnBundleCde);
		this.newPlyBase.setNBefEdrPrjNo(this.NBefEdrPrjNo);
		this.newPlyBase.setNEdrPrjNo(this.NEdrPrjNo);
		this.newPlyBase.setCSusBusiness(this.CSusBusiness);
		this.newPlyBase.setCReceiHs(this.CReceiHs);

		// add 20090518
		this.newPlyBase.setNSavingAmt(NSavingAmt);
		this.newPlyBase.setNBefEdrSaving(NBefEdrSaving);
		this.newPlyBase.setNSavingVar(NSavingVar);

		// add 最低核保机构和最低核保级别 yaos
		this.newPlyBase.setCMinUndrCls(CMinUndrCls);
		this.newPlyBase.setCMinUndrCls(CMinUndrCls);

		// add yaos 退保计算公式
		this.newPlyBase.setCSurFormula(this.CSurFormula);

		// add 报停展期 zhanghj
		this.newPlyBase.setTRepStopExtBgnTm(this.TRepStopExtBgnTm);
		this.newPlyBase.setTRepStopExtEndTm(this.TRepStopExtEndTm);
	}

	public void attachToNewPlyBaseUsingPlyBaseData(AbstractBaseVO newPlyBase) {
		this.newPlyBase = newPlyBase;
		this.NAmtVar = this.newPlyBase.getNAmtVar();
		this.NCalcPrmVar = this.newPlyBase.getNCalcPrmVar();
		this.NPrmVar = this.newPlyBase.getNPrmVar();
		this.NIndemLmtVar = this.newPlyBase.getNIndemLmtVar();
		this.CAppPrsnCde = this.newPlyBase.getCAppPrsnCde();
		this.CAppPrsnNme = this.newPlyBase.getCAppPrsnNme();
		this.CEdrCtnt = this.newPlyBase.getCEdrCtnt();
		this.TEdrAppTm = this.newPlyBase.getTEdrAppTm();
		this.TEdrBgnTm = this.newPlyBase.getTEdrBgnTm();
		this.TEdrEndTm = this.newPlyBase.getTEdrEndTm();
		this.CEdrMrk = this.newPlyBase.getCEdrMrk();
		this.CEdrType = this.newPlyBase.getCEdrType();
		this.CAppNo = this.newPlyBase.getCAppNo();
		this.CPlyNo = this.newPlyBase.getCPlyNo();
		this.CPkgNo = this.newPlyBase.getCPkgNo();
		this.CEdrNo = this.newPlyBase.getCEdrNo();
		this.NBefEdrAmt = this.newPlyBase.getNBefEdrAmt();
		this.NBefEdrPrm = this.newPlyBase.getNBefEdrPrm();
		this.NAmt = this.newPlyBase.getNAmt();
		this.NPrm = this.newPlyBase.getNPrm();
		this.CDptCde = this.newPlyBase.getCDptCde();
		this.CProdNo = this.newPlyBase.getCProdNo();
		this.CEdrRsnBundleCde = this.newPlyBase.getCEdrRsnBundleCde();
		this.NBefEdrPrjNo = this.newPlyBase.getNBefEdrPrjNo();
		this.NEdrPrjNo = this.newPlyBase.getNEdrPrjNo();
		this.CAppTyp = this.newPlyBase.getCAppTyp();
		this.CReceiHs = this.newPlyBase.getCReceiHs();
		this.CSusBusiness = this.newPlyBase.getCSusBusiness();

		// add 20090518
		this.NSavingAmt = this.newPlyBase.getNSavingAmt();
		this.NBefEdrSaving = this.newPlyBase.getNBefEdrSaving();
		this.NSavingVar = this.newPlyBase.getNSavingVar();

		// add 最低核保机构和最低核保级别 yaos
		this.CMinUndrCls = this.newPlyBase.getCMinUndrCls();
		this.CMinUndrDpt = this.newPlyBase.getCMinUndrDpt();

		this.CSurFormula = this.newPlyBase.getCSurFormula();

		// add 报停展期 zhanghj
		this.TRepStopExtBgnTm = this.newPlyBase.getTRepStopExtBgnTm();
		this.TRepStopExtEndTm = this.newPlyBase.getTRepStopExtEndTm();
	}

	public void copyFieldsFromPlyBase(AbstractBaseVO plyBase) {
		this.CAppNo = plyBase.getCAppNo();
		this.CPlyNo = plyBase.getCPlyNo();
		this.CPkgNo = plyBase.getCPkgNo();
		this.CEdrNo = plyBase.getCEdrNo();
		this.CDptCde = plyBase.getCDptCde();
		this.CProdNo = plyBase.getCProdNo();
		this.NEdrPrjNo = plyBase.getNEdrPrjNo();
		this.NBefEdrPrjNo = plyBase.getNBefEdrPrjNo();
		this.NAmtVar = plyBase.getNAmtVar();
		this.NCalcPrmVar = plyBase.getNCalcPrmVar();
		this.NPrmVar = plyBase.getNPrmVar();
		this.NIndemLmtVar = plyBase.getNIndemLmtVar();
		this.CAppPrsnCde = plyBase.getCAppPrsnCde();
		this.CAppPrsnNme = plyBase.getCAppPrsnNme();
		this.CEdrCtnt = plyBase.getCEdrCtnt();
		this.TEdrAppTm = plyBase.getTEdrAppTm();
		this.TEdrBgnTm = plyBase.getTEdrBgnTm();
		this.TEdrEndTm = plyBase.getTEdrEndTm();
		this.CEdrMrk = plyBase.getCEdrMrk();
		this.CEdrType = plyBase.getCEdrType();
		this.CEdrRsnBundleCde = plyBase.getCEdrRsnBundleCde();
		this.NBefEdrAmt = plyBase.getNBefEdrAmt();
		this.NBefEdrPrm = plyBase.getNBefEdrPrm();
		this.NAmt = plyBase.getNAmt();
		this.NPrm = plyBase.getNPrm();
		this.CSusBusiness = plyBase.getCSusBusiness();
		this.CReceiHs = plyBase.getCReceiHs();

		// add 20090518
		this.NSavingAmt = plyBase.getNSavingAmt();
		this.NBefEdrSaving = plyBase.getNBefEdrSaving();
		this.NSavingVar = plyBase.getNSavingVar();

		// add 报停展期 zhanghj
		this.TRepStopExtBgnTm = plyBase.getTRepStopExtBgnTm();
		this.TRepStopExtEndTm = plyBase.getTRepStopExtEndTm();
		this.CRepStopExtRleAppNo = plyBase.getCRepStopExtRleAppNo();

	}

	public boolean isAttached() {
		return this.newPlyBase == null ? false : true;
	}

	public Double getNAmtVar() {
		return NAmtVar;
	}

	public void setNAmtVar(Double amtVar) {
		NAmtVar = amtVar;
		if (isAttached()) {
			this.newPlyBase.setNAmtVar(amtVar);
		}
	}

	public String getCAppNme() {
		return CAppNme;
	}

	public void setCAppNme(String appNme) {
		CAppNme = appNme;
	}

	public Double getNCalcPrmVar() {
		return NCalcPrmVar;
	}

	public void setNCalcPrmVar(Double calcPrmVar) {
		NCalcPrmVar = calcPrmVar;
		if (isAttached()) {
			this.newPlyBase.setNCalcPrmVar(calcPrmVar);
		}
	}

	public Double getNPrmVar() {
		return NPrmVar;

	}

	public void setNPrmVar(Double prmVar) {
		NPrmVar = prmVar;
		if (isAttached()) {
			this.newPlyBase.setNPrmVar(prmVar);
		}
	}

	public Double getNIndemLmtVar() {
		return NIndemLmtVar;
	}

	public void setNIndemLmtVar(Double indemLmtVar) {
		NIndemLmtVar = indemLmtVar;
		if (isAttached()) {
			this.newPlyBase.setNIndemLmtVar(indemLmtVar);
		}
	}

	public String getCAppPrsnCde() {
		return CAppPrsnCde;
	}

	public void setCAppPrsnCde(String appPrsnCde) {
		CAppPrsnCde = appPrsnCde;
		if (isAttached()) {
			this.newPlyBase.setCAppPrsnCde(appPrsnCde);
		}
	}

	public String getCReceiHs() {
		return CReceiHs;
	}

	public void setCReceiHs(String cReceiHs) {
		CReceiHs = cReceiHs;
	}

	public void setNewPlyBase(AbstractBaseVO newPlyBase) {
		this.newPlyBase = newPlyBase;
	}

	public String getCAppPrsnNme() {
		return CAppPrsnNme;
	}

	public void setCAppPrsnNme(String appPrsnNme) {
		CAppPrsnNme = appPrsnNme;
		if (isAttached()) {
			this.newPlyBase.setCAppPrsnNme(appPrsnNme);
		}
	}

	public String getCEdrCtnt() {
		return CEdrCtnt;
	}

	public void setCEdrCtnt(String edrCtnt) {
		CEdrCtnt = edrCtnt;
		if (isAttached()) {
			this.newPlyBase.setCEdrCtnt(edrCtnt);
		}
	}

	public Date getTEdrAppTm() {
		return TEdrAppTm;
	}

	public void setTEdrAppTm(Date edrAppTm) {
		TEdrAppTm = edrAppTm;
		if (isAttached()) {
			this.newPlyBase.setTEdrAppTm(edrAppTm);
		}
	}

	public Date getTEdrBgnTm() {
		return TEdrBgnTm;
	}

	public void setTEdrBgnTm(Date edrBgnTm) {
		TEdrBgnTm = edrBgnTm;
		if (isAttached()) {
			this.newPlyBase.setTEdrBgnTm(edrBgnTm);
		}
	}

	public Date getTEdrEndTm() {
		return TEdrEndTm;
	}

	public void setTEdrEndTm(Date edrEndTm) {
		TEdrEndTm = edrEndTm;
		if (isAttached()) {
			this.newPlyBase.setTEdrEndTm(edrEndTm);
		}
	}

	public String getCEdrMrk() {
		return CEdrMrk;
	}

	public void setCEdrMrk(String edrMrk) {
		CEdrMrk = edrMrk;
		if (isAttached()) {
			this.newPlyBase.setCEdrMrk(edrMrk);
		}
	}

	public String getCEdrType() {
		return CEdrType;
	}

	public void setCEdrType(String edrType) {
		CEdrType = edrType;
		if (isAttached()) {
			this.newPlyBase.setCEdrType(edrType);
		}
	}

	public String getCAppNo() {
		return CAppNo;
	}

	public void setCAppNo(String appNo) {
		CAppNo = appNo;
		if (isAttached()) {
			this.newPlyBase.setCAppNo(appNo);
		}
	}

	public String getCPlyNo() {
		return CPlyNo;
	}

	public void setCPlyNo(String plyNo) {
		CPlyNo = plyNo;
		if (isAttached()) {
			this.newPlyBase.setCPlyNo(plyNo);
		}
	}

	public String getCEdrNo() {
		return CEdrNo;
	}

	public void setCEdrNo(String edrNo) {
		CEdrNo = edrNo;
		if (isAttached()) {
			this.newPlyBase.setCEdrNo(edrNo);
		}
	}

	public Double getNBefEdrAmt() {
		return NBefEdrAmt;
	}

	public void setNBefEdrAmt(Double befEdrAmt) {
		NBefEdrAmt = befEdrAmt;
		if (isAttached()) {
			this.newPlyBase.setNBefEdrAmt(befEdrAmt);
		}
	}

	public Double getNBefEdrPrm() {
		return NBefEdrPrm;
	}

	public void setNBefEdrPrm(Double befEdrPrm) {
		NBefEdrPrm = befEdrPrm;
		if (isAttached()) {
			this.newPlyBase.setNBefEdrPrm(befEdrPrm);
		}
	}

	public Double getNAmt() {
		return NAmt;
	}

	public void setNAmt(Double amt) {
		NAmt = amt;
		if (isAttached()) {
			this.newPlyBase.setNAmt(amt);
		}
	}

	public Double getNPrm() {
		return NPrm;

	}

	public void setNPrm(Double prm) {
		NPrm = prm;
		if (isAttached()) {
			this.newPlyBase.setNPrm(prm);
		}
	}

	public String getCDptCde() {
		return CDptCde;
	}

	public void setCDptCde(String dptCde) {
		CDptCde = dptCde;
		if (isAttached()) {
			this.newPlyBase.setCDptCde(dptCde);
		}
	}

	public String getCProdNo() {
		return CProdNo;
	}

	public void setCProdNo(String prodNo) {
		CProdNo = prodNo;
		if (isAttached()) {
			this.newPlyBase.setCProdNo(prodNo);
		}
	}

	public String getCEdrRsnBundleCde() {
		return CEdrRsnBundleCde;
	}

	public void setCEdrRsnBundleCde(String edrRsnBundleCde) {
		CEdrRsnBundleCde = edrRsnBundleCde;
		if (isAttached()) {
			this.newPlyBase.setCEdrRsnBundleCde(edrRsnBundleCde);
		}
	}

	public Long getNBefEdrPrjNo() {
		return NBefEdrPrjNo;
	}

	public void setNBefEdrPrjNo(Long befEdrPrjNo) {
		NBefEdrPrjNo = befEdrPrjNo;
		if (isAttached()) {
			this.newPlyBase.setNBefEdrPrjNo(befEdrPrjNo);
		}
	}

	public Long getNEdrPrjNo() {
		return NEdrPrjNo;
	}

	public void setNEdrPrjNo(Long edrPrjNo) {
		NEdrPrjNo = edrPrjNo;
		if (isAttached()) {
			this.newPlyBase.setNEdrPrjNo(edrPrjNo);
		}
	}

	public String getCPkgNo() {
		return this.CPkgNo;
	}

	public void setCPkgNo(String pkgNo) {
		CPkgNo = pkgNo;
		if (isAttached()) {
			this.newPlyBase.setCPkgNo(pkgNo);
		}
	}

	public String getCAppTyp() {
		return CAppTyp;
	}

	public void setCAppTyp(String appTyp) {
		CAppTyp = appTyp;
	}

	public Double getNSavingAmt() {
		return NSavingAmt;
	}

	public void setNSavingAmt(Double savingAmt) {
		NSavingAmt = savingAmt;
		if (isAttached()) {
			this.newPlyBase.setNSavingAmt(savingAmt);
		}
	}

	public Double getNBefEdrSaving() {
		return NBefEdrSaving;
	}

	public void setNBefEdrSaving(Double befEdrSaving) {
		NBefEdrSaving = befEdrSaving;
		if (isAttached()) {
			this.newPlyBase.setNBefEdrSaving(befEdrSaving);
		}
	}

	public Double getNSavingVar() {
		return NSavingVar;
	}

	public void setNSavingVar(Double savingVar) {
		NSavingVar = savingVar;
		if (isAttached()) {
			this.newPlyBase.setNSavingVar(savingVar);
		}
	}

	public String getCMinUndrCls() {
		return CMinUndrCls;
	}

	public void setCMinUndrCls(String minUndrCls) {
		CMinUndrCls = minUndrCls;
	}

	public String getCMinUndrDpt() {
		return CMinUndrDpt;
	}

	public void setCMinUndrDpt(String minUndrDpt) {
		CMinUndrDpt = minUndrDpt;
	}

	public String getCSurFormula() {
		return CSurFormula;
	}

	public void setCSurFormula(String surFormula) {
		CSurFormula = surFormula;
	}

	public String getCSusBusiness() {
		return CSusBusiness;
	}

	public void setCSusBusiness(String susBusiness) {
		CSusBusiness = susBusiness;
	}

	public Date getTRepStopExtBgnTm() {
		return TRepStopExtBgnTm;
	}

	public Date getTRepStopExtEndTm() {
		return TRepStopExtEndTm;
	}

	public void setTRepStopExtBgnTm(Date repStopExtBgnTm) {
		TRepStopExtBgnTm = repStopExtBgnTm;
	}

	public void setTRepStopExtEndTm(Date repStopExtEndTm) {
		TRepStopExtEndTm = repStopExtEndTm;
	}

	public Double getNAggTaxVar() {
		return NAggTaxVar;
	}

	public void setNAggTaxVar(Double aggTaxVar) {
		NAggTaxVar = aggTaxVar;
	}

}
