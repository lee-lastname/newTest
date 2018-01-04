package com.answern.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.ax.dubbo.cmq.Account;
import com.ax.dubbo.cmq.CMQServerException;
import com.ax.dubbo.cmq.CmqConnConstants;
import com.ax.dubbo.cmq.Message;
import com.ax.dubbo.cmq.Queue;
import com.ax.dubbo.service.AsynchronousHandleService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.isoftstone.fwk.util.SpringUtils;
import com.isoftstone.pcis.policy.app.payseemoney.vo.PayConfirmInfoVO;
import com.isoftstone.pcis.policy.app.platform.utils.VoHelper;
import com.isoftstone.pcis.policy.app.quickapp.action.QuickAppBaseBizAction;
import com.isoftstone.pcis.policy.dm.bo.Policy;
import com.isoftstone.pcis.policy.dm.bo.PolicyApplication;

/**
 * 核保消息监听器
 * 
 * @date 2017年8月30日
 */
public class MessageListenerUnderwriting {
	private int interval = 30; // 消息接收间隔
	private int pollingWaitSeconds = 30;// 请求最长的Polling等待时间10s
	private MessageHandler messageHandler = null;
	private Queue queue = null;
	private Account account = null;
	

	/**
	 * 创建一个消息监听器
	 * @param queue 队列
	 * @param messageHandler 消息处理器
	 */
	public MessageListenerUnderwriting(Account account, MessageHandler messageHandler) {
		this.account = account;
		this.messageHandler = messageHandler;
	}
	
	/**
	 * 启动监听器
	 */
	public String start() {
		Runnable runnable=new Runnable() {
			
			public void run() {
				System.out.println("开始接收消息...");
				try {
					
					Queue queue = account.getQueue(CmqConnConstants.QUEUENAME);
//					Queue queue = account.getQueue("underwriting");
					
					Message msg = queue.receiveMessage(pollingWaitSeconds);
//					queue.deleteMessage(msg.receiptHandle); //处理成功，删除消息
//					Date start = new Date();
					
					System.out.println("收到消息："+msg.msgBody);
					//net.sf.json.JSONObject obj= new net.sf.json.JSONObject();
					//取出mq数据
					String mesBody = msg.msgBody;
					
					JSONObject json = JSON.parseObject(mesBody);
					
					//由于mq数据里面分两个大类，还是个string的json串。需要先将String转成map。从map里面取出两个大类。然后转成bean
					boolean containsKey = json.containsKey(CmqConnConstants.JQAPPMAPS);
					
					underwritingMethod(json, mesBody);
					
					//queue.deleteMessage(msg.receiptHandle); //处理成功，删除消息
/*					Date end = new Date();
					System.out.println(start.getTime());
					System.out.println("waster time:"+ (end.getTime()));
					System.out.println("收到消息有效时间："+msg.nextVisibleTime);*/


				} catch(CMQServerException e){
   					System.out.println(e.getErrorMessage());
				}catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("接收消息完毕");
			}

			

		};
		//启动一个单线程任务调度器（3秒后开始执行调度，每interval秒执行一次）
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();  
	    // 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间  
	    service.scheduleAtFixedRate(runnable, 3, interval, TimeUnit.SECONDS);
		return null;
	}
	
	
	public void underwritingMethod(JSONObject json, String mesBody) {
		
		JSONObject jqAppMapJson =  (JSONObject) json.get(CmqConnConstants.JQAPPMAPS);
		
		//创建保单大对象
		PolicyApplication policyApplication = new PolicyApplication();
		//获取 composition map
		JSONObject compositionJson =  (JSONObject) jqAppMapJson.get(CmqConnConstants.COMPOSITION_JSON);
		Map<String, List> composition = JSON.parseObject(compositionJson.toString(), HashMap.class);
		Map<String, List> mapComposition = convertEntityTypeForMap(composition);
		policyApplication.setComposition(mapComposition);
		
		//获取 todelete map
		JSONObject toDeleteJson =  (JSONObject) jqAppMapJson.get(CmqConnConstants.TO_DELETE_JSON);
		Map<String, List> toDelete = JSON.parseObject(toDeleteJson.toString(), HashMap.class);
		Map<String, List> mapDelete = convertEntityTypeForMap(toDelete);
		policyApplication.setToDelete(mapDelete);

		//调用dubbo传首付，再保等后续流程
		try {
			QuickAppBaseBizAction quickAppBaseBizAction = new QuickAppBaseBizAction();
			quickAppBaseBizAction.initVhlOrderAndPolicy(policyApplication, null, "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 将map里的object对象转换为具体实体类型
	 * @param toDelete
	 * @return
	 */
	private Map<String, List> convertEntityTypeForMap(Map<String, List> map) {
		Map<String, List> mapRturn = new HashMap<>();
		for (Map.Entry<String, List> entry : map.entrySet()) {
			String key = entry.getKey();
			JSONArray value = (JSONArray) entry.getValue();
			Class appVoClass = VoHelper.getAppVoClass(key);
			List abstractBaseVOs = JSON.parseArray(value.toJSONString(), appVoClass);
			mapRturn.put(key, abstractBaseVOs);
		}
		return mapRturn;
	}
	

	public int getInterval() {
		return interval;
	}

	/**
	 * 设置从MQ取消息的间隔（秒）默认30s
	 * @param interval
	 */
	public void setInterval(int interval) {
		this.interval = interval;
	}

	public int getPollingWaitSeconds() {
		return pollingWaitSeconds;
	}
	/**
	 * 设置从MQ读取消息时如果MQ没有消息则线程等待秒数，默认10s
	 * @param pollingWaitSeconds
	 */
	public void setPollingWaitSeconds(int pollingWaitSeconds) {
		this.pollingWaitSeconds = pollingWaitSeconds;
	}

	public MessageHandler getMessageHandler() {
		return messageHandler;
	}

	public void setMessageHandler(MessageHandler messageHandler) {
		this.messageHandler = messageHandler;
	}

	public Queue getQueue() {
		return queue;
	}

	public void setQueue(Queue queue) {
		this.queue = queue;
	}
	
	

}
