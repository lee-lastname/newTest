/**
 * create date: Aug 8, 2008
 */
package com.isoftstone.pcis.policy.prod.base.dao;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;
import javax.sql.RowSet;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.isoftstone.fwk.dao.BaseDao;
import com.isoftstone.fwk.dao.DaoException;
import com.isoftstone.fwk.dao.SQLPara;
import com.isoftstone.fwk.dao.vo.FilterParameter;
import com.isoftstone.fwk.dao.vo.ResultParameter;
import com.isoftstone.fwk.service.BusinessServiceException;
import com.isoftstone.fwk.util.DateTool;
import com.isoftstone.fwk.util.SpringUtils;
import com.isoftstone.pcis.common.recycle.ISerianoRecycle;
import com.isoftstone.pcis.common.recycle.SerianoRecycleImpl;
import com.isoftstone.pcis.common.recycle.vo.SerianoRecycleVO;
import com.isoftstone.pcis.policy.app.payseemoney.vo.PayConfirmInfoVO;
import com.isoftstone.pcis.policy.common.constants.PolicyConstants;
import com.isoftstone.pcis.policy.common.utils.VoToucher;
import com.isoftstone.pcis.policy.core.helper.VoHelper;
import com.isoftstone.pcis.policy.dm.bo.AbstractPolicy;
import com.isoftstone.pcis.policy.dm.bo.Policy;
import com.isoftstone.pcis.policy.vo.AppAcctinfoVO;
import com.isoftstone.pcis.policy.vo.AppBaseVO;
import com.isoftstone.pcis.policy.vo.AppFixSpecVO;
import com.isoftstone.pcis.policy.vo.PlyAcctinfoVO;
import com.isoftstone.pcis.policy.vo.PlyApplicantVO;
import com.isoftstone.pcis.policy.vo.PlyBaseVO;
import com.isoftstone.pcis.policy.vo.PlyCiVO;
import com.isoftstone.pcis.policy.vo.PlyFeeVO;
import com.isoftstone.pcis.policy.vo.PlyImgIdxVO;
import com.isoftstone.pcis.policy.vo.PlyInsuredVO;
import com.isoftstone.pcis.policy.vo.TelnetOrderVO;
import com.isoftstone.pcis.prod.service.IProdService;
import com.isoftstone.pcis.sys.right.service.GrtRightService;
import com.isoftstone.utils.DateUtil;

/**
 * 保单处理DAO
 * @author Yesic
 */
public class PolicyDAO extends BaseDao {
    private static Logger logger = Logger.getLogger(PolicyDAO.class);

    private IProdService prodService;

    private SerianoRecycleVO serianoRecyclevo;
    
    public SerianoRecycleVO getSerianoRecyclevo() {
        return this.serianoRecyclevo;
    }

    public void setProdService(IProdService prodService) {
        this.prodService = prodService;
    }

    /**
     * 保存投保单费用信息
     * @param appNo 投保单号
     * @param plyNo 保单号
     * @throws DaoException
     * @author yjhuang@isoftstone.com
     * 2009-05-15
     */
   /* public void savePlyFeeInfo(String appNo, String plyNo) throws DaoException {
        try {
            //获取投保单费用信息
            List<AppFeeVO> appList = this.find("FROM AppFeeVO WHERE CAppNo=? order by CFeetypCde", appNo);
            List<PlyFeeVO> plyList = new ArrayList<PlyFeeVO>();

            String hql = "FROM AppBaseVO t WHERE t.CAppNo=?";
            List<AppBaseVO> list = super.find(hql, appNo);
            AppBaseVO base = list.get(0);
            Long nEdrPrjNo = 0l;
            if (base.getNEdrPrjNo() != null) {
                nEdrPrjNo = base.getNEdrPrjNo();
            }

            //映射产生保单费用信息
            for (AppFeeVO feeVo : appList) {
                PlyFeeVO vo = new PlyFeeVO();
                PropertyUtils.copyProperties(vo, feeVo);
                vo.setCPlyNo(plyNo);
                vo.setCCrtCde(null); //清空创建人
                vo.setTCrtTm(null); //清空创建时间

                vo.setCLatestMrk(PolicyConstants.FLAG_TRUE);
                vo.setNEdrPrjNo(nEdrPrjNo);
                vo.setCEdrNo(base.getCEdrNo());
                plyList.add(vo);

                VoToucher.touch(vo);
            }

            //保存保单费用信息
            this.saveOrUpdateAll(plyList);
            this.flush();
        } catch (Exception e) {
            logger.error("PolicyDAO->savePlyFeeInfo() 保存保单费用信息异常");
            throw new DaoException(e);
        }
    }*/

    /**
     * 保存投保单
     * @param ply
     */
/*    public void saveOrUpdatePolicy(Policy ply) throws DaoException {
        boolean idAsigned = StringUtils.isBlank(ply.getBase().getCPlyNo()) ? false : true; // 用于异常时恢复
        String plyNo = "";
        try {
            // 没有投保单号则生成投保单号
            if (!idAsigned) {
                ISerianoRecycle iserianoRecycle = (ISerianoRecycle) SpringUtils.getSpringBean("iserianoRecycle");
                plyNo = iserianoRecycle.getBizNo(ply.getProdNo(), ply.getBase().getCDptCde(),
                        SerianoRecycleImpl.BIZTYPE_PLY);
                if ("".equals(plyNo) || plyNo == null) {
                    plyNo = PolicyIdGenerator.generatePolicyNo(ply);
                }
            } else {
                plyNo = ply.getBase().getCPlyNo();
            }

            Date now = new Date();

            // 先保存主表
            PlyBaseVO base = ply.getBase();
            base.setCPlyNo(plyNo);
            VoToucher.touch(base, now);
            super.saveOrUpdate(base);
            this.flush();
            // 删除需删除的数据
            for (String componentName : ply.getComponentToDeleteNameSet()) {
                List component = ply.getComponentToDelete(componentName);
                this.delete(component);
            }

            Set<String> componentNameSet = new HashSet<String>();
            componentNameSet.addAll(ply.getComponentNameSet());
            componentNameSet.remove(null);// YS
            componentNameSet.remove("Base"); // 不再保存主表
            for (String componentName : componentNameSet) {
                List com = ply.getComponent(componentName);
                for (Object entry : com) {
                    VoToucher.touch(entry);
                    this.saveOrUpdate(entry);
                }
            }

//            this.updateImage(ply); // 更新影象信息
//            this.flush();
//            if ("1".equals(ply.getBase().getCGrpMrk())) {
//                this.updateGroup(ply); // 更新团单信息
//            }
            //20090515 增加对费用信息的保存处理
//            this.savePlyFeeInfo(ply.getAppNo(), plyNo);
        } catch (DaoException e) {
            e.printStackTrace();
            if (!idAsigned) {
                ply.setPlyNo(null); // 未保存过的投保单，恢复到无投保单号
                ply.clearOprFieldsIgnoreException(); // 清空创建/修改人时间
            }
            // 设置保单号回收信息用于回收控制
            this.serianoRecyclevo = new SerianoRecycleVO(plyNo);
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            // 设置保单号回收信息用于回收控制
            this.serianoRecyclevo = new SerianoRecycleVO(plyNo);
            throw new DaoException("保存保单，saveOrUpdatePolicy()时出错", e);
        }
    }*/

    /**
     * 更新团单信息 [投保单、保单共用一套表]
     * Web_App_Grp_Member、Web_App_Grp_Cvrg
     * @param ply 保单对象
     * @return 处理结果
     * @throws DaoException
     */
    public boolean updateGroup(Policy ply) throws DaoException {
    	if (!"1".equals(ply.getBase().getCGrpMrk())){
    		return true;
    	}
        boolean b = false;
        try {
            // 更新团单成员
            StringBuffer sSQL = new StringBuffer();
            sSQL.append(" UPDATE AppGrpMemberVO SET CPlyNo=:CPlyNo, ").append(
                    " CUdrMrk=:CUdrMrk, TUdrTm=:TUdrTm, ").append(
                    " CUpdCde=:CUpdCde, TUpdTm=:TUpdTm  ").append(" WHERE CAppNo=:CAppNo ");

            HashMap paramMap = new HashMap();
            paramMap.put("CPlyNo", ply.getPlyNo());
            paramMap.put("CUdrMrk", ply.getBase().getCUdrMrk());
            paramMap.put("TUdrTm", ply.getBase().getTUdrTm());
            paramMap.put("CUpdCde", ply.getBase().getCUpdCde());
            paramMap.put("TUpdTm", ply.getBase().getTUpdTm());
            paramMap.put("CAppNo", ply.getAppNo());
            this.batchUpdate(sSQL.toString(), paramMap);

            // 更新团单险别
            sSQL = new StringBuffer();
            sSQL.append(" UPDATE AppGrpCvrgVO SET CPlyNo=:CPlyNo, ").append(
                    " CUdrMrk=:CUdrMrk, TUdrTm=:TUdrTm, ").append(
                    " CUpdCde=:CUpdCde, TUpdTm=:TUpdTm  ").append(" WHERE CAppNo=:CAppNo ");
            this.batchUpdate(sSQL.toString(), paramMap);
            b = true;
        } catch (Exception e) {
            throw new DaoException("生成保单，更新团单信息时出错", e);
        }
        return b;
    }

    /**
     * 更新影象信息 [投保单、保单共用一套表]
     * Web_Ply_Img_Idx
     * @param ply 保单对象
     * @return 处理结果
     * @throws DaoException
     */
    public boolean updateImage(Policy ply) throws DaoException {
        boolean b = false;
        List resultList = new ArrayList();
        try {
            StringBuffer sSQL = new StringBuffer();
            sSQL.append(" UPDATE PlyImgIdxVO SET CPlyNo=:CPlyNo, ").append(
                    " CUpdCde=:CUpdCde, TUpdTm=:TUpdTm  ").append(" WHERE CAppNo=:CAppNo ");

            HashMap paramMap = new HashMap();
            paramMap.put("CPlyNo", ply.getPlyNo());
            paramMap.put("CUpdCde", ply.getBase().getCUpdCde());
            paramMap.put("TUpdTm", ply.getBase().getTUpdTm());
            paramMap.put("CAppNo", ply.getAppNo());

            //MYSQL对于带索引不存在的数据进行更新会导致锁表
		    String Shql= "FROM PlyImgIdxVO where CAppNo = ?";
			resultList = this.find(Shql.toString(), ply.getAppNo());	
			if (resultList!=null&&resultList.size()>0)
			{   
				this.batchUpdate(sSQL.toString(), paramMap);
				
            }
			
            b = true;
            
        } catch (Exception e) {
            throw new DaoException("生成报单，更新影象信息时出错", e);
        }
        return b;
    }
    
    
    

    /**
     * 根据申请单号(主键)查询保单
     * @param appNo 申请单号
     * @return 保单对象(Policy)
     */
    public Policy getPolicyByAppNo(String appNo) throws BusinessServiceException {
        Policy policy = new Policy();

        // 先查Base，如果没有记录就不用查从表
        try {
            PlyBaseVO base = (PlyBaseVO) super.getById(PlyBaseVO.class, appNo);
            if (base == null) {
                return null;
            } else {
                policy.setBase(base);
            }

            // 从产品定义服务读取表（类）名，排除Base，并加入险别Cvrg(可能没有传过来)
            List<String> defComponentNameList = this.prodService.getTableNameList(policy.getBase().getCProdNo());
            Set<String> componentNameSet = new HashSet<String>(defComponentNameList);
            // 除去Base
            componentNameSet.remove("Base");
            componentNameSet.remove(null);// ys
            // 加入Cvrg
            componentNameSet.add("Cvrg");

            for (String componentName : componentNameSet) {
                String hql = "from " + VoHelper.getPlyVoClassName(componentName)
                        + " c where c.CAppNo=?";
                List component = this.find(hql, appNo);
                policy.putComponent(componentName, component);
            }
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return policy;
    }

    /**
     * 根据保单号获得保单(最新记录)
     * @param plyNo 保单号
     * @return 保单对象
     */
 /*   public Policy getPolicyByPlyNo(String plyNo) throws DaoException {
        return this.getPlyLatestSnapshotByPlyNo(plyNo);
    }*/

    /**
     * 获得保单主档对象(最新记录)
     *
     * @param plyNo
     * @return 保单主档对象
     */
    public PlyBaseVO getPlyBaseByPlyNo(String plyNo) {
        return this.getPlyLatestSnapshotBaseByPlyNo(plyNo);
    }

    /**
     * 获得保单最新记录主档
     *
     * @param plyNo
     * @return 保单主档对象
     */
    public PlyBaseVO getPlyLatestSnapshotBaseByPlyNo(String plyNo) {
        try {
            String baseHql = "from PlyBaseVO b where b.CPlyNo=:CPlyNo and b.CLatestMrk=:CLatestMrk";
            HashMap baseParaMap = new HashMap<String, Object>();
            baseParaMap.put("CPlyNo", plyNo);
            baseParaMap.put("CLatestMrk", "1");
            List result = this.search(baseHql, baseParaMap);
            if (result != null && result.size() > 0) {
                return (PlyBaseVO) result.get(0);
            } else {
                return null;
            }
        } catch (DaoException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获得最新保单对象局部的内容
     * @param plyNo
     * @param componentNameSet
     * @return 保单对象（只包含指定的组件）
     */
 /*   public Policy getPlyLatestSnapshotPartByPlyNo(String plyNo, Set<String> componentNameSet)
            throws DaoException {
    	Policy ply = new Policy();
        for (String componentName : componentNameSet) {
            
            if(componentName.equals("Cvrg")){
                String hql = "from " + VoHelper.getPlyVoClassName(componentName)
                        + " c where c.CPlyNo=:CPlyNo and c.CLatestMrk=:CLatestMrk  order by c.NSeqNo ";
                HashMap<String, Object> paraMap = new HashMap<String, Object>();
                paraMap.put("CPlyNo", plyNo);
                paraMap.put("CLatestMrk", "1");
                List component = this.search(hql, paraMap);
                if (component == null || component.size() == 0) {
                    continue;
                }
                ply.putComponent(componentName, component);
            } else if(componentName.equals("GrpMember")){
            	GrpMemberVOMgr grpMemberVOMgr=new GrpMemberVOMgr();
                List component = grpMemberVOMgr.getLatestGrpMemberList(plyNo);
                if (component == null || component.size() == 0) {
                    continue;
                }
                ply.putComponent(componentName, component);
            } else{
                String hql = "from " + VoHelper.getPlyVoClassName(componentName)
                        + " c where c.CPlyNo=:CPlyNo and c.CLatestMrk=:CLatestMrk";
                HashMap<String, Object> paraMap = new HashMap<String, Object>();
                paraMap.put("CPlyNo", plyNo);
                paraMap.put("CLatestMrk", "1");
                List component = this.search(hql, paraMap);
                if (component == null || component.size() == 0) {
                    continue;
                }
                ply.putComponent(componentName, component);
            }
        }
        return ply;
    	
    	//老团单实现代码
//        Policy ply = new Policy();
//        for (String componentName : componentNameSet) {
//            String hql ="";
//            HashMap<String, Object> paraMap = new HashMap<String, Object>();
//            if(componentName.equals("GrpMember")||componentName.equals("GrpCvrg")){ //团单成员信息，投保单与保单共用一个表web_App_Grp_Member ztf 09.11.30
//                 hql = "from " + VoHelper.getAppVoClassName(componentName)
//                + " c where c.CPlyNo=?";
//                 paraMap.put("CPlyNo", plyNo);
//                 //Author zhangmn 2010.2.22
//                 //判断在团单表中没有PlyNo，就把PlyNo加上
//                 List component = this.find(hql, plyNo);
//                 if(component!=null && component.size() != 0){
//                	 ply.putComponent(componentName, component);
//                 }
////                 if (component == null || component.size() == 0) {
////                     this.updateGroup(ply); // 更新团单信息
////                 }
//
//            }else{
//                 hql = "from " + VoHelper.getPlyVoClassName(componentName)
//                + " c where c.CPlyNo=? and c.CLatestMrk=?";
//                
//                 paraMap.put("CPlyNo", plyNo);
//                 paraMap.put("CLatestMrk", "1");
//                 
////                 List component = this.search(hql, paraMap);
//                 List component = this.find(hql,new Object[]{plyNo,"1"});
//                 if (component == null || component.size() == 0) {
//                	 continue;
//                 }
//                 ply.putComponent(componentName, component);
//            }
//            
//        }
//        return ply;
    }*/

    /**
     * 获通过申请单号得保单对象局部的内容
     * @param plyNo
     * @param componentNameSet
     * @return 保单对象（只包含指定的组件）
     */
    public Policy getPlySnapshotPartByAppNo(String app, Set<String> componentNameSet)
            throws DaoException {
        Policy ply = new Policy();
        for (String componentName : componentNameSet) {
            String hql = "from " + VoHelper.getPlyVoClassName(componentName)
                    + " c where c.CAppNo=:CAppNo ";
            HashMap<String, Object> paraMap = new HashMap<String, Object>();
            paraMap.put("CAppNo", app);
            List component = this.search(hql, paraMap);
            if (component == null || component.size() == 0) {
                continue;
            }
            ply.putComponent(componentName, component);
        }
        return ply;

    }

    /**
     * 获得历史保单对象局部的内容
     * @param plyNo 保单号
     * @param edrPrjNo 批改序号
     * @param componentNameSet
     * @return 保单对象（只包含指定的组件）
     */
    public Policy getPlyHistorySnapshotPartByPlyNo(String plyNo, Long edrPrjNo,
            Set<String> componentNameSet) throws DaoException {
        Policy ply = new Policy();
        for (String componentName : componentNameSet) {
            String hql = "from " + VoHelper.getPlyVoClassName(componentName)
                    + " c where c.CPlyNo=:CPlyNo and c.NEdrPrjNo=:NEdrPrjNo";
            HashMap<String, Object> paraMap = new HashMap<String, Object>();
            paraMap.put("CPlyNo", plyNo);
            paraMap.put("NEdrPrjNo", edrPrjNo);
            List component = this.search(hql, paraMap);
            if (component == null || component.size() == 0) {
                continue;
            }
            ply.putComponent(componentName, component);
        }
        return ply;

    }

    /**
     * 根据保单号获得保单最新记录
     * @param plyNo 保单号
     * @return 保单对象
     */
 /*   public Policy getPlyLatestSnapshotByPlyNo(String plyNo) throws DaoException {

        try {
            // 先查Base，如果没有记录就不用查从表
            PlyBaseVO base = this.getPlyBaseByPlyNo(plyNo);
            if (base == null) {
                return null;
            }

            // 从产品定义服务读取表（类）名，排除Base，并加入险别Cvrg(可能没有传过来)
            List<String> defComponentNameList = this.prodService.getTableNameList(base.getCProdNo());
            Set<String> componentNameSet = new HashSet<String>(defComponentNameList);
            componentNameSet.remove(null);
            // 除去Base
            componentNameSet.remove("Base");
            // 加入Cvrg
            componentNameSet.add("Cvrg");

            Policy policy = this.getPlyLatestSnapshotPartByPlyNo(plyNo, componentNameSet);
            policy.setBase(base);

            return policy;
        } catch (Exception e) {
            DaoException de = new DaoException(e);
            de.setErrorMsg(e.getMessage() + "\n查询保单最新记录时出错:保单号" + plyNo);
            throw de;
        }
    }*/

    /**
     * 获得原始保单对象局部的内容
     *
     * @param plyNo
     * @param componentNameSet
     * @return
     */
    public Policy getPlyPrimalSnapshotPartByPlyNo(String plyNo, Set<String> componentNameSet)
            throws DaoException {
        Policy ply = new Policy();
        for (String componentName : componentNameSet) {
            String hql = "from " + VoHelper.getPlyVoClassName(componentName)
                    + " c where c.CPlyNo=:CPlyNo and NEdrPrjNo=0";
            HashMap<String, Object> paraMap = new HashMap<String, Object>();
            paraMap.put("CPlyNo", plyNo);
            List component = this.search(hql, paraMap);
            if (component == null || component.size() == 0) {
                continue;
            }
            ply.putComponent(componentName, component);
        }
        return ply;
    }

    /**
     * 查询保单最新记录
     * @param fp
     * @return
     */
    public ResultParameter queryPolicyList(FilterParameter fp) throws BusinessServiceException {
        ResultParameter rp = null;
        try {
        	
        	Map<String, Object> paramMap = fp.getParems();
        	// 处理时间+1问题
        	String oldTAppTm1 = (String) paramMap.get("TAppTm1");
        	String oldTAppTm2 = (String) paramMap.get("TAppTm2");
        	DateUtil dateUtil = (DateUtil) SpringUtils.getSpringBean("dateUtil");
        	String newTAppTm1 = dateUtil.DayPlusOrSubtract(oldTAppTm1,-1);
        	String newTAppTm2 = dateUtil.DayPlusOrSubtract(oldTAppTm2,1);
        	paramMap.put("TAppTm1", newTAppTm1);
        	paramMap.put("TAppTm2", newTAppTm2);
        	
            String hql = "from PlyBaseVO t1,PlyApplicantVO t2,PlyInsuredVO t3 where t1.CPlyNo=':CPlyNo' "
                    + "and t1.TAppTm=':TAppTm' "
                    + "and t1.CDptCde=':CDptCde' "
                    + "and t1.CProdNo=':CProdNo' "
                    + "and t1.Vhl.CPlateNo=':Vhl.CPlateNo' "
                    + "and t1.TAppTm>=to_date(':TAppTm1','yyyy-mm-dd') "
//                  + "and t1.TAppTm>=to_date(':TAppTm1','yyyy-mm-dd')-1 "
                    + "and t1.TAppTm<=to_date(':TAppTm2','yyyy-mm-dd') "
//                  + "and t1.TAppTm<=to_date(':TAppTm2','yyyy-mm-dd')+1 "
                    + "and t2.CAppNme=':CAppNme' "
                    + "and t3.CInsuredNme=':name' "
                    + "and t1.CPlyNo = t2.CPlyNo " + "and t2.CPlyNo = t3.CPlyNo ";

            rp = this.query(hql, fp);
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        rp.getData();

        return rp;
    }

    /**
     * 获得保单原始记录主档
     *
     * @param plyNo
     * @return 保单主档对象
     */
    public PlyBaseVO getPlyPrimalSnapshotBaseByPlyNo(String plyNo) {
        try {
            String baseHql = "from PlyBaseVO b where b.CPlyNo=:CPlyNo and NEdrPrjNo=0";
            HashMap baseParaMap = new HashMap<String, Object>();
            baseParaMap.put("CPlyNo", plyNo);
            List result = this.search(baseHql, baseParaMap);
            if (result != null && result.size() > 0) {
                return (PlyBaseVO) result.get(0);
            } else {
                return null;
            }
        } catch (DaoException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 获得保单原始记录
     * @param plyNo
     * @return
     */
    public Policy getPlyPrimalSnapshotByPlyNo(String plyNo,Long edrPrjNo) throws BusinessServiceException {
        String sAppNo = "";
        try {
            // 根据保单号与批改序号为0查询原始保单
            StringBuffer sSQL = new StringBuffer();
            sSQL.append("SELECT CAppNo FROM PlyBaseVO WHERE NEdrPrjNo=? AND CPlyNo=? ");
            Object param[] = {edrPrjNo,plyNo};
            List list = super.find(sSQL.toString(), param);
            
            sAppNo = (String) list.get(0);
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return this.getPolicyByAppNo(sAppNo);
    }

    /**
     * 获得保单原始记录
     * @param plyNo
     * @return
     */
    public Policy getPlyPrimalSnapshotByPlyNo(String plyNo) throws BusinessServiceException {
        try {
            PlyBaseVO base = this.getPlyPrimalSnapshotBaseByPlyNo(plyNo);

            if (base == null) {
                return null;
            }

            return this.getPolicyByAppNo(base.getCAppNo());

        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
    }

    /**
     * 获得保单历史记录
     * @return
     */
    public List querySnapshotsByPlyNo(String plyNo) throws BusinessServiceException {
        List list = new ArrayList();
        try {
            StringBuffer sSQL = new StringBuffer();
            sSQL.append(" SELECT new map( ").append("  CAppNo as CAppNo,  ").append(
                    "  CPlyNo as CPlyNo, ").append("  CEdrNo as CEdrNo, ").append(
                    "  TUdrTm as TUdrTm, ").append("  NPrmVar as NprmVar, ").append("  NEdrPrjNo as NEdrPrjNo, ").append(
                    "  TEdrBgnTm as TEdrBgnTm, ").append("  TEdrEndTm as TEdrEndTm ").append(" ) ").append(
                    " from PlyBaseVO ").append(" WHERE CPlyNo=? ").append(" ORDER BY NEdrPrjNo ");
            list = super.find(sSQL.toString(), plyNo);
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return list;
    }

    /**
     * 根据保单号和批改序号获得保单主档历史记录
     * @return
     */
    public PlyBaseVO getPlyBaseSnapshot(String plyNo, Long edrPrjNo) throws DaoException {
        String hql = "FROM PlyBaseVO WHERE CPlyNo=? AND NEdrPrjNo=?";
        Object param[] = { plyNo, edrPrjNo };
        List<PlyBaseVO> list = super.find(hql, param);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 根据保单号和批改序号获得保单历史记录
     * @return
     */
    public Policy getPlySnapshot(String plyNo, Long edrPrjNo) throws DaoException {
        Policy policy = new Policy();
        try {
            String hql = "from PlyBaseVO where CPlyNo=:CPlyNo and NEdrPrjNo=:NEdrPrjNo";
            HashMap<String, Object> paraMap = new HashMap<String, Object>();
            paraMap.put("CPlyNo", plyNo);
            paraMap.put("NEdrPrjNo", edrPrjNo);
            List<PlyBaseVO> baseList = this.search(hql, paraMap);
            if (baseList == null || baseList.isEmpty()) {
                return null;
            }
            PlyBaseVO base = baseList.get(0);
            String appNo = base.getCAppNo();

            policy.setBase(base);

            // 从产品定义服务读取表（类）名，排除Base，并加入险别Cvrg(可能没有传过来)
            List<String> defComponentNameList = this.prodService.getTableNameList(policy.getBase().getCProdNo());
            Set<String> compoenentNameSet = new HashSet<String>();
            for (String componentName : defComponentNameList) {
                if (componentName != null && !"".equals(componentName)) {
                    compoenentNameSet.add(componentName);
                }
            }

            // 移除处理
            compoenentNameSet.remove(AbstractPolicy.COMPONENT_NAME_BASE); // 除去Base
            compoenentNameSet.remove(AbstractPolicy.COMPONENT_NAME_GRP_MEMBER); // 移除团单成员

            // 添加处理
            compoenentNameSet.add(AbstractPolicy.COMPONENT_NAME_COVERAGE); // 加入Cvrg
            compoenentNameSet.remove(null);// ys

            for (String componentName : compoenentNameSet) {
                String voHql = "from " + VoHelper.getPlyVoClassName(componentName)
                        + " c where c.CAppNo=?";
                List component = this.find(voHql, appNo);

                policy.putComponent(componentName, component);
            }
        } catch (Exception e) {
            throw new DaoException(e);
        }
        return policy;
    }

    /**
     * @deprecated 建议使用 getPlySnapshot
     * @param CEdrNo
     * @return
     */
    @Deprecated
    public PlyBaseVO getPlyBaseByEdrNo(String CEdrNo) {
        String hql = "from PlyBaseVO where CEdrNo=?";
        List<PlyBaseVO> list = super.find(hql, CEdrNo);
        PlyBaseVO plyBaseVO = list.get(0);
        return plyBaseVO;
    }

    /**
     * 查询保单列表（续保）
     * @param CPlateNo 车牌号
     * @param CEngNo 发动机号
     * @param CFrmNo 车架号
     * @return
     * @throws BusinessServiceException
     */
    public List<Map> queryPlyNoList(String CPlateNo, String CEngNo, String CFrmNo)
            throws BusinessServiceException {
        List<Map> list = new ArrayList<Map>();
        try {
            StringBuffer sSQL = new StringBuffer();
            sSQL.append(" SELECT new map( ").append("  a.CPlyNo as COrigPlyNo ").append("  )").append(
                    " FROM PlyVhlVO a").append("  WHERE a.CPlateNo = :CPlateNo ").append(
                    " AND a.CEngNo = :CEngNo ").append("  AND a.CFrmNo = :CFrmNo ").append(
                    " ORDER BY a.TCrtTm ");

            HashMap paramMap = new HashMap();
            paramMap.put("CPlateNo", CPlateNo);
            paramMap.put("CEngNo", CEngNo);
            paramMap.put("CFrmNo", CFrmNo);

            list = super.search(sSQL.toString(), paramMap);
        } catch (Exception e) {
            logger.error("PolicyDAO -> queryPlyNoList");
            logger.error(e.getMessage());
            throw new BusinessServiceException(e);
        }
        return list;
    }

    /**
     * 在批单注销（回退到上一次状态）中使用
     * 根据批改序号和保单号获取保单对象
     * 功能:
     * @param CEdrNo
     * @return
     * @throws BusinessServiceException
     */
    public Policy getPlyBaseByNPrjEdrNo(String plyNo, String edrPrjNo)
            throws BusinessServiceException {

        Policy policy = new Policy();
        try {
            String hql = "from PlyBaseVO where CPlyNo=? and NEdrPrjNo=?";
            Object param[] = { plyNo, Long.parseLong(edrPrjNo) };
            List<PlyBaseVO> list = super.find(hql, param);
            PlyBaseVO base = list.get(0);
            String appNo = base.getCAppNo();
            if (base == null) {
                return null;
            } else {
                policy.setBase(base);
            }

            // 从产品定义服务读取表（类）名，排除Base，并加入险别Cvrg(可能没有传过来)
            List<String> defComponentNameList = this.prodService.getTableNameList(policy.getBase().getCProdNo());
            Set<String> compoenentNameSet = new HashSet<String>();
            for (String componentName : defComponentNameList) {
                compoenentNameSet.add(componentName);
            }

            // 除去Base
            compoenentNameSet.remove("Base");
            compoenentNameSet.remove("GrpMember");
            // 加入Cvrg
            compoenentNameSet.add("Cvrg");
            compoenentNameSet.remove(null);

            for (String componentName : compoenentNameSet) {
                String voHql = "from " + VoHelper.getPlyVoClassName(componentName)
                        + " c where c.CAppNo=?";
                List component = this.find(voHql, appNo);

                policy.putComponent(componentName, component);
            }
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return policy;
        // return plyBaseVO;
    }

    /**
     * 查询投保人信息列表（根据保单号）
     * @param plyNo 保单号
     * @return List<Map<String, String>>
     * @throws BusinessServiceException
     * 返回值格式：
     *   key:   Insured.CInsuredCde
     *   value:
     */
    /*public List<Map<String, String>> queryInsuredListByPlyNo(String plyNo)
            throws BusinessServiceException {
        List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
        try {
            boolean bGrpFlag = this.isGroup(plyNo);
            StringBuffer sSQL = new StringBuffer();

            if (bGrpFlag) {// 团单，查询 APP_GRP_MEMBER 表
            	GrpMemberVOMgr mgr = new GrpMemberVOMgr();
            	List<BaseGrpMemberVO> list = mgr.getLatestGrpMemberList(plyNo);
            	
            	// 被保人编码、被保人名称、性别、年龄、证件类型、证件号码、职业
                for (int i = 0; list != null && i < list.size(); i++) {
                	BaseGrpMemberVO vo = list.get(i);
                	
                	Map<String, String> map = new HashMap<String, String>();
            		map.put("ClientCde", vo.getCClntCde());
            		map.put("ClientNme", vo.getCNme());
            		map.put("CCertTyp",  vo.getCCertTyp());
            		map.put("CCertNo",   vo.getCCertNo());
            		// map.put("COccupCde", vo.getCOccupCde());
            		map.put("NSeqNo", String.valueOf(vo.getNSeqNo()));
            		map.put("CSex", "");
            		// map.put("CAge", "");
            		
            		retList.add(map);
                }
            } else {// 非团单，查询 PLY_INSURED 表
                sSQL.append("FROM PlyInsuredVO WHERE CAppNo in (SELECT CAppNo FROM PlyBaseVO WHERE CPlyNo=? AND CLatestMrk='1') ");
                
                // 被保人编码、被保人名称、性别、年龄、证件类型、证件号码、职业
                List list = super.find(sSQL.toString(), plyNo);
                for (int i = 0; list != null && i < list.size(); i++) {
                	Object voObj = list.get(i);
            		Map<String, String> map = new HashMap<String, String>();
            		PlyInsuredVO vo = (PlyInsuredVO) voObj;
            		map.put("ClientCde", vo.getCInsuredCde());
            		map.put("ClientNme", vo.getCInsuredNme());
            		map.put("CCertTyp", vo.getCCertfCls());
            		map.put("CCertNo", vo.getCCertfCde());
            		// map.put("COccupCde", vo.getCOccupCde());
            		map.put("CSex", vo.getCSex());
            		map.put("NSeqNo", String.valueOf(vo.getNSeqNo()));
            		// map.put("CAge",String.valueOf(DateUtils.calcAge(vo.getTBirthDate())));
            		
            		retList.add(map);
                }
            }

        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return retList;
    }
*/
    /**
     * 查询被保人的险别信息
     * @param plyNo 保单号
     * @param insuredCde 被保人编码
     * @return List<Map<String, String>>
     * @throws BusinessServiceException
     * 返回值格式：
     *   key:   Insured.CInsuredCde
     *   value:
     */
    public List<Map<String, String>> queryCvrgList(String plyNo, String insuredCde)
            throws BusinessServiceException {
        List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
        try {
            boolean bGrpFlag = this.isGroup(plyNo);
            StringBuffer sSQL = new StringBuffer();
            Object[] obj;

            if (bGrpFlag) {// 团单，查询 APP_GRP_CVRG 表
                sSQL.append(" SELECT new map(a.CCvrgNo as CvrgNo, SUM(a.NAmt) as NAmt, b.CNmeCn as CCvrgNme) "
                        + " FROM AppGrpCvrgVO a, PrdCvrgVO b, AppGrpMemberVO c " 
                        + " WHERE a.CCvrgNo=b.CCvrgNo " 
                        + " AND a.CMemberId=c.CPkId "
                        + " AND a.CPlyNo=? and c.CClntCde=? "
                        + " GROUP BY a.CCvrgNo, b.CNmeCn ");
                obj = new Object[] { plyNo, insuredCde };
            } else {// 非团单，查询 PLY_CVRG 表
                sSQL.append(" SELECT new map(a.CCvrgNo as CvrgNo, SUM(a.NAmt) as NAmt, b.CNmeCn as CCvrgNme) "
                        + " FROM PlyCvrgVO a, PrdCvrgVO b WHERE a.CCvrgNo=b.CCvrgNo "
                        + " AND a.CAppNo IN (SELECT CAppNo FROM PlyBaseVO WHERE CLatestMrk = '1' AND CPlyNo=?) "
                        + " GROUP BY a.CCvrgNo, b.CNmeCn ");
                obj = new Object[] { plyNo };
            }

            // 险别编码、险别名称、险别保额

            retList = super.find(sSQL.toString(), obj);

            // for(int i=0;list != null && i < list.size();i++){
            // Object voObj = list.get(i);
            // if(voObj instanceof PlyCvrgVO){
            // Map<String, String> map = new HashMap<String, String>();
            // PlyCvrgVO vo = (PlyCvrgVO)voObj;
            // map.put("CvrgNo", vo.getCCvrgNo());
            // map.put("NAmt", String.valueOf(vo.getNAmt()));
            // map.put("CCvrgNme", "");
            //
            // retList.add(map);
            // }else if(voObj instanceof AppGrpCvrgVO){
            // Map<String, String> map = new HashMap<String, String>();
            // AppGrpCvrgVO vo = (AppGrpCvrgVO)voObj;
            // map.put("CvrgNo", vo.getCCvrgNo());
            // map.put("NAmt", String.valueOf(vo.getNAmt()));
            // map.put("CCvrgNme", "");
            //
            // retList.add(map);
            // }
            // }
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return retList;
    }

    /**
     * 查询共保信息
     * @param plyNo 保单号
     * @return List<Map<String, String>>
     * @throws BusinessServiceException
     * 返回值格式：
     *   key:   Insured.CInsuredCde
     *   value:
     */
    public List<Map<String, String>> queryCiList(String plyNo) throws BusinessServiceException {
        List<Map<String, String>> retList = new ArrayList<Map<String, String>>();
        try {
            StringBuffer sSQL = new StringBuffer();
            sSQL.append("FROM PlyCiVO WHERE CPlyNo=? AND CSelfMrk IS NULL");
            Object[] obj = new Object[] { plyNo };
            List list = super.find(sSQL.toString(), obj);

            // 共保人编码、共保标识、共保比例

            for (int i = 0; list != null && i < list.size(); i++) {
                Object voObj = list.get(i);
                if (voObj instanceof PlyCiVO) {
                    Map<String, String> map = new HashMap<String, String>();
                    PlyCiVO vo = (PlyCiVO) voObj;
                    map.put("CCoinsurerCde", vo.getCCoinsurerCde());
                    map.put("CChiefMrk", vo.getCChiefMrk());
                    map.put("NCiShare", String.valueOf(vo.getNCiShare()));

                    retList.add(map);
                }
            }
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return retList;
    }

    /**
     * 根据查询器参数查询投保单信息列表
     * 条件:
     *  1. 查询器参数
     *  2. 需要查询的属性名称列表(格式:表名缩写.字段名  如:Cvrg.CCvrgNo)
     * 返回结果:
     *  List<Map<属性名称, 属性值>>
     * @param filter 查询器参数
     * @param propertyList 需要查询的属性列表
     * @return ResultParameter
     * @throws BusinessServiceException
     */
    public ResultParameter queryPlyListByFilter(FilterParameter filter, String dptCde,
            boolean includeSubDpt) throws BusinessServiceException {
        try {
            Map paramMap = filter.getParems();
            // paramMap.put("SubDpt", "1");
            // paramMap.put("CDptCde", "000002");
            // paramMap.put("CBsType", "0");
            // paramMap.put("TAccdntTm", "2008-11-15 18:06:53");
            // paramMap.put("CDptCde1", "912010000:天津分公司营业部");
            // paramMap.put("CPlyNo", "1000000010012008000013");

            // 个团单分开
            StringBuffer hsqlIp = new StringBuffer();
            StringBuffer hsqlGp = new StringBuffer();
            
            // 处理时间+1问题
            String oldDate = (String) paramMap.get("TInsrncBgnTmEnd");
            DateUtil dateUtil = (DateUtil) SpringUtils.getSpringBean("dateUtil");
            String newDate = dateUtil.DayPlusOrSubtract(oldDate,1);
            paramMap.put("TInsrncBgnTmEnd", newDate);
            
            hsqlIp.append(" SELECT new map( ")
            	.append(" a.CPlyNo as CPlyNo, ")
            	.append(" a.CProdNo as CProdNo, ")
            	.append(" b.CInsuredNme as CInsuredNme, ")
            	.append(" a.TUdrTm as TUdrTm, ")
            	.append(" a.TInsrncBgnTm as TInsrncBgnTm, ")
            	.append(" a.TInsrncEndTm as TInsrncEndTm, ")
            	.append(" a.CPrmSts as CPrmSts, ") // 保单实收状态
            	.append(" CASE a.CPlySts ")
            	.append("   WHEN 'I'")// 保单状态:I:// 非终止(有效或满期)
            	.append("     THEN ")
            	.append("      CASE ")
            	.append("        WHEN a.TInsrncEndTm < now() ")// 期满，返回0（无效）
            	.append("          THEN '0' ")
            // .append(" WHEN a.NIndemLmt IS NULL OR a.NIndemLmt <= 0
            // ")//赔偿限额为空或0，返回0（无效）
            // .append(" THEN '0' ")
            	.append("        ELSE '1' ")// 保单有效，返回1（有效）
            	.append("      END ")
            	.append("   ELSE '0' ")// 保单状态:T: Terminated // 终止(注退) 保单已终止，返回0（无效）
            	.append(" END as CPlyStatus ")
            	.append(" ) ")
            	.append(" FROM PlyBaseVO a, PlyInsuredVO b, PrdProdVO c, OrgDptVO o ")
            	.append(" WHERE a.CAppNo = b.CAppNo ")
            	.append(" AND a.CProdNo = c.CProdNo ")
            	.append(" AND a.CPlyNo = ':CPlyNo' ")
            	.append(" AND a.CProdNo = ':CProdNo' ")
            	.append(" AND a.CGrpMrk = '0' AND b.CInsuredCde = ':CInsuredCde' ")
            	.append(" AND b.CInsuredNme = ':CInsuredNme' ")
            	.append(" AND a.TInsrncBgnTm >= to_date(':TInsrncBgnTmStart', 'yyyy-mm-dd') ")
            	.append(" AND a.TInsrncBgnTm <  to_date(':TInsrncBgnTmEnd', 'yyyy-mm-dd') ")
//            	.append(" AND a.TInsrncBgnTm <  to_date(':TInsrncBgnTmEnd', 'yyyy-mm-dd')+1 ")
            	.append(" AND a.TUdrTm <= to_date(':TAccdntTm', 'yyyy-mm-dd hh24:mi:ss') ")
            	.append(" AND a.TNextEdrUdrTm > to_date(':TAccdntTm', 'yyyy-mm-dd hh24:mi:ss') ")
            	.append(" AND a.TInsrncBgnTm < to_date(':TNowDate', 'yyyy-mm-dd hh24:mi:ss') ")
            	.append(" AND a.CDptCde=o.CDptCde");

            hsqlGp.append(" SELECT new map( ")
            	.append(" a.CPlyNo as CPlyNo, ")
            	.append(" a.CProdNo as CProdNo, ")
            	.append(" b.CNme as CInsuredNme, ")
            	.append(" a.TUdrTm as TUdrTm, ")
            	.append(" a.TInsrncBgnTm as TInsrncBgnTm, ")
            	.append(" a.TInsrncEndTm as TInsrncEndTm, ")
            	.append(" a.CPrmSts as CPrmSts, ") // 保单实收状态
            	.append(" CASE a.CPlySts ")
            	.append("   WHEN 'I'")// 保单状态:I: // 非终止(有效或满期)
            	.append("     THEN ")
            	.append("      CASE ")
            	.append("        WHEN a.TInsrncEndTm < now() ")// 期满，返回0（无效）
            	.append("          THEN '0' ")
            // .append(" WHEN a.NIndemLmt IS NULL OR a.NIndemLmt <= 0
            // ")//赔偿限额为空或0，返回0（无效）
            // .append(" THEN '0' ")
            	.append("        ELSE '1' ")// 保单有效，返回1（有效）
            	.append("      END ")
            	.append("   ELSE '0' ")// 保单状态:T: Terminated
            // 终止(注退) 保单已终止，返回0（无效）
            	.append(" END as CPlyStatus ")
            	.append(" ) ")
            	.append(" FROM PlyBaseVO a, AppGrpMemberVO b, PrdProdVO c, OrgDptVO o ")
            	.append(" WHERE a.CAppNo = b.CAppNo ")
            	.append(" AND a.CProdNo = c.CProdNo ")
            	.append(" AND a.CPlyNo = ':CPlyNo' ")
            	.append(" AND a.CProdNo = ':CProdNo' ")
            	.append(" AND a.CGrpMrk = '1' AND b.CClntCde = ':CInsuredCde' ")
            	.append(" AND b.CNme = ':CInsuredNme' ")
            	.append(" AND a.TInsrncBgnTm >= to_date(':TInsrncBgnTmStart', 'yyyy-mm-dd') ")
            	.append(" AND a.TInsrncBgnTm <  to_date(':TInsrncBgnTmEnd', 'yyyy-mm-dd') ")
//            	.append(" AND a.TInsrncBgnTm <  to_date(':TInsrncBgnTmEnd', 'yyyy-mm-dd')+1 ")
            	.append(" AND a.TUdrTm <= to_date(':TAccdntTm', 'yyyy-mm-dd hh24:mi:ss') ")
//            	.append(" AND a.TNextEdrUdrTm > to_date(':TAccdntTm', 'yyyy-mm-dd hh24:mi:ss') ")
            	.append(" AND a.TInsrncBgnTm < to_date(':TNowDate', 'yyyy-mm-dd hh24:mi:ss') ")
            	.append(" AND a.CDptCde=o.CDptCde");

            if (includeSubDpt) {
                hsqlIp.append(" AND EXISTS ( SELECT CDptRelCde FROM OrgDptVO WHERE CDptCde=':CDptCde' AND o.CDptRelCde like concat('%',CDptRelCde,'%') ) ");
                hsqlGp.append(" AND EXISTS ( SELECT CDptRelCde FROM OrgDptVO WHERE CDptCde=':CDptCde' AND o.CDptRelCde like concat('%',CDptRelCde,'%') ) ");

                //hsqlIp.append(" AND o.CDptRelCde like ':CDptCde%' ");
                //hsqlGp.append(" AND o.CDptRelCde like ':CDptCde%' ");
            } else {
                hsqlIp.append(" AND a.CDptCde=':CDptCde' ");
                hsqlGp.append(" AND a.CDptCde=':CDptCde' ");
            }

            // 需要过滤保险起期在当天以后保单 yjhuang@isoftstone.com 2009-03-09
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sNowDate = sdf.format(new Date());
            paramMap.put("TNowDate", sNowDate);

            // 过滤车险 与 非车险 产品 yjhuang@isoftstone.com 2009-03-10
            String prodType = (String) paramMap.get("prodType");
            if ("0".equals(prodType)) {// 车险
                hsqlIp.append(" AND c.CKindNo = '03' ");
                hsqlGp.append(" AND c.CKindNo = '03' ");
            } else if ("1".equals(prodType)) {// 非车险
                hsqlIp.append(" AND c.CKindNo != '03' ");
                hsqlGp.append(" AND c.CKindNo != '03' ");
            }

            String prodNo = (String) paramMap.get("CProdNo");
            String kindNo = (String) paramMap.get("CKindNo"); // 产品大类
            List<String> kindList = new ArrayList<String>();

            if (kindNo != null && !"".equals(kindNo)) {
                kindList.add(kindNo);
            } else {
                kindList = null;
            }

            GrtRightService grtRightService = (GrtRightService) SpringUtils.getSpringBean("grtRightService");
            List<String> prodListByRole = grtRightService.getAllPermProd(kindList, null);
            // logger.debug("权限产品列表："+StringUtils.join(prodListByRole.toArray(),"','"));

            if (prodListByRole != null && !prodListByRole.isEmpty()) {
                hsqlIp.append(" AND a.CProdNo in (':ProdListByRole') ");
                hsqlGp.append(" AND a.CProdNo in (':ProdListByRole') ");
                paramMap.put("ProdListByRole", prodListByRole);
            }

            // if(prodNo == null || "".equals(prodNo)){
            // String bsType = (String)paramMap.get("CBsType");
            // List<String> prodListByType =
            // prodService.getProdListByBsType(bsType);
            // List<String> prodListByRole = new ArrayList<String>();
            //
            // GrtRightService grtRightService =
            // (GrtRightService)SpringUtils.getSpringBean("grtRightService");
            // RowSet rs = grtRightService.getPermissionProd(null);
            // while(rs.next()){
            // String sNo = rs.getString("C_PROD_NO");
            // if(sNo != null && !"".equals(sNo)){
            // prodListByRole.add(sNo);
            // }
            // }
            //
            // sSQL.append(" AND a.CProdNo in (':ProdListByType') ")
            // .append(" AND a.CProdNo in (':ProdListByRole') ");
            // filter.getParems().put("ProdListByType", prodListByType);
            // filter.getParems().put("ProdListByRole", prodListByRole);
            // }

            hsqlIp.append(" ORDER BY a.TUpdTm DESC ");
            hsqlGp.append(" ORDER BY a.TUpdTm DESC ");
            
            logger.debug("查询个单列表：" + hsqlIp.toString());
            ResultParameter rpi = super.query(hsqlIp.toString(), filter);
            
            logger.debug("查询团单列表：" + hsqlGp.toString());
            ResultParameter rpg = super.query(hsqlGp.toString(), filter);
            
            rpi.getData().addAll(rpg.getData());
            return rpi;
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
    }

    /**
     * 根据查询器参数查询投保单信息列表
     * 条件:
     * 	1. 查询器参数
     *  2. 需要查询的属性名称列表(格式:表名缩写.字段名  如:Cvrg.CCvrgNo)
     * 返回结果:
     *  List<Map<属性名称, 属性值>>
     * @param filter 查询器参数
     * @param propertyList 需要查询的属性列表
     * @return ResultParameter
     * @throws BusinessServiceException
     * @deprecated
     */
    public ResultParameter queryPlyListByFilter(FilterParameter filter, String loadSub)
            throws BusinessServiceException {
        try {
            Map paramMap = filter.getParems();
            // paramMap.put("SubDpt", "1");
            // paramMap.put("CDptCde", "000002");
            // paramMap.put("CBsType", "0");
            // paramMap.put("TAccdntTm", "2008-11-15 18:06:53");
            // paramMap.put("CDptCde1", "912010000:天津分公司营业部");
            // paramMap.put("CPlyNo", "1000000010012008000013");

            // 个团单分开
            StringBuffer hsqlIp = new StringBuffer();
            StringBuffer hsqlGp = new StringBuffer();
            
            // 处理时间+1问题
            String oldTInsrncBgnTmStart = (String) paramMap.get("TInsrncBgnTmStart");
            String oldTInsrncBgnTmEnd = (String) paramMap.get("TInsrncBgnTmEnd");
            DateUtil dateUtil = (DateUtil) SpringUtils.getSpringBean("dateUtil");
            String newTInsrncBgnTmStart = dateUtil.DayPlusOrSubtract(oldTInsrncBgnTmStart,-1);
            String newTInsrncBgnTmEnd = dateUtil.DayPlusOrSubtract(oldTInsrncBgnTmEnd,1);
            paramMap.put("TInsrncBgnTmStart", newTInsrncBgnTmStart);
            paramMap.put("TInsrncBgnTmEnd", newTInsrncBgnTmEnd);
            
            hsqlIp.append(" SELECT new map( ").append(" a.CPlyNo as CPlyNo, ").append(
                    " a.CProdNo as CProdNo, ").append(" b.CInsuredNme as CInsuredNme, ").append(
                    " a.TUdrTm as TUdrTm, ").append(" a.TInsrncBgnTm as TInsrncBgnTm, ").append(
                    " a.TInsrncEndTm as TInsrncEndTm, ").append(" a.CPrmSts as CPrmSts, ") // 保单实收状态
            .append(" CASE a.CPlySts ").append("   WHEN 'I'")// 保单状态:I:// 非终止(有效或满期)
            .append("     THEN ").append("      CASE ").append(
                    "        WHEN a.TInsrncEndTm < now() ")// 期满，返回0（无效）
            .append("          THEN '0' ")
            // .append(" WHEN a.NIndemLmt IS NULL OR a.NIndemLmt <= 0
            // ")//赔偿限额为空或0，返回0（无效）
            // .append(" THEN '0' ")
            .append("        ELSE '1' ")// 保单有效，返回1（有效）
            .append("      END ").append("   ELSE '0' ")// 保单状态:T: Terminated // 终止(注退) 保单已终止，返回0（无效）
            .append(" END as CPlyStatus ").append(" ) ").append(
                    " FROM PlyBaseVO a, PlyInsuredVO b, PrdProdVO c, OrgDptVO o ").append(
                    " WHERE a.CAppNo = b.CAppNo ").append(" AND a.CProdNo = c.CProdNo ").append(
                    " AND a.CPlyNo = ':CPlyNo' ").append(" AND a.CProdNo = ':CProdNo' ").append(
                    " AND a.CGrpMrk = '0' AND b.CInsuredCde = ':CInsuredCde' ").append(
                    " AND b.CInsuredNme = ':CInsuredNme' ").append(
                    " AND a.TInsrncBgnTm >= to_date(':TInsrncBgnTmStart', 'yyyy-mm-dd') ").append(
//                  " AND a.TInsrncBgnTm >= to_date(':TInsrncBgnTmStart', 'yyyy-mm-dd')-1 ").append(
                    " AND a.TInsrncBgnTm <= to_date(':TInsrncBgnTmEnd', 'yyyy-mm-dd') ").append(
//                  " AND a.TInsrncBgnTm <= to_date(':TInsrncBgnTmEnd', 'yyyy-mm-dd')+1 ").append(
                    " AND a.TUdrTm <= to_date(':TAccdntTm', 'yyyy-mm-dd hh24:mi:ss') ").append(
                    " AND a.TNextEdrUdrTm > to_date(':TAccdntTm', 'yyyy-mm-dd hh24:mi:ss') ").append(
                    " AND a.TInsrncBgnTm < to_date(':TNowDate', 'yyyy-mm-dd hh24:mi:ss') ").append(
                    " AND a.CDptCde=o.CDptCde ");

            hsqlGp.append(" SELECT new map( ").append(" a.CPlyNo as CPlyNo, ").append(
                    " a.CProdNo as CProdNo, ").append(" b.CNme as CInsuredNme, ").append(
                    " a.TUdrTm as TUdrTm, ").append(" a.TInsrncBgnTm as TInsrncBgnTm, ").append(
                    " a.TInsrncEndTm as TInsrncEndTm, ").append(" a.CPrmSts as CPrmSts, ") // 保单实收状态
            .append(" CASE a.CPlySts ").append("   WHEN 'I'")// 保单状态:I: // 非终止(有效或满期)
            .append("     THEN ").append("      CASE ").append(
                    "        WHEN a.TInsrncEndTm < now() ")// 期满，返回0（无效）
            .append("          THEN '0' ")
            // .append(" WHEN a.NIndemLmt IS NULL OR a.NIndemLmt <= 0
            // ")//赔偿限额为空或0，返回0（无效）
            // .append(" THEN '0' ")
            .append("        ELSE '1' ")// 保单有效，返回1（有效）
            .append("      END ").append("   ELSE '0' ")// 保单状态:T: Terminated
            // 终止(注退) 保单已终止，返回0（无效）
            .append(" END as CPlyStatus ").append(" ) ").append(
                    " FROM PlyBaseVO a, AppGrpMemberVO b, PrdProdVO c, OrgDptVO o ").append(
                    " WHERE a.CAppNo = b.CAppNo ").append(" AND a.CProdNo = c.CProdNo ").append(
                    " AND a.CPlyNo = ':CPlyNo' ").append(" AND a.CProdNo = ':CProdNo' ").append(
                    " AND a.CGrpMrk = '1' AND b.CCusCde = ':CInsuredCde' ").append(
                    " AND b.CNme = ':CInsuredNme' ").append(
                    " AND a.TInsrncBgnTm >= to_date(':TInsrncBgnTmStart', 'yyyy-mm-dd') ").append(
//                  " AND a.TInsrncBgnTm >= to_date(':TInsrncBgnTmStart', 'yyyy-mm-dd')-1 ").append(
                    " AND a.TInsrncBgnTm <= to_date(':TInsrncBgnTmEnd', 'yyyy-mm-dd') ").append(
//                  " AND a.TInsrncBgnTm <= to_date(':TInsrncBgnTmEnd', 'yyyy-mm-dd')+1 ").append(
                    " AND a.TUdrTm <= to_date(':TAccdntTm', 'yyyy-mm-dd hh24:mi:ss') ").append(
                    " AND a.TNextEdrUdrTm > to_date(':TAccdntTm', 'yyyy-mm-dd hh24:mi:ss') ").append(
                    " AND a.TInsrncBgnTm < to_date(':TNowDate', 'yyyy-mm-dd hh24:mi:ss') ").append(
                    " AND a.CDptCde=o.CDptCde ");

            boolean includeSubDpt = StringUtils.equals(loadSub, "1");
            if (includeSubDpt) {
                hsqlIp.append(" AND EXISTS ( SELECT CDptRelCde FROM OrgDptVO WHERE CDptCde=':CDptCde' AND o.CDptRelCde like concat('%',CDptRelCde,'%') ) ");
                hsqlGp.append(" AND EXISTS ( SELECT CDptRelCde FROM OrgDptVO WHERE CDptCde=':CDptCde' AND o.CDptRelCde like concat('%',CDptRelCde,'%') ) ");

                //hsqlIp.append(" AND o.CDptRelCde like ':CDptCde%' ");
                //hsqlGp.append(" AND o.CDptRelCde like ':CDptCde%' ");
            } else {
                hsqlIp.append(" AND a.CDptCde=':CDptCde' ");
                hsqlGp.append(" AND a.CDptCde=':CDptCde' ");
            }

            // 需要过滤保险起期在当天以后保单 yjhuang@isoftstone.com 2009-03-09
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sNowDate = sdf.format(new Date());
            paramMap.put("TNowDate", sNowDate);

            // 过滤车险 与 非车险 产品 yjhuang@isoftstone.com 2009-03-10
            String prodType = (String) paramMap.get("prodType");
            if ("0".equals(prodType)) {// 车险
                hsqlIp.append(" AND c.CKindNo = '03' ");
                hsqlGp.append(" AND c.CKindNo = '03' ");
            } else if ("1".equals(prodType)) {// 非车险
                hsqlIp.append(" AND c.CKindNo != '03' ");
                hsqlGp.append(" AND c.CKindNo != '03' ");
            }

            String prodNo = (String) paramMap.get("CProdNo");
            String kindNo = (String) paramMap.get("CKindNo"); // 产品大类
            List<String> kindList = new ArrayList<String>();

            if (kindNo != null && !"".equals(kindNo)) {
                kindList.add(kindNo);
            } else {
                kindList = null;
            }

            GrtRightService grtRightService = (GrtRightService) SpringUtils.getSpringBean("grtRightService");
            List<String> prodListByRole = grtRightService.getAllPermProd(kindList, null);
            // logger.debug("权限产品列表："+StringUtils.join(prodListByRole.toArray(),"','"));

            if (prodListByRole != null && !prodListByRole.isEmpty()) {
                hsqlIp.append(" AND a.CProdNo in (':ProdListByRole') ");
                hsqlGp.append(" AND a.CProdNo in (':ProdListByRole') ");
                paramMap.put("ProdListByRole", prodListByRole);
            }

            // if(prodNo == null || "".equals(prodNo)){
            // String bsType = (String)paramMap.get("CBsType");
            // List<String> prodListByType =
            // prodService.getProdListByBsType(bsType);
            // List<String> prodListByRole = new ArrayList<String>();
            //
            // GrtRightService grtRightService =
            // (GrtRightService)SpringUtils.getSpringBean("grtRightService");
            // RowSet rs = grtRightService.getPermissionProd(null);
            // while(rs.next()){
            // String sNo = rs.getString("C_PROD_NO");
            // if(sNo != null && !"".equals(sNo)){
            // prodListByRole.add(sNo);
            // }
            // }
            //
            // sSQL.append(" AND a.CProdNo in (':ProdListByType') ")
            // .append(" AND a.CProdNo in (':ProdListByRole') ");
            // filter.getParems().put("ProdListByType", prodListByType);
            // filter.getParems().put("ProdListByRole", prodListByRole);
            // }

            hsqlIp.append(" ORDER BY a.TUpdTm DESC ");
            hsqlGp.append(" ORDER BY a.TUpdTm DESC ");
            logger.debug("查询个单列表：" + hsqlIp.toString());
            ResultParameter rpi = super.query(hsqlIp.toString(), filter);
            logger.debug("查询团单列表：" + hsqlGp.toString());
            ResultParameter rpg = super.query(hsqlGp.toString(), filter);
            rpi.getData().addAll(rpg.getData());
            return rpi;
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
    }

    /**
     * 是否团单
     * @param plyNo 保单号
     * @return boolean
     * @throws BusinessServiceException
     */
    private boolean isGroup(String plyNo) throws BusinessServiceException {
        boolean b = false;
        try {
            List list = super.find("SELECT CGrpMrk FROM PlyBaseVO WHERE CPlyNo=?", plyNo);
            if (list != null && !list.isEmpty()) {
                if ("1".equals((String) list.get(0))) {
                    b = true;
                }
            }
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return b;
    }

    /**
     * 获取承保影像信息列表
     * @param appNo 申请单号
     * @return List<PlyImgIdxVO>
     * @throws BusinessServiceException
     * @author Yesic
     */
    public List<PlyImgIdxVO> queryImageListByPlyno(String plyno) throws BusinessServiceException {
        List<PlyImgIdxVO> list = new ArrayList<PlyImgIdxVO>();
        try {
            StringBuffer sSQL = new StringBuffer();
            sSQL.append(" FROM PlyImgIdxVO WHERE CPlyNo=? and CStatus ='0'");
            list = this.find(sSQL.toString(), plyno);
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return list;
    }
    
    /**
     * 获取所有承保影像信息列表根据保单号
     * @param plyNo 保单号
     * @return List<PlyImgIdxVO>
     * @throws BusinessServiceException
     * @author Yesic
     */
    public List<PlyImgIdxVO> queryImagesByPlyno(String plyNo, String appNo) throws BusinessServiceException {
        List<PlyImgIdxVO> list = new ArrayList<PlyImgIdxVO>();
        try {
          //申请单号	
          List appNos = super.find("SELECT CAppNo FROM PlyBaseVO WHERE CPlyNo=?", plyNo);
          Set<String> set = new HashSet<String>(appNos);
          if(appNo!=null&&!"".equals(appNo)&&!"null".equals(appNo)){
          	set.add(appNo);
          }
          List<String> lists = new ArrayList<String>(set);
	      if (lists != null && !lists.isEmpty()) {
        	for(Object appno : lists){
        		List<PlyImgIdxVO> queryImageList = queryImageList(appno.toString());
        		for(PlyImgIdxVO plyImgIdxVO : queryImageList){
        			list.add(plyImgIdxVO);
        		}
        		
        	}
	      }
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return list;
    }
    /**
     * 获取所有承保影像信息列表根据保单号(联合核保)
     * @param plyNo 保单号
     * @return List<PlyImgIdxVO>
     * @throws BusinessServiceException
     * @author Yesic
     */
    public List<PlyImgIdxVO> queryImagesByPlynos(String sy_plyNo, String jq_plyNo, String sy_appNo, String jq_appNo) throws BusinessServiceException {
    	List<PlyImgIdxVO> list = new ArrayList<PlyImgIdxVO>();
    	try {
    		//申请单号	
    		List appNos = super.find("SELECT CAppNo FROM PlyBaseVO WHERE CPlyNo in("+"'"+sy_plyNo+"'"+","+"'"+jq_plyNo+"'"+")");
    		Set<String> set = new HashSet<String>(appNos);
    		if(sy_appNo!=null&&!"".equals(sy_appNo)||jq_appNo!=null&&!"".equals(jq_appNo)){
    			set.add(sy_appNo);
    			set.add(jq_appNo);
    		}
    		List<String> lists = new ArrayList<String>(set);
    		if (lists != null && !lists.isEmpty()) {
    			for(Object appno : lists){
    				List<PlyImgIdxVO> queryImageList = queryImageList(appno.toString());
    				for(PlyImgIdxVO plyImgIdxVO : queryImageList){
    					list.add(plyImgIdxVO);
    				}
    				
    			}
    		}
    	} catch (Exception e) {
    		throw new BusinessServiceException(e);
    	}
    	return list;
    }
    
    /**
     * 获取承保影像信息列表
     * @param appNo 申请单号
     * @return List<PlyImgIdxVO>
     * @throws BusinessServiceException
     * @author Yesic
     */
    public List<PlyImgIdxVO> queryImageList(String appNo) throws BusinessServiceException {
    	List<PlyImgIdxVO> list = new ArrayList<PlyImgIdxVO>();
    	try {
    		List cEdrNo = super.find("SELECT CEdrNo FROM PlyBaseVO WHERE CAppNo=?",appNo);
    		StringBuffer sSQL = new StringBuffer();
    		sSQL.append(" FROM PlyImgIdxVO WHERE CAppNo=? and CStatus ='0'");
    		list = this.find(sSQL.toString(), appNo);
    		if(!cEdrNo.isEmpty()){
    			String edrNo = String.valueOf(cEdrNo.get(0));
    			for(PlyImgIdxVO plyImgIdxVO : list){
    				if(!"".equals(edrNo) && !"null".equals(edrNo)){
    					plyImgIdxVO.setCEdrNo(edrNo);
    				}
    			}
    		}
    	} catch (Exception e) {
    		throw new BusinessServiceException(e);
    	}
    	return list;
    }
    
    /**
     * 更新承保影像信息
     * @param appNo 申请单号
     * @return List<PlyImgIdxVO>
     * @throws BusinessServiceException
     * @author Yesic
     */
    public void updateImageTarget(String appNo) throws BusinessServiceException {
        List<PlyImgIdxVO> list = new ArrayList<PlyImgIdxVO>();
        try {
        	String sql = "UPDATE PlyImgIdxVO SET CTarget = '1' WHERE CAppNo=:CAppNo";
			HashMap mapParam = new HashMap();
			mapParam.put("CAppNo", appNo);
			this.batchUpdate(sql, mapParam);
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
    }
    /**
     * 获取承保特别约定信息列表
     * @param appNo 申请单号
     * @return List<AppFixSpecVO>
     * @throws BusinessServiceException
     * @author by pxg 2012-09-19
     */
    public List<AppFixSpecVO> queryFixSpecList(String appNo) throws BusinessServiceException {
        List<AppFixSpecVO> list = new ArrayList<AppFixSpecVO>();
        try {
            StringBuffer sSQL = new StringBuffer();
            sSQL.append(" FROM AppFixSpecVO WHERE CAppNo=?");
            list = this.find(sSQL.toString(), appNo);
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return list;
    }

    /**
     * 根据批单号查询批单主档
     * @param edrNo
     * @return
     */
    public PlyBaseVO getEdrBaseByEdrNo(String edrNo) {
        String hql = "FROM PlyBaseVO t WHERE t.CEdrNo=?";
        List<PlyBaseVO> list = this.find(hql, edrNo);
        return list.get(0);
    }

    /**
     * 更新 保单号 到影像信息
     * @param appNo
     * @param plyNo
     * @return
     * @throws BusinessServiceException
     * @author Yesic
     */
//    public boolean updatePlyNoToImgIdx(String appNo, String plyNo) throws BusinessServiceException {
//        boolean b = false;
//        try {
//            StringBuffer sSQL = new StringBuffer();
//            sSQL.append(" UPDATE PlyImgIdxVO SET CPlyNo=:CPlyNo WHERE CAppNo=:CAppNo");
//
//            HashMap paramMap = new HashMap();
//            paramMap.put("CPlyNo", plyNo);
//            paramMap.put("CAppNo", appNo);
//            this.batchUpdate(sSQL.toString(), paramMap);
//            b = true;
//        } catch (Exception e) {
//            throw new BusinessServiceException(e);
//        }
//        return b;
//    }

    /**
     * 更新 保单号 到团单信息
     * @param appNo
     * @param plyNo
     * @return
     * @throws BusinessServiceException
     * @author Yesic
     */
    public boolean updatePlyNoToGroup(Policy ply) throws BusinessServiceException {
        boolean b = false;
        try {
            HashMap paramMap = new HashMap();
            paramMap.put("CPlyNo", ply.getPlyNo());
            paramMap.put("CAppNo", ply.getAppNo());
            paramMap.put("NEdrPrjNo", ply.getBase().getNEdrPrjNo());

            String sSQL = new String(" UPDATE AppGrpMemberVO  "
                    + " SET CPlyNo=:CPlyNo, NEdrPrjNo=:NEdrPrjNo "
                    + " WHERE CAppNo=:CAppNo AND NEdrPrjNo IS NULL ");
            this.batchUpdate(sSQL, paramMap);

            sSQL = " UPDATE AppGrpCvrgVO " + " SET CPlyNo=:CPlyNo, NEdrPrjNo=:NEdrPrjNo "
                    + " WHERE CAppNo=:CAppNo AND NEdrPrjNo IS NULL ";
            this.batchUpdate(sSQL, paramMap);
            b = true;
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return b;
    }

    /**
     * 根据保单号 查询出有效最新记录的最大缴费期次
     * @param plyNo
     * @return
     */
    public Long getMaxPayTms(String plyNo) {
        String hql = "SELECT max(t2.NTms) FROM AppPayVO t2 WHERE t2.CAppNo=? ";
        List list = this.find(hql, plyNo);
        if (list != null && list.size() > 0) {
            return Long.valueOf(list.get(0).toString());
        }
        return 1L;
    }

    /**
     * 获得保单原始记录
     * @param plyNo
     * @return
     */
    public Policy getPlySnapshotBaseByPlyNo(String plyNo, Long edrPrjNo)
            throws BusinessServiceException {
        String sAppNo = "";
        try {
            // 根据保单号与批改序号为0查询原始保单
            StringBuffer sSQL = new StringBuffer();
            sSQL.append("SELECT CAppNo FROM PlyBaseVO WHERE NEdrPrjNo=? AND CPlyNo=? ");
            Object param[] = { edrPrjNo, plyNo };
            List list = super.find(sSQL.toString(), param);

            sAppNo = (String) list.get(0);
        } catch (Exception e) {
            throw new BusinessServiceException(e);
        }
        return this.getPolicyByAppNo(sAppNo);
    }
    
    /**
     * 根据保单号得到缴费状态
     * @param plyNo
     * @author PanYan
     * @date 09-10-26
     * @return
     */
    public PayConfirmInfoVO getPayStatusByPlyNo(String plyNo){
    	String hql = "from PayConfirmInfoVO c where c.CPlyNo=? order by TUdrTm desc";
    	List<PayConfirmInfoVO> list = this.find(hql,plyNo);
    	return list.get(0);
    }
    
    /**
     * 根据申请单号查询保单账户信息
     * @param plyNo
     * @author 
     * @date 09-10-26
     * @return
     */
    public PlyAcctinfoVO getPlyAcctifoByAppNo(String appNo){
    	String hql = "from PlyAcctinfoVO c where c.CAppNo=?";
    	List<PlyAcctinfoVO> list = this.find(hql,appNo);
    	if(list!= null & list.size()>0)
    	{
    		return list.get(0);
    	}
    	return null;
    }
    
    /**
     * 根据申请单号查询申请单账户信息
     * @param plyNo
     * @author 
     * @date 09-10-26
     * @return
     */
    public AppAcctinfoVO getAppAcctifoByAppNo(String appNo){
    	String hql = "from AppAcctinfoVO c where c.CAppNo=?";
    	List<AppAcctinfoVO> list = this.find(hql,appNo);
    	if(list!= null & list.size()>0)
    	{
    		return list.get(0);
    	}
    	return null;
    }
    
    
    //**************************************************************************
    /**
     * TODO 方法getPlyBaseByPlyNoSql的简要说明 <br><pre>
     * 方法getPlyBaseByPlyNoSql的详细说明 <br>
     * 在数据库中读取保单信息
     * 编写者：zhanghj
     * 创建时间：2011-1-5 下午02:19:09 </pre>
     * @param 参数类型 参数名 说明
     * @return String 说明
     * @throws 异常类型 说明
     */
    //**************************************************************************
    public String getPlyNoByAppNo(String appNo) throws DaoException,Exception{
    	String returnPlyNo=null;
		StringBuffer sql = new StringBuffer();
    	sql.append("SELECT C_PLY_NO FROM WEB_PLY_BASE WHERE  C_APP_NO=?  ");
    	SQLPara sqlPara = new SQLPara();
    	sqlPara.add(appNo);
    	RowSet rs = null;
    	rs = super.queryNativeSQL(sql.toString(), sqlPara, false);
    	if(rs.next()){
    		returnPlyNo = rs.getString("C_PLY_NO");
    	}
    	return returnPlyNo;
    }
    
    
    //**************************************************************************
    /**
     *  查询投保人对象<br><pre>
     * 方法getPlyApplicantVO的详细说明 <br>
     * 编写者：liuwei
     * 创建时间：Jan 20, 2011 6:22:39 PM </pre>
     * @param 参数类型 参数名 说明
     * @return PlyApplicantVO 说明
     * @throws 异常类型 说明
     */
    //**************************************************************************
    public PlyApplicantVO getPlyApplicantVO(String appNo){
    	String hql="from PlyApplicantVO p where p.CAppNo=? "; 	
    	List<PlyApplicantVO> plyApplicantVOList= this.find(hql,appNo);
    	if(plyApplicantVOList!= null & plyApplicantVOList.size()>0)
    	{
    		return plyApplicantVOList.get(0);
    	}
    	return null;
    }
    /**
     * 根据车牌号查最近保单的保单号产品id<br><pre>
     * 方法getLastPlyNoByCPlateNo的详细说明 <br>
     * 编写者：hjxing@isoftstone.com
     * 创建时间：xhj  2011-10-18  </pre>
     * @param 参数类型 参数名 说明
     * @return Map 说明
     * @throws DaoException 
     * @throws SQLException 
     * @throws 异常类型 说明
     */
    public ResultParameter getLastPlyNoByVhlInfo(FilterParameter filterPara) throws DaoException, SQLException{
    	ResultParameter rp = new ResultParameter();
    	String CPlateNo = ObjectUtils.toString(filterPara.getParems().get("CPlateNo"));
    	String CFrmNo = ObjectUtils.toString(filterPara.getParems().get("CFrmNo"));
    	String CEngNo = ObjectUtils.toString(filterPara.getParems().get("CEngNo"));
    	String CVin = ObjectUtils.toString(filterPara.getParems().get("CVin"));
    	String CPlyNo = ObjectUtils.toString(filterPara.getParems().get("CPlyNo"));
    	StringBuffer hql = new StringBuffer();
    	hql.append("select new map (a.CPlyNo as CPlyNo,a.CAppNo as CAppNo,a.CAppTyp as CAppTyp,a.CPlyNo as CPlyNo,a.CProdNo as CProdNo,a.CDptCde as CDptCde,a.CAmtCur as CAmtCur,c.CInsuredNme as CInsuredNme, " )
			.append("a.NAmt as NAmt,a.NPrm as NPrm,a.TInsrncBgnTm as TInsrncBgnTm,a.TInsrncEndTm as TInsrncEndTm,b.CPlateNo as CPlateNo, b.CEngNo as CEngNo,b.CFrmNo as CFrmNo,b.CModelCde as CModelCde,b.CModelNme as CModelNme,b.CVhlTyp as CVhlTyp,a.CProvince as CProvince)" )
			.append(" from PlyBaseVO a,PlyVhlVO b,PlyInsuredVO c where a.CAppNo = b.CAppNo and a.CAppNo = c.CAppNo and a.CLatestMrk='1' ");
			if(!StringUtils.isEmpty(CPlateNo)){
				hql.append( " and b.CPlateNo=':CPlateNo'");
			}
			if(!StringUtils.isEmpty(CFrmNo)){
				hql.append( " and b.CFrmNo=':CFrmNo'");
			}
			if(!StringUtils.isEmpty(CEngNo)){
				hql.append( " and b.CEngNo=':CEngNo'");
			} 
			if(!StringUtils.isEmpty(CVin)){
				hql.append( " and b.CVin=':CVin'");
			}
			if(!StringUtils.isEmpty(CPlyNo)){
				hql.append( " and a.CPlyNo=':CPlyNo'");
			}
			//关泽宇 7.1 复制录入 输入保单号查询
			hql.append( " and a.CProdNo in ('033001','030001','033011','033014','030003','030005','033012','033013')");
			rp = super.query(hql.toString(), filterPara);
    	return rp;
    	
    	 
    }
    
    
    //**************************************************************************
    /**
     * 获取订单号<br><pre>
     * 方法getTelnetOrderVO的详细说明 <br>
     * 编写者：liuwei
     * 创建时间：2012-7-10 上午10:47:28 </pre>
     * @param 参数类型 参数名 说明
     * @return String 说明
     * @throws 异常类型 说明
     */
    //**************************************************************************
    public String  getTelnetOrderVO(String cAppNo){
    	String cBookingNo="";
    	String hql ="from TelnetOrderVO where  CAppNo=?";
    	List<TelnetOrderVO> telOrderList =this.find(hql, cAppNo);
    	if(null!=telOrderList){
    		if(telOrderList.size()>=2){
    			BusinessServiceException ex = new BusinessServiceException("");
    			ex.setErrorMsg("电销业务存在多个订单号，请联系系统管理员！");
    		}else{
    			 cBookingNo = telOrderList.get(0).getCBookingNo();
    		}
    	}
    	return cBookingNo;
    }
    /**
     * 根据保单号查询原始保单的是否快速出单标志状态
     * @param CPlyNo
     * @return
     * @throws DaoException
     */
    public String getOldCQuickFlag(String CPlyNo) throws DaoException {
		List<String> resultList =null;
		String result=null;
		try{
			StringBuffer sbHQL = new StringBuffer();
			sbHQL.append("SELECT CQuickFlag FROM PlyBaseVO WHERE CAppTyp = 'A' AND CPlyNo = '" + CPlyNo + "'");
			resultList = this.find(sbHQL.toString());
			if(!CollectionUtils.isEmpty(resultList)){
				result=resultList.get(0);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new DaoException(e.getMessage());
		}
		return result;
	}
    
    /**
     * 根据驾驶员姓名，证件类型，证件号，获取其投保情况
     * 只查商业险\\\\\\\\\\\\\\\\\\\\\\\\\\
     * @throws ParseException 
     */
    public List getVhlDrv(String vhlDrvName ,String type , String no ,Date bgnTm ,Date endTm,String cProdNo) throws ParseException{
    	StringBuffer sb = new StringBuffer();
    	
    	// 处理时间+1问题
    	String TInsrncBgnTm = DateTool.dateToStr(new Date(new Date().getTime()+48*60*60*1000));
    	String oldTInsrncEndTm = DateTool.dateToStr(new Date());
    	DateUtil dateUtil = (DateUtil) SpringUtils.getSpringBean("dateUtil");
    	String TInsrncEndTm = dateUtil.DayPlusOrSubtract(oldTInsrncEndTm,1);
    	
    	sb.append(" SELECT base.CPlyNo ")
    	  .append(" FROM PlyBaseVO base,PlyVhlDrvVO vhlDrv ")
    	  .append(" Where base.CPlyNo = vhlDrv.CPlyNo AND base.CLatestMrk='1' AND vhlDrv.CLatestMrk='1' AND ")
    	  .append("       vhlDrv.CDrvNme = '"+vhlDrvName+"' AND vhlDrv.CAllowedVhlTyp = '"+type+"' AND ")
    	  .append("       vhlDrv.CDrvLcnNo = '"+no+"' AND base.CProdNo = '033001'    ")
    	  .append(" AND base.TInsrncBgnTm <= to_date('"+TInsrncBgnTm+"', 'yyyy-mm-dd hh24:mi:ss') ")
//    	  .append(" AND base.TInsrncBgnTm <= to_date('"+DateTool.dateToStr(new Date(new Date().getTime()+48*60*60*1000))+"', 'yyyy-mm-dd hh24:mi:ss') ")
    	  .append(" AND base.TInsrncEndTm >  to_date('"+TInsrncEndTm+"', 'yyyy-mm-dd hh24:mi:ss') ");
//    	  .append(" AND base.TInsrncEndTm >  to_date('"+DateTool.dateToStr(new Date())+"', 'yyyy-mm-dd hh24:mi:ss')+1 ");
    	 return this.find(sb.toString());
    }
    
    //查看该操作员是录入税控信息权限 jkfanc
    public Boolean findRoleForSendTC(String operid){
	  		int outNum = 0;
	  		Boolean isok=false;
	  		try {
	  			RowSet rs = null;
	  			String sql = "SELECT count(*) FROM web_grt_usr_role r WHERE r.c_oper_id =? AND c_opgrp_cde ='ROLE_00000389'";
	  			SQLPara para = new SQLPara();
	  			para.add(operid);
	  			PolicyDAO policyDao = (PolicyDAO) SpringUtils.getSpringBean("policyDao");
	  			rs = policyDao.queryNativeSQL(sql, para, false);
	  			if (rs.next()) {
	  				outNum = rs.getInt(1);
	  				if(outNum>=1){
	  					isok=true;
	  				}
	  			}
	  		} catch (Exception e) {
	  		}
	  		return isok;
	  	}
  //查看该操作员是录入税控信息权限 jkfanc
    public Boolean findRoleForSaveTC(String operid){
	  		int outNum = 0;
	  		Boolean isok=false;
	  		try {
	  			RowSet rs = null;
	  			String sql = "SELECT count(*) FROM web_grt_usr_role r WHERE r.c_oper_id =? AND c_opgrp_cde ='ROLE_00000388'";
	  			SQLPara para = new SQLPara();
	  			para.add(operid);
	  			PolicyDAO policyDao = (PolicyDAO) SpringUtils.getSpringBean("policyDao");
	  			rs = policyDao.queryNativeSQL(sql, para, false);
	  			if (rs.next()) {
	  				outNum = rs.getInt(1);
	  				if(outNum>=1){
	  					isok=true;
	  				}
	  			}
	  		} catch (Exception e) {
	  		}
	  		return isok;
	  	}
    
	/**
    * 获取北京身份证信息无需校验的保单
    * @author liuhuan
	* @date   2016-05-18
    * @return 保单号列表
    * @throws DaoException 
    */
   public List<String> getBJIDNoCheckConfig() throws DaoException{
   	List<String> policyNoList = new ArrayList<String>();
   	String policyNos ="";
   	RowSet rs = null;
   	String sql = "SELECT c_para_value  FROM web_cofing_para WHERE 1=1 and c_id = 'Vhl_BJ_ID_No_Check_Config'";
   	try {
   		rs = super.queryNativeSQL(sql, null, false);
   		while (rs.next()) {
   			policyNos =rs.getString(1);
   		}
   	} catch (Exception e) {
   		e.printStackTrace();
   		throw new DaoException(e);
   	}
   	if(policyNos != null && !"".equals(policyNos)){
   		policyNoList = Arrays.asList(policyNos.trim().split(";"));
   	}
   	
   	return policyNoList;
   }
    
   /**
    * 获得保单批单记录
    * @return
    */
   public ResultParameter queryAppEdrByPlyNo(FilterParameter filterPara) throws BusinessServiceException {
       ResultParameter rp = null ;
       try {
    	rp = new ResultParameter();
       	String plyNo = ObjectUtils.toString(filterPara.getParems().get("CPlyNo"));
    	   
           StringBuffer sSQL = new StringBuffer();
           sSQL.append(" SELECT DISTINCT new map( ").append(" b.NEdrPrjNo as NEdrPrjNo,").append("b.CAppNo as CAppNo,").append("b.CPlyNo as CPlyNo")
           		//.append(",b.CProdNmeCn as CProdNmeCn,")
           		.append(",b.CEdrNo as CEdrNo")
           		.append(",b.CProdNo as CProdNo ,")
           		.append(" a.CAppNme as CAppNme,").append(" a.CEmail as CEmails,").append(" a.CMobile as CMobile,")
           		.append(" a.CCertfCde as CCertfCde,").append(" a.CClntMrk as CClntMrk,").append(" a.CCertfCls as CCertfCls,")
           		.append("i.CInsuredNme as CInsuredNme,")
           		.append("v.CPlateNo as CPlateNo,").append("v.CEngNo as CEngNo,").append("v.CFrmNo as CFrmNo")
           		.append(") ").append(" from PlyBaseVO b ")
           		.append(" ,PlyApplicantVO a ")
           		.append(" ,PlyInsuredVO i  ")
           		.append(",PlyVhlVO v  ")
           		.append(" WHERE 1=1 ")
           		.append(" and b.CAppNo=a.CAppNo  ")
           		.append(" and b.CPlyNo=i.CPlyNo  ")
           		.append(" and b.CPlyNo=v.CPlyNo  ");
           		
           	 if(!StringUtils.isEmpty(plyNo)){
          	   sSQL.append( " and b.CPlyNo=':CPlyNo'");
  			}
           	 
           	 sSQL.append(" group by  b.CAppNo  ORDER BY b.NEdrPrjNo ");
           
           	rp = super.query(sSQL.toString(), filterPara );
           	
       } catch (Exception e) {
           throw new BusinessServiceException(e);
       }
       return rp;
   }
    
   /**
    * 获得保单险别停复驶相关记录
    *
    * @param plyNo
    * @return 保单主档对象
    */
   public List<PlyBaseVO> getEdrBaseByPlyNo(String plyNo) {
       try {
           String baseHql = "from PlyBaseVO b where b.CPlyNo=:CPlyNo and CAppTyp = 'E' and CEdrRsnBundleCde in ('73','74','75')  ORDER BY NEdrPrjNo "  ;
           HashMap baseParaMap = new HashMap<String, Object>();
           baseParaMap.put("CPlyNo", plyNo);
           List<PlyBaseVO> result = this.search(baseHql, baseParaMap);
           if (result != null && result.size() > 0) {
               return result;
           } else {
               return null;
           }
       } catch (DaoException e) {
           e.printStackTrace();
           return null;
       }
   }
    
}
