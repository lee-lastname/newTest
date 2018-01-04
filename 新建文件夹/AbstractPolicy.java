package com.isoftstone.pcis.policy.dm.bo;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.isoftstone.fwk.dao.CommonDao;
import com.isoftstone.fwk.security.CurrentUser;
import com.isoftstone.fwk.service.BusinessServiceException;
import com.isoftstone.fwk.util.BeanTool;
import com.isoftstone.fwk.util.SpringUtils;
import com.isoftstone.pcis.policy.base.dm.IVoManipulator;
import com.isoftstone.pcis.policy.base.dm.voManipulatorImpl.PersistenceEvictor;
import com.isoftstone.pcis.policy.base.dm.voManipulatorImpl.VoBatchManipulator;
import com.isoftstone.pcis.policy.base.dm.voManipulatorImpl.VoMultiPropertiesSetter;
import com.isoftstone.pcis.policy.base.dm.voManipulatorImpl.VoOprFieldsCleaner;
import com.isoftstone.pcis.policy.base.dm.voManipulatorImpl.VoPropertiesCleaner;
import com.isoftstone.pcis.policy.base.dm.voManipulatorImpl.VoPropertiesCopier;
import com.isoftstone.pcis.policy.base.dm.voManipulatorImpl.VoPropertySetter;
import com.isoftstone.pcis.policy.common.constants.AppConstants;
import com.isoftstone.pcis.policy.common.constants.EndorseConstants;
import com.isoftstone.pcis.policy.common.constants.PolicyConstants;
import com.isoftstone.pcis.policy.common.helper.PolicyElementHelper;
import com.isoftstone.pcis.policy.core.exception.VoManipulateException;
import com.isoftstone.pcis.policy.core.helper.VoHelper;
import com.isoftstone.pcis.policy.hardcode.PolicyHardCodeConstants;
import com.isoftstone.pcis.policy.vo.AbstractApplicantVO;
import com.isoftstone.pcis.policy.vo.AbstractBaseVO;
import com.isoftstone.pcis.policy.vo.AbstractCvrgVO;
import com.isoftstone.pcis.policy.vo.AbstractInsuredVO;
import com.isoftstone.pcis.policy.vo.AbstractPayVO;
import com.isoftstone.pcis.policy.vo.AbstractPrmCoefVO;
import com.isoftstone.pcis.policy.vo.AbstractVhlVO;
import com.isoftstone.pcis.policy.vo.AbstractVsTaxVO;
import com.isoftstone.pcis.prod.exception.ProductDefinitionException;
import com.isoftstone.pcis.prod.helper.LongTermProductHelper;
import com.isoftstone.pcis.prod.service.IProdService;


/**
 * AbstractPolicy: 
 *
 * @author jiangqf
 */
public abstract class AbstractPolicy implements java.io.Serializable {
    private static final Logger logger = Logger.getLogger(AbstractPolicy.class);

    public static final String COMPONENT_NAME_BASE = "Base";
    public static final String COMPONENT_NAME_COVERAGE = "Cvrg";
    public static final String COMPONENT_NAME_PAYMENT = "Pay";
    public static final String COMPONENT_NAME_GRP_MEMBER = "GrpMember"; // 团单成员

    /**
     * 要素分类Map 结构为<Map<List<Map<要素名,值>> composition(Map of component)
     * component(List of entry) entry(VO) element(VO property)
     */
    private Map<String, List> composition = new HashMap<String, List>();

    /** toDelete */
    private transient Map<String, List> toDelete = new HashMap<String, List>();

    // //////////////////////////////////////////////

    /**
     * 获得组件Map
     *
     * @return
     */
    public Map<String, List> getComposition() {
        return this.composition;
    }

    /**
     * 设置组件Map
     *
     * @param composition
     */
    public void setComposition(Map<String, List> composition) {
        this.composition = composition;
    }

    /**
     * 获得组件列表
     *
     * @return
     */
    public Collection<List> getComponentCollection() {
        return this.composition.values();
    }

    /**
     * 获得组件名称集合
     *
     * @return
     */
    public Set<String> getComponentNameSet() {
        return this.composition.keySet();
    }

    /**
     * @param componentName
     * @return
     */
    public boolean containsComponent(String componentName) {
        return this.getComponentNameSet().contains(componentName);
    }

    /**
     * 获得组件(List<Vo>)
     *
     * @param componentName
     * @return
     */
    public List getComponent(String componentName) {
        return this.composition.get(componentName);
    }

    /**
     * 设置组件
     *
     * @param componentName
     * @param component
     */
    public void putComponent(String componentName, List component) {
        this.composition.put(componentName, component);
    }

    /**
     * 去除组件
     *
     * @param componentName
     */
    public void removeComponent(String componentName) {
        this.composition.remove(componentName);
    }

    /**
     * 获得单记录组件VO
     *
     * @param componentName
     * @return
     */
    public Object getMonoComponent(String componentName) {
        List dataList = this.composition.get(componentName);
        if (CollectionUtils.isNotEmpty(dataList)) {
            return dataList.get(0);
        } else {
            return null;
        }
    }

    /**
     * 设置单记录组件
     *
     * @param componentName
     * @param entry
     */
    public void putMonoComponent(String componentName, Object entry) {
        List component = new ArrayList();
        component.add(entry);
        this.composition.put(componentName, component);
    }

    public Object getPolyComponentEntry(String componentName, String entryKey)
            throws VoManipulateException, ProductDefinitionException {
        Map componentAsMap = this.getComponentAsMap(componentName);
        return componentAsMap.get(entryKey);
    }

    // ////////////////////////////////////////////

    /**
     * 对所有vo进行操作
     *
     * @param manipulator
     * @throws VoManipulateException
     */
    public void manipulateAllEntries(IVoManipulator manipulator) throws VoManipulateException {
        for (List component : this.getComponentCollection()) {
            for (Object entry : component) {

                manipulator.manipulate(entry);
            }
        }
    }

    /**
     * 对所有vo进行多个操作
     *
     * @param manipulator
     * @throws VoManipulateException
     */
    public void manipulateAllEntries(IVoManipulator... manipulators) throws VoManipulateException {
        VoBatchManipulator batch = new VoBatchManipulator();
        for (IVoManipulator manipulator : manipulators) {
            batch.addManipulator(manipulator);
        }

        for (List component : this.getComponentCollection()) {
            for (Object entry : component) {

                batch.manipulate(entry);
            }
        }
    }

    /**
     * 为所有VO设置属性
     *
     * @param name
     * @param value
     * @param ignoreException
     *            是否忽略异常
     */
    public void setPropertyForAllEntries(String name, Object value, boolean ignoreException)
            throws VoManipulateException {
        VoPropertySetter propSetter = new VoPropertySetter(name, value, ignoreException);
        this.manipulateAllEntries(propSetter);
    }

    /**
     * 为所有VO设置属性，不忽略异常
     *
     * @param name
     * @param value
     */
    public void setPropertyForAllEntries(String name, Object value) throws VoManipulateException {
        VoPropertySetter propSetter = new VoPropertySetter(name, value, false);
        this.manipulateAllEntries(propSetter);
    }

    /**
     * 为所有VO设置多个属性，属性用 名称-值 Map中获得
     *
     * @param nameValueMap
     * @param ignoreException
     * @throws VoManipulateException
     */
    public void setPropertiesForAllEntries(Map<String, Object> nameValueMap, boolean ignoreException)
            throws VoManipulateException {
        VoMultiPropertiesSetter multiPropSetter = new VoMultiPropertiesSetter(nameValueMap,
                ignoreException);
        this.manipulateAllEntries(multiPropSetter);
    }

    /**
     * 为所有VO设置多个属性,不忽略异常，属性用 名称-值 Map中获得
     *
     * @param nameValueMap
     * @throws VoManipulateException
     */
    public void setPropertiesForAllEntries(Map<String, Object> nameValueMap)
            throws VoManipulateException {
        VoMultiPropertiesSetter multiPropSetter = new VoMultiPropertiesSetter(nameValueMap, false);
        this.manipulateAllEntries(multiPropSetter);
    }

    /**
     * 为所有VO设置多个属性，这此属性从Object中拷贝
     *
     * @param template
     * @param ignoreException
     * @throws VoManipulateException
     */
    public void copyPropertiesForAllEntries(Object template, boolean ignoreException)
            throws VoManipulateException {
        VoPropertiesCopier propCopier = new VoPropertiesCopier(template, ignoreException);
        this.manipulateAllEntries(propCopier);
    }

    /**
     * 获得元素值
     *
     * @param elementComplexName
     *            形如 Base.CAppNo或Cvrg.030018.CCvrgNo
     * @return
     * @throws ProductDefinitionException
     */
    public Object getElementValue(String elementComplexName) throws VoManipulateException,
            ProductDefinitionException {
        String[] nameSects = elementComplexName.split("\\.");
        String componentName;
        String entryKey;
        String elementName;
        if (nameSects.length == 2) {
            componentName = nameSects[0];
            elementName = nameSects[1];
            Object entry = this.getMonoComponent(componentName);

            return this.getPropertyOfEntry(entry, elementName);
        } else if (nameSects.length == 3) {
            componentName = nameSects[0];
            entryKey = nameSects[1];
            elementName = nameSects[2];
            Object entry = this.getPolyComponentEntry(componentName, entryKey);

            return this.getPropertyOfEntry(entry, elementName);
        } else {
            return null;
        }
    }

    /**
     * 获得单记录组件的属性
     *
     * @param elementComplexName
     *            形如Base.CAppNo
     * @return
     */
    public Object getPropertyOfMonoComponent(String elementComplexName) {
        String[] nameScores = elementComplexName.split("\\.");
        if (nameScores.length != 2) {
            logger.info("无法获取(投)保单要素属性：非有效名称:" + elementComplexName);
            return null;
        }

        Object component = this.getMonoComponent(nameScores[0]);
        if (component == null) {
            return null;
        }
        try {
            return BeanTool.getAttributeValue(component, nameScores[1]);
        } catch (Exception e) {
            // 不作为异常（投保单界面上需要的某字段在保单表里不存在，如投保单状态）
            logger.info("无法获取(投)保单要素属性:" + elementComplexName);
            return null;
        }
    }

    /**
     * 获得单记录组件的属性并转换为String
     *
     * @param elementComplexName
     * @return
     */
    public String getPropertyOfMonoComponentAsString(String elementComplexName) {
        String[] nameScores = elementComplexName.split("\\.");

        Object component = this.getMonoComponent(nameScores[0]);
        if (component == null) {
            logger.info("无法获取(投)保单要素属性：非有效名称:" + elementComplexName);

            return null;
        }
        try {
            return BeanTool.getAttributeValueAsString(component, nameScores[1]);
        } catch (Exception e) {
            // 不作为异常（投保单界面上需要的某字段在保单表里不存在，如投保单状态）
            logger.info("无法获取(投)保单要素属性:" + elementComplexName);

            return null;
        }
    }

    /**
     * 为单记录组件设置属性值
     *
     * @param elementComplexName
     *            为 componentName.elementName格式
     * @param valueStr
     */
    public void setPropertyForMonoComponentByString(String elementComplexName, String valueStr) {
        String[] nameScores = elementComplexName.split("\\.");

        Object entry = this.getMonoComponent(nameScores[0]);
        if (entry == null) {
            return;
        }
        try {
            BeanTool.setAttributeByString(entry, nameScores[1], valueStr);
        } catch (Exception e) {
            logger.info("无法设置(投)保单要素属性：" + elementComplexName);
        }
    }

    /**
     * 为单记录组件设置属性值
     *
     * @param complexName
     *            为 componentName.elementName格式
     * @param valueStr
     */
    public void setPropertyForMonoComponent(String elementComplexName, Object value) {
        String[] nameScores = elementComplexName.split("\\.");

        Object entry = this.getMonoComponent(nameScores[0]);
        if (entry == null) {
            return;
        }
        try {
            BeanTool.setAttributeValue(entry, nameScores[1], value);
        } catch (Exception e) {
            logger.info("无法设置(投)保单要素属性：" + elementComplexName);
        }
    }

    /**
     * @param entry
     * @param propName
     * @return
     */
    public Object getPropertyOfEntry(Object entry, String propName) {
        if (entry == null) {
            return null;
        }
        try {
            return BeanTool.getAttributeValue(entry, propName);
        } catch (Exception e) {
            logger.info("无法获取(投)保单要素属性：" + entry.getClass().getName() + "属性" + propName);
            return null;
        }
    }

    /**
     * @param entry
     * @param propName
     * @param value
     * @return
     */
    public void setPropertyOfEntry(Object entry, String propName, String value) {
        if (entry == null) {
            return;
        }
        try {
            BeanTool.setAttributeValue(entry, propName, value);
        } catch (Exception e) {
            logger.info("无法设置(投)保单要素属性：" + entry.getClass().getName() + "属性" + propName);
        }
    }

    /**
     * @param entry
     * @param propName
     * @param value
     * @return
     */
    public void setPropertyOfObjectValue(Object entry, String propName, Object value) {
        if (entry == null) {
            return;
        }
        try {
            BeanTool.setAttributeValue(entry, propName, value);
        } catch (Exception e) {
            logger.info("无法设置(投)保单要素属性：" + entry.getClass().getName() + "属性" + propName);
        }
    }

    public Map<String, ? extends Object> getComponentAsRowIdMap(String componentName)
            throws VoManipulateException {
        Map<String, Object> rowidEntryMap = new HashMap<String, Object>();

        for (Object entry : this.getComponent(componentName)) {
            if (VoHelper.hasPkId(entry)) {
                String rowId = VoHelper.getOrigPkId(entry);
                if (StringUtils.isNotEmpty(rowId)) {
                    rowidEntryMap.put(rowId, entry);
                }
            }
        }

        return rowidEntryMap;
    }

    /**
     * 将component的List数据转为Map取出，基中key由产品设置定义
     *
     * @param componentName
     * @return
     * @throws ProductDefinitionException
     * @throws com.isoftstone.pcis.policy.core.exception.VoManipulateException 
     */
    public Map<String, ? extends Object> getComponentAsMap(String componentName)
            throws VoManipulateException, ProductDefinitionException, com.isoftstone.pcis.policy.core.exception.VoManipulateException {
        Map<String, Object> entryMap = new HashMap<String, Object>();

        if (componentName.equals(COMPONENT_NAME_COVERAGE)) {
            // 险别特殊处理
            List<AbstractCvrgVO> cvrgEntryList = this.getCoverages();
            if (cvrgEntryList != null && !cvrgEntryList.isEmpty()) {
                for (AbstractCvrgVO cvrgEntry : cvrgEntryList) {
                    List<String> keyNameList = VoHelper.getCoverageKeyNameList(this.getProdNo(),
                            cvrgEntry.getCCvrgNo());
                    String keyValueStr = VoHelper.getKeyValueAsStr(cvrgEntry, keyNameList);
                    entryMap.put(keyValueStr, cvrgEntry);
                }
            }
        } else {
            List<String> keyNameList = VoHelper.getKeyNameList(this.getProdNo(), componentName);
            List componentList = this.getComponent(componentName);
            if (componentList != null) {
                for (Object entry : componentList) {
                    String keyValueStr = VoHelper.getKeyValueAsStr(entry, keyNameList);
                    entryMap.put(keyValueStr, entry);
                }
            }
        }
        return entryMap;
    }

    // ///////////////////////////////
    // 特定方法 - 操作具体组件或属性 //
    // ///////////////////////////////

    // 固定的要素分类提供访问方法
    public AbstractBaseVO getBase() {
        return (AbstractBaseVO) this.getMonoComponent("Base");
    }

    public void setBase(AbstractBaseVO base) {
        this.putMonoComponent("Base", base);
    }

    public List<AbstractCvrgVO> getCoverages() {
        return this.getComponent("Cvrg");
    }

    public AbstractApplicantVO getApplicant() {
        Object applicantObj = this.getMonoComponent("Applicant");
        if (applicantObj == null) {
            return null;
        } else {
            return (AbstractApplicantVO) applicantObj;
        }
    }

    public List<AbstractInsuredVO> getInsureds() {
        return this.getComponent("Insured");
    }

    public AbstractPrmCoefVO getPremiumCoef() {
        return (AbstractPrmCoefVO) this.getMonoComponent("PrmCoef");
    }

    //非公共的要素分类，提供快捷的获取方法
    public AbstractVhlVO getVhl() {
        Object vhlObj = this.getMonoComponent("Vhl");
        if (vhlObj == null) {
            return null;
        } else {
            return (AbstractVhlVO) vhlObj;
        }
    }

    /**
     * 清空创建/修改人/时间
     */
    public void clearOprFields() throws VoManipulateException {
        VoOprFieldsCleaner oprFieldsCleaner = new VoOprFieldsCleaner();
        this.manipulateAllEntries(oprFieldsCleaner);
    }

    /**
     * 清空创建/修改人/时间
     */
    public void clearOprFieldsIgnoreException() {
        VoOprFieldsCleaner oprFieldsCleaner = new VoOprFieldsCleaner();
        try {
            this.manipulateAllEntries(oprFieldsCleaner);
        } catch (VoManipulateException e) {
            logger.warn("清除创建/修改 人/时间 异常被忽略");
        }
    }

    // ////////////////////////////////////////
    // 组件属性
    // ///////////////////////////////////////
    public String getProdNo() {
        return this.getBase().getCProdNo();
    }

    public String getAppNo() {
        // return this.getBase().getCAppNo();
        if (this.getBase() != null) {
            return this.getBase().getCAppNo();
        } else {
            Map<String, List> map = this.getComposition();
            if (map == null) {
                return "";
            }
            for (Map.Entry<String, List> entry : map.entrySet()) {
                List list = entry.getValue();
                for (int i = 0; list != null && i < list.size(); i++) {
                    Object obj = list.get(i);
                    String value = "";
                    try {
                        value = (String) PropertyUtils.getProperty(obj, "CAppNo");
                        if (value != null && !"".equals(value)) {
                            return value;
                        }
                    } catch (Exception e) {
                    }
                }
            }
            return "";
        }
    }

    /**
     * @deprecated Use {@link #setAppNoForAllEntries(String)} instead
     */
    @Deprecated
    public void setAppNo(String appNo) {
        this.setAppNoForAllEntries(appNo);
    }

    public void setAppNoForAllEntries(String appNo) {
        try {
            this.setPropertyForAllEntries("CAppNo", appNo, true);
        } catch (VoManipulateException e) {
            logger.error("设置保单VO的appNo时出错");
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void setPlyNo(String plyNo) {
        try {
            this.setPropertyForAllEntries("CPlyNo", plyNo, false);
        } catch (VoManipulateException e) {
            logger.error("一般不会发生：设置保单VO的plyNo时出错");
            e.printStackTrace();
        }
    }

    // /////////////
    // 业务方法 //
    // ////////////

    /**
     * 判断是否套餐产品
     *
     * @return
     */
    public boolean isPkg() {
        if (this.getBase().getCPkgMrk() == null
                || this.getBase().getCPkgMrk().equals(PolicyConstants.PKG_TYPE_NON) // 非套餐
                || this.getBase().getCPkgMrk().equals(PolicyConstants.PKG_TYPE_FLEET) // 车队投保
        ) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 取险别数据到Map
     *
     * @return Map<CvrgNo,AbstractCvrgVO对象>
     */
    public Map<String, AbstractCvrgVO> getCoverageMap() {
        Map<String, AbstractCvrgVO> coverageMap = new HashMap();
        if (this.getCoverages() != null) {
            for (AbstractCvrgVO c : this.getCoverages()) {
                coverageMap.put(c.getCCvrgNo() + "-" + c.getNSeqNo(), c);
            }
        } else {
            return null;
        }

        return coverageMap;
    }

    /**
     * 组装险别费率因子 参数： 1. 险别费率因子名称列表（险别费率因子命名：Cvrg.CIndemLmtLvl.030018）
     *
     * @param cvrgComplexNameList
     * @return Map<String, Object>
     */
    public Map<String, Object> getCvrgPrmFactorMap(List<String[]> cvrgComplexNameList) {
        Map<String, Object> prmFactorMap = new HashMap<String, Object>();

        List<AbstractCvrgVO> cvrgList = this.getCoverages();
        for (AbstractCvrgVO vo : cvrgList) {
            for (String[] sNameArray : cvrgComplexNameList) {
                if (sNameArray[2].equals(vo.getCCvrgNo())) {
                    String propertyName = sNameArray[0] + "." + sNameArray[1];
                    try {
                        Object value = PropertyUtils.getProperty(vo, sNameArray[1]);
                        prmFactorMap.put(propertyName, value);
                    } catch (Exception e) {
                        System.out.println("AbstractPolicy -> getCvrgPrmFactorMap 获取属性"
                                + propertyName + "失败");
                    }
                }
            }
        }
        return prmFactorMap;
    }

    /**
     * 取费率因子Map 费率因子： 1. 普通费率因子：对于FREE_EDIT的单记录DW 2. 险别费率因子：针对险别的GRID_EDIT多记录DW
     * 因子命名可能性： 1. Vhl.CVhlTyp : 车辆类型（普通费率因子） 2. Cvrg.CIndemLmtLvl.030018 :
     * 【第三者责任险】限额档次（险别费率因子） (030018)(CIndemLmtLvl)
     *
     * @return Map<要素明细名，值> 基中要素明细名如 Base.CAppNo
     */
    public Map getPrmFactorMap() {
        return this.getPrmFactorMap(null);
    }

    /**
     * 根据险别编码获取指定险别的费率因子信息
     *
     * @param CCvrgNo
     *            险别编码
     * @return Map
     */
    public Map getPrmFactorMap(String CCvrgNo) {
        Map<String, Object> prmFactorMap = new HashMap<String, Object>();

        // 从产品定义取标志为费率因子的要素明细
        IProdService prodService = (IProdService) SpringUtils.getSpringBean("prodService");
        String prodNo = this.getBase().getCProdNo();
        List<String> elemComplexNameList = new ArrayList<String>();

        try {
            elemComplexNameList = prodService.getProdFactorList(prodNo);
            // elemComplexNameList.remove("Cvrg.CIndemLmtLvl");
            // elemComplexNameList.add("Cvrg.CIndemLmtLvl.030018");
            // elemComplexNameList.add("Cvrg.CIndemLmtLvl.030006");
        } catch (BusinessServiceException e) {
            e.printStackTrace();
            return null;
        }

        // 取要素名细的值,并存入Map
        List<String[]> cvrgComplexNameList = new ArrayList<String[]>();
        for (String elemComplexName : elemComplexNameList) {
            String[] sNameArray = elemComplexName.split("\\.");
            if (sNameArray.length == 2) {// 普通费率因子(单记录)
                prmFactorMap.put(elemComplexName, this.getPropertyOfMonoComponent(elemComplexName));
            } else if (sNameArray.length == 3) {// 险别费率因子（多记录）
                if (CCvrgNo == null) {
                    cvrgComplexNameList.add(sNameArray);
                } else if (CCvrgNo != null && !"".equals(CCvrgNo) && sNameArray[2].equals(CCvrgNo)) {// 只保留指定险别的费率因子信息
                    cvrgComplexNameList.add(sNameArray);
                }
            }
        }
        if (cvrgComplexNameList != null && !cvrgComplexNameList.isEmpty()) {
            prmFactorMap.putAll(this.getCvrgPrmFactorMap(cvrgComplexNameList));
        }

        // 测试数据
        // prmFactorMap.put("Cvrg.NIndemLmtLvl.030018", "345012001");
        // prmFactorMap.put("Cvrg.NIndemLmtLvl.030006", "345012001");
        return prmFactorMap;
    }

    /**
     * 取保费系数Map
     *
     * @return Map<要素明细名，值> 基中要素明细名如 Base.CAppNo
     */
    public Map getPrmCoefMap() {
        Map<String, Object> prmCoefMap = new HashMap<String, Object>();

        // 从产品定义取标志为费率因子的要素明细
        IProdService prodService = (IProdService) SpringUtils.getSpringBean("prodService");
        String prodNo = this.getBase().getCProdNo();
        List<String> elemComplexNameList = new ArrayList<String>();
        try {
            elemComplexNameList = prodService.getProdCoefList(prodNo);
        } catch (BusinessServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        // 取要素名细的值,并存入Map
        for (String elemComplexName : elemComplexNameList) {
            prmCoefMap.put(elemComplexName, this.getPropertyOfMonoComponent(elemComplexName));
        }

        return prmCoefMap;
    }

    /**
     * 合计险别的保额和保费
     */
    public void sumAmtAndPrem() {
        BigDecimal sumAmount = new BigDecimal("0.0");
        BigDecimal sumPremium = new BigDecimal("0.0");
        List<AbstractCvrgVO> coverageList = this.getComponent("Cvrg");
        String prodNo = this.getProdNo();
//        if (prodNo.equalsIgnoreCase(PolicyHardCodeConstants.PROD_033003) 
//        		|| prodNo.equalsIgnoreCase(PolicyHardCodeConstants.PROD_033002)
//                || prodNo.equalsIgnoreCase(PolicyHardCodeConstants.PROD_033001)) {
        	//YAIC 商业险保额合计为所有险别合计的和
//            for (AbstractCvrgVO coverage : coverageList) {
//                if (coverage.getNAmt() != null) {
//                    if (coverage.getCCvrgNo().equalsIgnoreCase("030001")
//                            || coverage.getCCvrgNo().equalsIgnoreCase("030006")
//                            || coverage.getCCvrgNo().equalsIgnoreCase("030009")
//                            || coverage.getCCvrgNo().equalsIgnoreCase("030018")) {
//                        sumAmount = sumAmount.add(new BigDecimal(
//                                Double.toString(coverage.getNAmt())));
//                    }
//                }
//                if (coverage.getNPrm() != null) {
//                    sumPremium = sumPremium.add(new BigDecimal(Double.toString(coverage.getNPrm())));
//                }
//            }
//        } else {
            for (AbstractCvrgVO coverage : coverageList) {
                if (coverage.getNAmt() != null) {
                    sumAmount = sumAmount.add(new BigDecimal(Double.toString(coverage.getNAmt())));
                }
                if (coverage.getNPrm() != null) {
                    sumPremium = sumPremium.add(new BigDecimal(Double.toString(coverage.getNPrm())));
                }
            }
//        }
        this.getBase().setNAmt(sumAmount.doubleValue());
        this.getBase().setNPrm(sumPremium.doubleValue());

        //YAIC 交强险只有一个险别
//        if ( this.getBase().getCProdNo().equals(PolicyHardCodeConstants.PROD_030001)
//                || this.getBase().getCProdNo().equals(PolicyHardCodeConstants.PROD_030003)
//                || this.getBase().getCProdNo().equals(PolicyHardCodeConstants.PROD_030005)) {
//            this.getBase().setNAmt(new Double("122000.00"));
//        }
    }
    
    
    /**
     * 合计险别的保额
     */
    public void sumAmt() {
        BigDecimal sumAmount = new BigDecimal("0.0");
        List<AbstractCvrgVO> coverageList = this.getComponent("Cvrg");
            for (AbstractCvrgVO coverage : coverageList) {
                if (coverage.getNAmt() != null) {
                    sumAmount = sumAmount.add(new BigDecimal(Double.toString(coverage.getNAmt())));
                }
            }
//        }
        this.getBase().setNAmt(sumAmount.doubleValue());
    }

    /**
     * 判断保费计算结果是否一致
     *
     * @param other
     *            要比较的投保单
     * @return
     */
    public boolean isSamePremCalcResult(AbstractPolicy other) {
        Map<String, AbstractCvrgVO> thisMap = this.getCoverageMap();
        Map<String, AbstractCvrgVO> otherMap = other.getCoverageMap();
        if (!thisMap.keySet().equals(otherMap.keySet())) {
            // TODO
            return false;
        }
        for (String key : thisMap.keySet()) {
            if (!thisMap.get(key).isSamePremCalcResult(otherMap.get(key))) {
                // TODO
                return false;
            }
        }

        return true;
    }

    // //////////////////////////////////////////////
    // 保单拷贝工具
    // //////////////////////////////////////////////
    /**
     * 复制（投）保单对象
     *
     * @param src
     *            源保单对象
     * @param voType
     *            指出是复制保单还是投保单
     * @return
     */
    public AbstractPolicy clone(String voType) {
        AbstractPolicy dst;
        if (voType.equals(VoHelper.VO_TYPE_APP)) {
            dst = new PolicyApplication();
        } else {
            dst = new Policy();
        }

        try {
            String VoType = voType;
            for (String comName : this.getComposition().keySet()) {
                if (comName.equals("GrpMember") || comName.equals("GrpCvrg")) {
                    dst.putComponent(comName, this.getComponent(comName));
                } else {
                    List dstComponent = new ArrayList();
                    List srcComponent = this.getComponent(comName);
                    if(srcComponent != null){
                    	Class clazz = VoHelper.getVoClass(comName, VoType);
                        for (Object srcEntry : srcComponent) {
                            Object dstEntry = clazz.newInstance();
                            PropertyUtils.copyProperties(dstEntry, srcEntry);
                            dstComponent.add(dstEntry);
                        }
                    }

                    dst.putComponent(comName, dstComponent);
                }
            }

            return dst;
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public AbstractPolicy clone(String voType, boolean flag) {
        AbstractPolicy dst;
        if (voType.equals(VoHelper.VO_TYPE_APP)) {
            dst = new PolicyApplication();
        } else {
            dst = new Policy();
        }

        try {
            for (String comName : this.getComposition().keySet()) {
                List dstComponent = new ArrayList();
                List srcComponent = this.getComponent(comName);
                Class clazz = VoHelper.getVoClass(comName, voType);
                for (Object srcEntry : srcComponent) {
                    Object dstEntry = clazz.newInstance();
                    PropertyUtils.copyProperties(dstEntry, srcEntry);
                    if (flag) {
                        try {
                            PropertyUtils.setProperty(dstEntry, "CPkId", null);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block

                        }
                    }
                    dstComponent.add(dstEntry);
                }

                dst.putComponent(comName, dstComponent);
            }

            return dst;
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 清空一些数据用于相同产品保单拷贝
     */
    public void cleanForSameProdCopy() throws VoManipulateException {
        /*
         * 判断如下:
         * 1. 同产品复制
         *    1. 除了共保信息不复制，其他都复制
         *    2. 缴费计划不复制[20090709]
         * 2. 同大类复制
         *    1. 复制基本信息
         *	 2. 复制投保人信息
         *	 3. 复制被保人信息
         *	 4. 如果复制与被复制的产品都为车险，则需要复制车辆信息与驾驶员信息
         *	 5. 缴费计划不复制[20090709]
         * 3. 不同大类复制
         *    1. 复制基本信息
         *	 2. 复制投保人信息
         *	 3. 复制被保人信息
         *	 4. 缴费计划不复制[20090709]
         *
         * 业务咨询: 田军
         */

        // 移除不必要的组件
        this.removeComponent("Ci"); // 共保信息
        this.removeComponent("Pay"); //缴费计划
        this.removeComponent("VsTax");//车船税
        this.removeComponent("PrmCoef");//优惠系数

        // 所有vo都要进行的处理
        PersistenceEvictor persistEvictor = new PersistenceEvictor(
                (CommonDao) SpringUtils.getSpringBean("commonDao"));
        String[] props2Clean = new String[] { "CPkId", "CRowId", "CAppNo", "CEdrNo" };
        VoPropertiesCleaner propsCleaner = new VoPropertiesCleaner(props2Clean, true);
        VoPropertySetter edrPrjNoSetter = new VoPropertySetter("NEdrPrjNo", new Long(0), true);
        VoOprFieldsCleaner oprFieldsCleaner = new VoOprFieldsCleaner();
        VoBatchManipulator batchModifier = new VoBatchManipulator();
        batchModifier.addManipulator(persistEvictor);
        batchModifier.addManipulator(propsCleaner);
        batchModifier.addManipulator(edrPrjNoSetter);
        batchModifier.addManipulator(oprFieldsCleaner);

        this.manipulateAllEntries(batchModifier);

        // 个别vo特殊处理
        if (this.containsComponent("Base")) {
            AbstractBaseVO base = this.getBase();
            base.setCPlyNo(null);
            base.setTIssueTm(null);
            base.setCOrigPlyNo(null);
            base.setCRelPlyNo(null);
            base.setCAppPrsnCde(null);
            base.setCAppPrsnNme(null);
            base.setCResvTxt1(null);
            base.setNPrm(null);
            base.setNAmt(null);

            //清空批改信息
            base.setNAmtVar(null);
            base.setNPrmVar(null);
            base.setNCalcPrmVar(null);
            base.setNIndemLmtVar(null);
            base.setCSaveMrk("0");
            base.setNSavingVar(null);
            base.setCEdrCtnt(null);
            base.setTEdrAppTm(null);
            base.setTEdrBgnTm(null);
            base.setTEdrEndTm(null);
            base.setCEdrType(null);   // add  by  baidong  2011 07 20
            base.setTNextEdrBgnTm(EndorseConstants.MAX_TIME);
            base.setTNextEdrEndTm(EndorseConstants.MAX_TIME);
            base.setTNextEdrUdrTm(null);

            //20090706 yjhuang@isoftstone.com 设置操作员与操作时间字段的值
            base.setCOprCde(CurrentUser.getUser().getOpRelCde());
            base.setTOprTm(new Date());
            
            //清空特别约定  2011-3-9
//            base.setCUnfixSpc(null);
            //清除保费相关 
            base.setNBasePrm(null);
            base.setNAllPrm(null);
            base.setNIrrRatio(null);//浮动率
            base.setCSlsId(null);//业务员工号
            base.setCSlsCde(null);//业务员
            base.setCSlsTel(null);//业务员电话
            base.setCBrkrCde(null);//Base.CBrkrCde清空 代理人 此时不能判断代理人以及其代理协议是否有效。
            base.setCAgtAgrNo(null);
            base.setCPrnNo(null);
            
        }

        if (this.containsComponent("Cvrg")) {
            List<AbstractCvrgVO> cvrgList = this.getCoverages();

            // 对理财险满期给付金的特殊处理
            // zm 2009.7.13
            String cvrgNo = cvrgList.get(0).getCCvrgNo();
            if (PolicyHardCodeConstants.CVRG_000001.equals(cvrgNo)) {//为理财险的险别号
                cvrgList.get(0).setNResvNum3(null);//清除满期给付金
            }

            //清空
            for (AbstractCvrgVO cvrg : cvrgList) {
                cvrg.setNPrm(null);
                cvrg.setNAmtVar(null);
                cvrg.setNPrmVar(null);
                cvrg.setNCalcPrmVar(null);
                cvrg.setNIndemVar(null);
            }

        }
    }

    /**
     * 清空一些数据用于不同产品（投）保单拷贝
     */
    public void cleanForDiffProdCopy(String dstProdNo) throws VoManipulateException {
        /*
         * 判断如下:
         * 1. 同产品复制
         *    1. 除了共保信息不复制，其他都复制包括车主信息
         *    2. 缴费计划不复制[20090709]
         * 2. 同大类复制
         *    1. 复制基本信息
         *	 2. 复制投保人信息
         *	 3. 复制被保人信息
         *	 4. 如果复制与被复制的产品都为车险，则需要复制车辆信息与驾驶员信息与车主信息
         *	 5. 缴费计划不复制[20090709]
         * 3. 不同大类复制
         *    1. 复制基本信息
         *	 2. 复制投保人信息
         *	 3. 复制被保人信息
         *	 4. 缴费计划不复制[20090709]
         *
         * 业务咨询: 田军
         * 车险复制车主信息【2011-3-8】
         */

        // 移除不必要的组件
        this.removeComponent("Pay"); //缴费计划
        this.removeComponent("PrmCoef");//缴费计划
        /*
         * 判断 投保产品是否车险[车险需要复制车险标的]
         * 20090617 yjhuang@isoftstone.com
         */
        IProdService prodService = (IProdService) SpringUtils.getSpringBean("prodService");
        String srcKindNo;
        String dstKindNo;
        try {
            srcKindNo = prodService.getProdKindNo(this.getProdNo());
            dstKindNo = prodService.getProdKindNo(dstProdNo);
        } catch (BusinessServiceException e) {
            throw new VoManipulateException("查询产品大类失败.", e);
        }

        boolean vhlFlag = StringUtils.equals(srcKindNo,PolicyHardCodeConstants.PROD_KIND_03)
                && StringUtils.equals(dstKindNo, PolicyHardCodeConstants.PROD_KIND_03) ? true : false;

        List<String> removeNameList = new ArrayList<String>();
        for (String compName : this.getComposition().keySet()) {
            if (!StringUtils.equals(compName, "Base") && !StringUtils.equals(compName, "Applicant")
                    && !StringUtils.equals(compName, "Insured")) {
                if (vhlFlag) {
                    if (StringUtils.equals(compName, "Vhl")) {
                        continue;
                    } else if (StringUtils.equals(compName, "VhlDrv")) {
                        continue;
                    }else if(StringUtils.equals(compName, "Vhlowner")){
                    	continue;
                    }else if(StringUtils.equals(compName, "Acctinfo")){
                    	continue;
                    }
                }
                removeNameList.add(compName);
            }
        }

        // 清除组件
        for (String compName : removeNameList) {
            this.removeComponent(compName);
        }

        // 所有vo都要进行的处理
        PersistenceEvictor persistEvictor = new PersistenceEvictor(
                (CommonDao) SpringUtils.getSpringBean("commonDao"));
        VoBatchManipulator batchModifier = new VoBatchManipulator();
        String[] props2Clean = new String[] { "CPkId", "CRowId", "CAppNo", "CEdrNo" };
        VoPropertiesCleaner propsCleaner = new VoPropertiesCleaner(props2Clean, true);
        VoPropertySetter edrPrjNoSetter = new VoPropertySetter("NEdrPrjNo", new Long(0), true);
        VoOprFieldsCleaner oprFieldsCleaner = new VoOprFieldsCleaner();

        batchModifier.addManipulator(persistEvictor);
        batchModifier.addManipulator(propsCleaner);
        batchModifier.addManipulator(edrPrjNoSetter);
        batchModifier.addManipulator(oprFieldsCleaner);

        this.manipulateAllEntries(batchModifier);

        // 个别vo特殊处理
        if (this.containsComponent("Base")) {
            AbstractBaseVO base = this.getBase();
            this.getBase().setCProdNo(null);
            this.getBase().setCPlyNo(null);
            this.getBase().setNAmt(null);
            this.getBase().setNCalcPrm(null);
            this.getBase().setNPrm(null);
            this.getBase().setNCiJntAmt(null);
            this.getBase().setNCiJntPrm(null);
            this.getBase().setTIssueTm(null);

            this.getBase().setCOrigPlyNo(null);
            this.getBase().setCRelPlyNo(null);
            base.setCAppPrsnCde(null);
            base.setCAppPrsnNme(null);
            base.setCResvTxt1(null);

            //清空批改信息
            base.setNAmtVar(null);
            base.setNPrmVar(null);
            base.setNCalcPrmVar(null);
            base.setNIndemLmtVar(null);
            base.setCSaveMrk("0");
            base.setNSavingVar(null);
            base.setCEdrCtnt(null);
            base.setTEdrAppTm(null);
            base.setTEdrBgnTm(null);
            base.setTEdrEndTm(null);
            base.setTNextEdrBgnTm(EndorseConstants.MAX_TIME);
            base.setTNextEdrEndTm(EndorseConstants.MAX_TIME);
            base.setTNextEdrUdrTm(null);

            //20090706 yjhuang@isoftstone.com 设置操作员与操作时间字段的值
            this.getBase().setCOprCde(CurrentUser.getUser().getOpRelCde());
            this.getBase().setTOprTm(new Date());
            
            //清空特别约定,还有其他一些无用信息  2011-3-9
            base.setCUnfixSpc(null);
            base.setTTermnTm(null);
            base.setCEdrMrk(null);
            base.setCEdrRsnBundleCde(null);
            base.setNBefEdrPrjNo(null);
            base.setNBefEdrPrm(null);
            base.setNBefEdrAmt(null);
            base.setNBefEdrSaving(null);
            base.setCUdrCde(null);
            base.setCUdrDptCde(null);
            base.setCUdrMrk(null);
            base.setTUdrTm(null);
            base.setCMinUndrCls(null);
            base.setCMinUndrDpt(null);
            base.setCBrkrCde(null);//Base.CBrkrCde清空 代理人 此时不能判断代理人以及其代理协议是否有效。
            base.setCAgtAgrNo(null);
            
            base.setNBasePrm(null);
            base.setNAllPrm(null);
            base.setCRenewMrk(PolicyHardCodeConstants.BASE_CRENEWMRK_0);//续保标志
            base.setCSlsId(null);//业务员工号
            base.setCSlsCde(null);//业务员
            base.setCSlsTel(null);//业务员电话
            base.setCPrnNo(null);
        }
    }

    /**
     * 清空并设置一些数据用于续保保单拷贝
     */
    public void cleanAndSetForRenewProdCopy() throws VoManipulateException {

        int term = LongTermProductHelper.getYearsOfProductPeriod(this.getBase().getCProdNo());// 获得产品的保险期限
        String sPlyNo = this.getBase().getCPlyNo();

        // 所有vo都要进行的处理
        PersistenceEvictor persistEvictor = new PersistenceEvictor(
                (CommonDao) SpringUtils.getSpringBean("commonDao"));
        VoBatchManipulator batchModifier = new VoBatchManipulator();
        String[] props2Clean = new String[] { "CPkId", "CRowId", "CAppNo", "CEdrNo" };
        VoPropertiesCleaner propsCleaner = new VoPropertiesCleaner(props2Clean, true);
        VoPropertySetter edrPrjNoSetter = new VoPropertySetter("NEdrPrjNo", new Long(0), true);
        VoOprFieldsCleaner oprFieldsCleaner = new VoOprFieldsCleaner();
        batchModifier.addManipulator(persistEvictor);
        batchModifier.addManipulator(propsCleaner);
        batchModifier.addManipulator(edrPrjNoSetter);
        batchModifier.addManipulator(oprFieldsCleaner);

        this.manipulateAllEntries(batchModifier);

        // 基本信息特殊处理
        if (this.containsComponent("Base")) {
            AbstractBaseVO base = this.getBase();
            this.getBase().setCAppTyp("A");
            this.getBase().setCPlySts("I");
            this.getBase().setNAmt(null);
            this.getBase().setNCalcPrm(null);
            this.getBase().setNPrm(null);
            this.getBase().setNCiJntAmt(null);
            this.getBase().setNCiJntPrm(null);
            this.getBase().setTIssueTm(null);

            //清空批改信息
            base.setNAmtVar(null);
            base.setNPrmVar(null);
            base.setNCalcPrmVar(null);
            base.setNIndemLmtVar(null);
            base.setCSaveMrk("0");
            base.setNSavingVar(null);
            base.setCEdrCtnt(null);
            base.setTEdrAppTm(null);
            base.setTEdrBgnTm(null);
            base.setTEdrEndTm(null);
            base.setTNextEdrBgnTm(EndorseConstants.MAX_TIME);
            base.setTNextEdrEndTm(EndorseConstants.MAX_TIME);
            base.setTNextEdrUdrTm(null);

            //20090706 yjhuang@isoftstone.com 设置操作员与操作时间字段的值
            this.getBase().setCOprCde(CurrentUser.getUser().getOpRelCde());
            this.getBase().setTOprTm(new Date());
        }

        // 设置续保保险起止期
        Date bgnTm = PolicyElementHelper.getRenewEffectDateOnPrevExpireDate(
                this.getBase().getTInsrncEndTm(), PolicyElementHelper.TIME_SYSTEM_0_24);
        Date endTm = PolicyElementHelper.getExpireDateOnEffectDate(bgnTm,
                PolicyElementHelper.TIME_SYSTEM_0_24, term);
        this.getBase().setTInsrncBgnTm(bgnTm);
        this.getBase().setTInsrncEndTm(endTm);
        this.getBase().setCOrigPlyNo(sPlyNo);
        this.getBase().setCRenewMrk(PolicyConstants.RENEW_TYPE_SAVE);
        //}

        // 险别信息特殊处理
        if (this.containsComponent("Cvrg")) {

            // 设置险别起止期，每个险别的起止期在原来基础上延长n年，n为保险期限
            List<AbstractCvrgVO> cvrgEntryList = this.getCoverages();
            if (CollectionUtils.isNotEmpty(cvrgEntryList)) {
                for (AbstractCvrgVO cvrgVo : cvrgEntryList) {

                    cvrgVo.setTBgnTm(bgnTm);
                    cvrgVo.setTEndTm(endTm);

                    //清空批改信息
                    cvrgVo.setNAmtVar(null);
                    cvrgVo.setNPrmVar(null);
                    cvrgVo.setNCalcPrmVar(null);
                    cvrgVo.setNIndemVar(null);
                }
            }
        }

        /*
         * 20090709 yjhuang@isoftstone[注释]
        // 缴费计划特殊处理
        if (this.containsComponent("Pay")) {
            // 设置缴费计划起止期，每个缴费计划的起止期在原来基础上加n年，n为保险期限
            List<AbstractPayVO> payEntryList = this.getComponent("Pay");
            if (payEntryList != null && !payEntryList.isEmpty()) {
                for (AbstractPayVO payVo : payEntryList) {

                    payVo.setTPayBgnTm(bgnTm);
                    payVo.setTPayEndTm(endTm);
                }
            }
        }*/
    }

    /**
     * 清空一些数据用于套餐保单子产品拷贝
     *
     * @throws VoManipulateException
     */
    public void cleanForPkgProdCopy() throws VoManipulateException {
        // 移除不必要的组件
        this.removeComponent("Cvrg");
        this.removeComponent("FixSpec"); // 固定特别约定

        // 所有vo都要进行的处理
        PersistenceEvictor persistEvictor = new PersistenceEvictor(
                (CommonDao) SpringUtils.getSpringBean("commonDao"));
        VoBatchManipulator batchModifier = new VoBatchManipulator();
        VoPropertySetter appNoCleaner = new VoPropertySetter("CAppNo", null, false);
        VoPropertySetter pkIdCleaner = new VoPropertySetter("CPkId", null, true);
        VoPropertySetter rowIdCleaner = new VoPropertySetter("CRowId", null, true);
        VoOprFieldsCleaner oprFieldsCleaner = new VoOprFieldsCleaner();
        batchModifier.addManipulator(persistEvictor);
        batchModifier.addManipulator(appNoCleaner);
        batchModifier.addManipulator(pkIdCleaner);
        batchModifier.addManipulator(rowIdCleaner);
        batchModifier.addManipulator(oprFieldsCleaner);

        this.manipulateAllEntries(batchModifier);

        // 个别vo特殊处理
        // base
        if (this.containsComponent("Base")) {
            this.getBase().setCProdNo(null);
            this.getBase().setCPlyNo(null);
            this.getBase().setNAmt(null);
            this.getBase().setNCalcPrm(null);
            this.getBase().setNPrm(null);
            this.getBase().setNCiJntAmt(null);
            this.getBase().setNCiJntPrm(null);
            this.getBase().setCOrigPlyNo(null);
            this.getBase().setCRelPlyNo(null);
            this.getBase().setCResvTxt1(null);

            //20090706 yjhuang@isoftstone.com 设置操作员与操作时间字段的值
            this.getBase().setCOprCde(CurrentUser.getUser().getOpRelCde());
            this.getBase().setTOprTm(new Date());
        }
        // pay
        if (this.containsComponent("Pay")) {
            for (AbstractPayVO pay : (List<AbstractPayVO>) this.getComponent("Pay")) {
                pay.setNPayablePrm(null);
                pay.setNPaidPrm(null);
            }
        }

    }

    /**
     *
     * 复制为同产品投保单
     *
     * @return
     */
    public PolicyApplication copyAsSameProdApp() throws VoManipulateException {
        PolicyApplication plyApp = (PolicyApplication) this.clone(VoHelper.VO_TYPE_APP);
        plyApp.cleanForSameProdCopy();
        return plyApp;
    }

    /**
     * 复制为不同产品投保单
     *
     * @return
     */
    public PolicyApplication copyAsDiffProdApp(String dstProdNo) throws VoManipulateException {
        PolicyApplication plyApp = (PolicyApplication) this.clone(VoHelper.VO_TYPE_APP);
        plyApp.cleanForDiffProdCopy(dstProdNo);
        return plyApp;
    }

    // //////////////////////////////////////////////////
    // 标志为删除的组件访问
    // //////////////////////////////////////////////////
    public void putComponentToDelete(String componentName, List component) {
        this.toDelete.put(componentName, component);
    }

    public Set<String> getComponentToDeleteNameSet() {
        return this.toDelete.keySet();
    }

    public List getComponentToDelete(String componentName) {
        return this.toDelete.get(componentName);
    }

    /**
     * 复制组件 author: Yesic 2008-11-04
     */
    public void copyComponent(PolicyApplication srcApp, String componentName) {
        List componentList = srcApp.getComponent(componentName);
        if (!CollectionUtils.isEmpty(componentList)) {
            this.getComposition().put(componentName, componentList);
        }
    }

    /**
     * 设置保单、投保单默认值
     *
     * @author yjhuang@isoftstone.com 2009-01-04
     */
    public void setDefaultValue() {
        // 基本信息 初始化信息
        this.setPropertyOfEntry(this.getBase(), "CRenewMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CInwdMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CCiMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CGrpMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CListorcolMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CMasterMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CPkgMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CRegMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CDecMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CForeignMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CImporexpMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CManualMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CInstMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CVipMrk", "0", true);
        this.setPropertyOfEntry(this.getBase(), "CAppTyp", AppConstants.APP_TYPE_PLY, true);
        this.setPropertyOfEntry(this.getBase(), "TAppTm", new Date(), true);
        this.setPropertyOfEntry(this.getBase(), "NCalcPrmVar", new Double(0), true);
        this.setPropertyOfEntry(this.getBase(), "CAmtCur", "01", true); // 保额 币种
        this.setPropertyOfEntry(this.getBase(), "CPrmCur", "01", true); // 保费 币种
        this.setPropertyOfEntry(this.getBase(), "CLongTermMrk", "0", true); // 多年期标志
        this.setPropertyOfEntry(this.getBase(), "COprTyp", "1", true); // 保单号生成方式
        this.setPropertyOfEntry(this.getBase(), "CUdrMrk", "0", true); // 核保标志
        this.setPropertyOfEntry(this.getBase(), "CRiFacMrk", "0", true); // 临分标志
        this.setPropertyOfEntry(this.getBase(), "CRiMrk", "0", true); // 再保确认标志
        this.setPropertyOfEntry(this.getBase(), "CAppStatus", "1", true); // 申请单状态
        this.setPropertyOfEntry(this.getBase(), "CLatestMrk", "1", true); // 是否最新单标志
        this.setPropertyOfEntry(this.getBase(), "NPrmRmbExch", new Double(1), true); // 汇率

        // 投保人 初始化信息
       // this.setPropertyOfEntry(this.getApplicant(), "CStkMrk", "0", true);
    }

    /**
     * 根据指定的对象，设置属性值
     *
     * @param entry
     *            对象
     * @param propName
     *            属性名称
     * @param value
     *            属性值
     * @param bCheck
     *            非空检查[true: 只有空值才会设置默认值]
     * @author yjhuang@isoftstone.com 2009-01-04
     */
    public void setPropertyOfEntry(Object entry, String propName, Object value, boolean bCheck) {
        if (bCheck) {
            Object obj = this.getPropertyOfEntry(entry, propName);
            if (obj == null) {
                this.setPropertyOfObjectValue(entry, propName, value);
            } else if (obj instanceof String) {
                if ("".equals((String) obj)) {
                    this.setPropertyOfObjectValue(entry, propName, value);
                }
            }
        } else {
            this.setPropertyOfObjectValue(entry, propName, value);
        }
    }

    /**
     * 分析承保大对象 把Map<"Base", List<BaseVO>>的组件结构修改成Map<"Base", List<VO属性名称,
     * 值>>
     *
     * @param absPly
     *            承保大对象
     * @return
     * @author yjhuang@isoftstone.com 2009-01-12
     */
    public Map<String, List<Map<String, Object>>> getCompositionMapData() {
        Map<String, List<Map<String, Object>>> mapListData = new HashMap<String, List<Map<String, Object>>>();
        Map<String, List> compostionMapList = this.getComposition();
        if (compostionMapList == null || compostionMapList.isEmpty()) {
            return mapListData;
        }
        for (Map.Entry<String, List> entry : compostionMapList.entrySet()) {
            List objList = entry.getValue();
            List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
            if (objList == null || objList.isEmpty()) {
                continue;
            }
            for (Object obj : objList) {
                if (obj == null) {
                    continue;
                }
                Map<String, Object> mapData = new HashMap<String, Object>();
                try {
                    BeanTool.bean2Map(obj, mapData);
                    mapList.add(mapData);
                } catch (Exception e) {
                    logger.error("分析承保大对象组件 -> BeanTool.bean2Map(obj, mapData)时出现异常", e);
                }
            }
            if (!mapList.isEmpty()) {
                mapListData.put(entry.getKey(), mapList);
            }
        }
        return mapListData;
    }
    /** 快速出单
     * add by hjxing 2012-09-15  
     * update by pxg 2010-10-17 代码1638--1647行 添加车船税需要清空的字段。
     * update by pxg 2010-10-18 代码1666--1671行 添加险别需要清空的字段。
     * 清空一些数据用于相同产品保单拷贝
     */
    public void qucikCleanForSameProdCopy() throws VoManipulateException {
        /*
         * 判断如下:
         * 1. 同产品复制
         *    1. 除了共保信息不复制，其他都复制
         *    2. 缴费计划不复制[20090709]
         * 2. 同大类复制
         *    1. 复制基本信息
         *	 2. 复制投保人信息
         *	 3. 复制被保人信息
         *	 4. 如果复制与被复制的产品都为车险，则需要复制车辆信息与驾驶员信息
         *	 5. 缴费计划不复制[20090709]
         * 3. 不同大类复制
         *    1. 复制基本信息
         *	 2. 复制投保人信息
         *	 3. 复制被保人信息
         *	 4. 缴费计划不复制[20090709]
         *
         * 业务咨询: 田军
         */

        // 移除不必要的组件
        /*this.removeComponent("Ci"); // 共保信息
        this.removeComponent("Pay"); //缴费计划
        this.removeComponent("VsTax");//车船税
        this.removeComponent("PrmCoef");//优惠系数
        */
        // 所有vo都要进行的处理
        PersistenceEvictor persistEvictor = new PersistenceEvictor(
                (CommonDao) SpringUtils.getSpringBean("commonDao"));
        String[] props2Clean = new String[] { "CPkId", "CRowId", "CAppNo", "CEdrNo" };
        VoPropertiesCleaner propsCleaner = new VoPropertiesCleaner(props2Clean, true);
        VoPropertySetter edrPrjNoSetter = new VoPropertySetter("NEdrPrjNo", new Long(0), true);
        VoOprFieldsCleaner oprFieldsCleaner = new VoOprFieldsCleaner();
        VoBatchManipulator batchModifier = new VoBatchManipulator();
        batchModifier.addManipulator(persistEvictor);
        batchModifier.addManipulator(propsCleaner);
        batchModifier.addManipulator(edrPrjNoSetter);
        batchModifier.addManipulator(oprFieldsCleaner);

        this.manipulateAllEntries(batchModifier);
        // 个别vo特殊处理
        if (this.containsComponent("Base")) {
            AbstractBaseVO base = this.getBase();
            //base.setCQuickFlag("1");//设置为快速出单 20121119  设置快速出单状态移到保存时设置
            base.setCPlyNo(null);
            base.setTIssueTm(null);
            base.setCOrigPlyNo(null);
            base.setCRelPlyNo(null);
            base.setCAppPrsnCde(null);
            base.setCAppPrsnNme(null);
            base.setCResvTxt1(null);
            base.setNPrm(null);
            base.setNAmt(null);
        
            //清空批改信息
            base.setNAmtVar(null);
            base.setNPrmVar(null);
            base.setNCalcPrmVar(null);
            base.setNIndemLmtVar(null);
            base.setCSaveMrk("0");
            base.setNSavingVar(null);
            base.setCEdrCtnt(null);
            base.setTEdrAppTm(null);
            base.setTEdrBgnTm(null);
            base.setTEdrEndTm(null);
            base.setCEdrType(null);   // add  by  baidong  2011 07 20
            base.setTNextEdrBgnTm(EndorseConstants.MAX_TIME);
            base.setTNextEdrEndTm(EndorseConstants.MAX_TIME);
            base.setTNextEdrUdrTm(null);

            //20090706 yjhuang@isoftstone.com 设置操作员与操作时间字段的值
            base.setCOprCde(CurrentUser.getUser().getOpRelCde());
            base.setTOprTm(new Date());
            base.setCAppTyp("A");//设置保单类型为保单
            //清空特别约定  2011-3-9
//            base.setCUnfixSpc(null);
            //清除保费相关 
            base.setNBasePrm(null);
            base.setNAllPrm(null);
            base.setNIrrRatio(0.0);//浮动率
            base.setCSlsId(null);//业务员工号
            base.setCSlsNme(null);//业务员名称
            base.setCSlsCde(null);//业务员
            base.setCSlsTel(null);//业务员电话
            base.setCBrkrCde(null);//Base.CBrkrCde清空 代理人 此时不能判断代理人以及其代理协议是否有效。
            base.setCBrkSlsCde(null);//代理人
            base.setCBrkSlsName(null);//代理人名称
            base.setCAgtAgrNo(null);
            base.setCPrnNo(null);
            //营改增增加字段,清空
            base.setNVat(0.0);//N_vat 增值税额
            base.setNPrice(0.0);//    保费收入
            base.setNVatVar(0.0);// 增值税变化值
            base.setNPriceVar(0.0);// 保费收入变化值
            base.setCChaVatType(null);//手续费纳税类型
            
        }
        /******************add by 幸海军清空商业险系数 20121019**************************/
        if (this.containsComponent("PrmCoef")) {
        	AbstractPrmCoefVO prmCoefVO = this.getPremiumCoef();
        	if(prmCoefVO!=null){
        		prmCoefVO.setCVhlMod(null);//车损车型系数(老.旧.稀.特 1.3-2.0)：
        		prmCoefVO.setCCusLoy(null);//续保情况
        		prmCoefVO.setCAgoClmRec(null);//无赔款优待及上年赔款记录
        		prmCoefVO.setCNdiscRsn(null);//不浮动原因
        		prmCoefVO.setNMulRdr(null);//多险种同时投保(0.95-1.00) 
        		prmCoefVO.setNAutoChaCoef(null);//自主渠道系数
        		prmCoefVO.setNAutoCheCoef(null);//自主核保系数
        		prmCoefVO.setNClaimTime(null);//上年理赔次数
        		prmCoefVO.setNTotalClaimAmount(null);//上年结案金额
        		prmCoefVO.setCDeprotectionFlag(null);    //是否脱保0:否,1:是
        	}
        	
        }
        
        
        /*******start by pxg 2010.10.17 add清空车船税*********/
        if (this.containsComponent("VsTax")) {
        	List<AbstractVsTaxVO> vsTaxList =  this.getComponent("VsTax");
        	vsTaxList.get(0).setCTaxUnit(null);//计税单位
        	vsTaxList.get(0).setNAnnUnitTaxAmt(null);//年单位税额(元)
        	vsTaxList.get(0).setNTaxableAmt(null);//当年应缴(元)
        	vsTaxList.get(0).setNAggTax(0.0);//合计(元)
        }
        /*******and by pxg 2010.10.17 add清空车船税*********/
        
        if (this.containsComponent("Cvrg")) {
            List<AbstractCvrgVO> cvrgList = this.getCoverages();

            // 对理财险满期给付金的特殊处理
            // zm 2009.7.13
            if(cvrgList!=null && cvrgList.size()>0){
            	String cvrgNo = cvrgList.get(0).getCCvrgNo();
                if (PolicyHardCodeConstants.CVRG_000001.equals(cvrgNo)) {//为理财险的险别号
                    cvrgList.get(0).setNResvNum3(null);//清除满期给付金
                }
            }

            //清空
            for (AbstractCvrgVO cvrg : cvrgList) {
                cvrg.setNPrm(null);
                cvrg.setNAmtVar(null);
                cvrg.setNPrmVar(null);
                cvrg.setNCalcPrmVar(null);
                cvrg.setNIndemVar(null);
                /*******start by pxg 2010.10.18 add清空险别的几个保费*********/
                cvrg.setNBefPrm(null);//折前保费
                cvrg.setNBasePrm(null);//基本保费
                cvrg.setNRate(null);//费率%
                cvrg.setNDductPrm(null);//不计免赔保费
                /*******and by pxg 2010.10.18 add清空险别的几个保费*********/
                cvrg.setNBasePurePrm(null);
            }

        }
        
       /* //如果不是快速出单出的单，那么将车型编码清空；否则，续保/复制原广信车型库的保单的话，可能会与精友车型库对不上，可能出问题
        if (this.containsComponent("Vhl")) {
        	boolean isQuickApp = false;		//是否快速出单
        	if (this.containsComponent("Base")) {
        		AbstractBaseVO base = this.getBase();
        		isQuickApp = "1".equals(base.getCQuickFlag());
        	}
        	
        	if (!isQuickApp) {	//不是快速出单出的单，则将车型编码清空
        		AbstractVhlVO vhlVO = this.getVhl();
            	vhlVO.setCModelCde(null);
        	}
        }*/
        
    }

}
