/**
 * create date: Aug 11, 2008
 */
package com.isoftstone.pcis.policy.dm.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.isoftstone.fwk.dao.DaoException;
import com.isoftstone.fwk.security.CurrentUser;
import com.isoftstone.fwk.util.SpringUtils;
import com.isoftstone.pcis.policy.app.edrrsn.bo.EdrBase;
import com.isoftstone.pcis.policy.app.edrrsn.bo.EntryChangedDiffElement;
import com.isoftstone.pcis.policy.app.edrrsn.bo.IComponentDiff;
import com.isoftstone.pcis.policy.app.edrrsn.bo.IEntryDiff;
import com.isoftstone.pcis.policy.common.utils.VoToucher;
import com.isoftstone.pcis.policy.core.exception.VoManipulateException;
import com.isoftstone.pcis.policy.prod.base.dao.PolicyDAO;
import com.isoftstone.pcis.policy.vo.AbstractEdrCmpItemVO;
import com.isoftstone.pcis.policy.vo.AbstractEdrRsnVO;

/**
 * @author jiangqf
 *
 */
public abstract class AbstractEndorsement {
    private static final Logger logger = Logger.getLogger(AbstractEndorsement.class);

    private Policy oldPolicy = null;
    /**
     * newPolicy 在批改申请对象中是申请表中的数据(相当于PolicyApplicant) 在批单（大）对象中是保单表中的数据（Policy)
     */
    private AbstractPolicy newPolicy;

    private EdrBase edrBase;
    private List<AbstractEdrRsnVO> edrRsnList = new ArrayList<AbstractEdrRsnVO>();
    private List<AbstractEdrCmpItemVO> edrCmpItemList = new ArrayList<AbstractEdrCmpItemVO>();

    //
    private Map<String, IComponentDiff> diff = new HashMap<String, IComponentDiff>();

    /**
     * 说明：返回批改EdrBase 对象
     *   修改说明：刚从数据库查出来的批改大对象并有没初始化EdrBase信息，ec处理时需要EdrBase对象
     *   修改原理：EdrBase信息是 newPolicy.getBase()一个子集，保存在同一张表中（向yaos确认过）
     *   modfiy by zhugh  2013-07-04
     * @return edrBase
     */
    public EdrBase getEdrBase(){
    	if(this.edrBase==null){
    		if(this.newPolicy!=null){
    			try {
    				EdrBase  eb = new EdrBase();
					PropertyUtils.copyProperties(eb, this.newPolicy.getBase());
					this.edrBase = eb;
				} catch (Exception e) {
					//此处异常不需要向外抛出
					logger.info("初始化批改大对象EdrBase信息异常！！！");
				}
    		}
    	}
        return this.edrBase;
    }

    public void setEdrBase(EdrBase edrBase) {
        this.edrBase = edrBase;
    }

    /**
     * @return edrRsnList
     */
    public List<AbstractEdrRsnVO> getEdrRsnList() {
        return this.edrRsnList;
    }

    /**
     * @param edrRsnList
     *            edrRsnList置入值
     */
    public void setEdrRsnList(List edrRsnList) {
        this.edrRsnList = edrRsnList;
    }

    /**
     * @return edrCmpItemList
     */
    public List<AbstractEdrCmpItemVO> getEdrCmpItemList() {
        return this.edrCmpItemList;
    }

    /**
     * @param edrCmpItemList
     *            edrCmpItemList置入值
     */
    public void setEdrCmpItemList(List edrCmpItemList) {
        this.edrCmpItemList = edrCmpItemList;
    }

    /**
     * @return oldPolicy
     */
    public Policy getOldPolicy() {
        if (this.oldPolicy != null) {
            return this.oldPolicy;
        }
        if (this.newPolicy == null) {
            return null;
        } else {
            try {
                String plyNo = this.newPolicy.getBase().getCPlyNo();
                Long edrPrjNo = this.newPolicy.getBase().getNBefEdrPrjNo();
                PolicyDAO policyDao = (PolicyDAO) SpringUtils.getSpringBean("policyDao");
                this.oldPolicy = policyDao.getPlySnapshot(plyNo, edrPrjNo);
                return this.oldPolicy;
            } catch (DaoException e) {
                e.printStackTrace();
                return null;
            }
        }

    }

    /**
     * @param oldPolicy
     *            oldPolicy置入值
     */
    public void setOldPolicy(Policy oldPolicy) {
        this.oldPolicy = oldPolicy;
    }

    /**
     * @return newPolicy
     */
    public AbstractPolicy getNewPolicy() {
        return this.newPolicy;
    }

    /**
     * @param newPolicy
     *            newPolicy置入值
     */
    public void setNewPolicy(AbstractPolicy newPolicy) {
        this.newPolicy = newPolicy;
    }

    /**
     * 添加批改原因
     *
     * @param rsn
     *            加入edrRsnList的原因
     */
    public void addEdrRsn(AbstractEdrRsnVO rsn) {
        this.edrRsnList.add(rsn);
    }

    /**
     * 添加批改项
     *
     * @param cmpItem
     *            加入edrCmpItemList的批改项
     */
    public void addEdrCmpItem(AbstractEdrCmpItemVO cmpItem) {
        this.edrCmpItemList.add(cmpItem);
    }

    // ////////////////////////////////
    /**
     * 获得比较数据
     *
     * @return
     */
    public Map<String, IComponentDiff> getDiff() {
        return this.diff;
    }

    /**
     * 设置比较数据
     *
     * @param diff
     */
    public void setDiff(Map<String, IComponentDiff> diff) {
        this.diff = diff;
    }

    /**
     * 添加组件比较数据
     *
     * @param componentDiff
     */
    public void addComponentDiff(IComponentDiff componentDiff) {
        this.diff.put(componentDiff.getComponentName(), componentDiff);
    }

    /**
     * 获得除险别和交费计划外所有组件的变更对象
     *
     * @param componentName
     *            要素分类名或"*#"， *#代表除Cvrg和Pay的所有要素
     * @param opr
     *            add|Remove|change|terminate
     * @return
     */
    /*public List<IEntryDiff> getEntryDiffList(String componentName, String opr) {
        if (this.diff.isEmpty()) {
            return null;
        }

        List<IEntryDiff> entryDiffList = new ArrayList<IEntryDiff>();
        if (StringUtils.equals(componentName, "Cvrg") || StringUtils.equals(componentName, "Pay")) {
            return null;
        } else if (StringUtils.equals(componentName, "*#")) {
            Set<String> comNameSet = new HashSet<String>();
            comNameSet.addAll(this.diff.keySet());
            comNameSet.remove("Cvrg");
            comNameSet.remove("Pay");
            if (comNameSet.isEmpty()) {
                return null;
            }
            for (String comName : comNameSet) {
                entryDiffList.addAll(this.getEntryDiffList(comName, "remove"));
                entryDiffList.addAll(this.getEntryDiffList(comName, "change"));
                entryDiffList.addAll(this.getEntryDiffList(comName, "add"));
            }
        } else {
            if (this.diff.containsKey(componentName)) {
                IComponentDiff comDiff = this.diff.get(componentName);
                if (StringUtils.equalsIgnoreCase(opr, "remove")) {
                    // 只可能是PlyComponentDiff
                    if (comDiff instanceof PolyComponentDiff) {
                        entryDiffList.addAll(((PolyComponentDiff) comDiff).getRemoved());
                    }
                } else if (StringUtils.equalsIgnoreCase(opr, "change")) {
                    if (comDiff instanceof PolyComponentDiff) {
                        entryDiffList.addAll(((PolyComponentDiff) comDiff).getChanged());
                    } else if (comDiff instanceof MonoComponentDiff) {
                        entryDiffList.add(((MonoComponentDiff) comDiff).getChanged());
                    }
                } else if (StringUtils.equalsIgnoreCase(opr, "add")) {
                    if (comDiff instanceof PolyComponentDiff) {
                        entryDiffList.addAll(((PolyComponentDiff) comDiff).getAdded());
                    }
                }
            } else {
                return null;
            }
        }
        for (IEntryDiff ed : entryDiffList) {
        	if("FixSpec".equals(componentName)){
        		for (EntryChangedDiffElement de : ((EntryChangedDiff)ed).getElementDiffCollection()){
        			String old = this.getOldPolicy().getBase().getCUnfixSpc();
        			String news= this.getNewPolicy().getBase().getCUnfixSpc();
        			if(news==null||"null".equals(news)){news="(无)";}
    				if(old==null||"null".equals(old)){old="(无)";}
        			de.setOld(old);
        			de.setNew(news);
        		}
        	}
        }

        return entryDiffList;
    }
*/
    // /////////////////////////////
    /**
     * 更新操作人操作时间
     */
    public void updateOprCdeTm() {
        try {
            Map nameValueMap = new HashMap();
            Date oprTime = new Date();
            String oprId = CurrentUser.getUser().getOpCde();
            nameValueMap.put("CUpdCde", oprId);
            nameValueMap.put("TUpdTm", oprTime);

            // jiangqf注: 不能在此更新oldPolicy的操作时间
            // 保存批改申请单时，不会更新被批改的保单信息
            // 生成保单/回填老保单，不在此处处理
            // if(this.getOldPolicy()!=null){
            // this.getOldPolicy().setPropertiesForAllEntries(nameValueMap);
            // }

            if (this.getNewPolicy() != null) {
                nameValueMap.put("CCrtCde", oprId);
                nameValueMap.put("TCrtTm", oprTime);
                this.getNewPolicy().setPropertiesForAllEntries(nameValueMap);
            }

            if (CollectionUtils.isNotEmpty(this.getEdrCmpItemList())) {
                for (AbstractEdrCmpItemVO edrCmpItem : this.getEdrCmpItemList()) {
                    VoToucher.touch(edrCmpItem, oprId, oprTime);
                }
            }

            if (CollectionUtils.isNotEmpty(this.getEdrRsnList())) {
                for (AbstractEdrRsnVO edrRsn : this.getEdrRsnList()) {
                    VoToucher.touch(edrRsn, oprId, oprTime);
                }
            }
        } catch (VoManipulateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getPolicyNo() {
    	if(this.getEdrBase()!=null)
    		return this.getEdrBase().getCPlyNo();
    	else if(this.getOldPolicy()!=null)
    		return this.getOldPolicy().getBase().getCPlyNo();
    	else
    		return this.getNewPolicy().getBase().getCPlyNo();
    }

}
