package com.isoftstone.pcis.policy.app.payseemoney.task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import com.isoftstone.fwk.dao.DaoFactory;
import com.isoftstone.fwk.util.SpringUtils;
import com.isoftstone.pcis.policy.app.payseemoney.constants.PayseemoneyConst;
import com.isoftstone.pcis.policy.app.payseemoney.dm.PayseemoneyServiceDao;
import com.isoftstone.pcis.policy.app.payseemoney.vo.PayConfirmBatchInfoVO;
import com.isoftstone.pcis.policy.app.payseemoney.vo.PayConfirmInfoVO;

/**
 * 批量合并缴费定时任务
 * @author lizhen
 *
 */
public class PayConfirmBatchJob implements StatefulJob {
    
    // 日志对象实例
    private Logger logger = Logger.getLogger(this.getClass());
    
//    private PayseemoneyServiceDao payseemoneyServiceDao;//dao对象实例
   
    /**
     * 定时任务执行方法
     * 
     * @param {@link JobExecutionContext} context Quartz调度上下文
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

           this.chequeTransferTPassBatch(context);
    }
    
    
    /**
     * 异步批量处理转账确认 lizhen
     * @param {@link JobExecutionContext} context Quartz调度上下文
     */
    public void chequeTransferTPassBatch(JobExecutionContext context) {
        List<PayConfirmBatchInfoVO> confirmBatchInfoVOs = null;
        // 获取全局变量的id
        List<String> jobDatalist = (List<String>) context.getJobDetail().getJobDataMap().get(PayseemoneyConst.JOBDATALIST);
        JobDataMap jbMap = context.getJobDetail().getJobDataMap();
        
        try {
            PayseemoneyServiceDao payseemoneyServiceDao = (PayseemoneyServiceDao) SpringUtils.getSpringBean("payseemoneyServiceDao");
            // 按照批次查询状态为未进行的批量缴费确认数据
            confirmBatchInfoVOs = payseemoneyServiceDao.getFeeBatchInfoByCBatchNo();
            // 如果全局变量中包含id就排除该id查询，否则直接查询
            if (!confirmBatchInfoVOs.isEmpty() && !StringUtils.isEmpty(confirmBatchInfoVOs.get(0).getCBatchNo())
                    && null != jobDatalist && jobDatalist.contains(confirmBatchInfoVOs.get(0).getCBatchNo())) {
                confirmBatchInfoVOs = payseemoneyServiceDao.getFeeBatchInfoExcludeCBatchNo(jobDatalist.toArray(new String[jobDatalist.size()]));
            }

            if (!confirmBatchInfoVOs.isEmpty()) {
                // 将id存入全部变量
                if (null == jobDatalist) {
                    jobDatalist = new ArrayList<String>();
                }
                jobDatalist.add(confirmBatchInfoVOs.get(0).getCBatchNo());
                jbMap.put(PayseemoneyConst.JOBDATALIST, jobDatalist);
                context.getJobDetail().setJobDataMap(jbMap);
                // 开启线程处理
                ThreadPoolExecutor threadPool = new ThreadPoolExecutor(2, 4, 3,
                        TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3),
                        new ThreadPoolExecutor.DiscardOldestPolicy());
                for (PayConfirmBatchInfoVO batchInfoVO : confirmBatchInfoVOs) {
                    DaoFactory.beginTransaction();// 开启事务
                    // 更新数据的状态为进行中
                    batchInfoVO.setCPayStatus("1");
                    payseemoneyServiceDao.update(batchInfoVO);
                    DaoFactory.commitTransaction();
                    //执行线程
                    final PayConfirmInfoVO confirmInfoVO = new PayConfirmInfoVO();
                    confirmInfoVO.setCUniqueNo(batchInfoVO.getCUniqueNo());
                    confirmInfoVO.setCBizConsultNo(batchInfoVO.getCBizConsultNo());
                    confirmInfoVO.setCPaySequence(batchInfoVO.getCPaySequence());
                    confirmInfoVO.setCBankAccounts(batchInfoVO.getCBankAccounts());
                    confirmInfoVO.setCBankCde(batchInfoVO.getCBankCde());
                    confirmInfoVO.setCCheckOpn(batchInfoVO.getCCheckOpn());
                    threadPool.execute(new ThreadPoolTask(confirmInfoVO, batchInfoVO));
                }
                // 处理成功，删除全局变量id
                jobDatalist.remove(confirmBatchInfoVOs.get(0).getCBatchNo());
                jbMap.put(PayseemoneyConst.JOBDATALIST, jobDatalist);
                context.getJobDetail().setJobDataMap(jbMap);
            }
        } catch (Exception e) {
            logger.error(PayseemoneyConst.PROJECTNAME + "_" + this.getClass()
                    + "_chequeTransferTPassBatch()_异常：" + e);
            // 处理失败，删除全局变量id
            if (!confirmBatchInfoVOs.isEmpty()) {
                jobDatalist.remove(confirmBatchInfoVOs.get(0).getCBatchNo());
                jbMap.put(PayseemoneyConst.JOBDATALIST, jobDatalist);
                context.getJobDetail().setJobDataMap(jbMap);
            }
        }
    }
}
