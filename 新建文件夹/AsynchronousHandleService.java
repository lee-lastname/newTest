package com.answern.dubbo.service;

import java.util.List;

import com.isoftstone.pcis.policy.app.payseemoney.vo.PayConfirmInfoVO;
import com.isoftstone.pcis.policy.dm.bo.Policy;


public interface AsynchronousHandleService {
	
	/**
	 * 缴费确认之后发送电子保单
	 * @throws Exception
	 */
	public void processConfirmPayInfoNew(Policy policy,List<PayConfirmInfoVO> list)throws Exception;
	

}
