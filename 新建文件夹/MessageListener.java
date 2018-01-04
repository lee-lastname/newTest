package com.answern.listener;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.answern.cmq.Account;
import com.answern.cmq.CMQServerException;
import com.answern.cmq.CmqConnConstants;
import com.answern.cmq.Message;
import com.answern.cmq.Queue;
import com.answern.mongo.dao.CmqProcessErrorTool;
import com.answern.mongo.entity.CmqProcessErrorVo;
import com.alibaba.fastjson.JSONObject;
import com.isoftstone.pcis.policy.app.payseemoney.hh.service.PayseemoneyService;
import com.isoftstone.pcis.policy.app.quickapp.action.QuickAppBaseBizAction;

/**
 * 消息监听器
 * 
 * @date 2017年7月4日
 */
public class MessageListener implements Runnable, Serializable {
	private Logger logger = Logger.getLogger(this.getClass());
	private int interval = 30; // 消息接收间隔
	private int pollingWaitSeconds = 30;// 请求最长的Polling等待时间30s
	private MessageHandler messageHandler = null;
	private Account account = null;

	/**
	 * 创建一个消息监听器
	 * 
	 * @param queue
	 *            队列
	 * @param messageHandler
	 *            消息处理器
	 */
	public MessageListener(Account account, MessageHandler messageHandler) {
		this.account = account;
		this.messageHandler = messageHandler;
	}

	/**
	 * 启动监听器
	 */
	public String start() {
		Runnable runnable = new Runnable() {

			public void run() {
				Date startDate = new Date();
				try {
					logger.info("开始接收消息...");
					
					//获取队列
					Queue queue = account.getQueue(CmqConnConstants.QUEUENAME);
					
					//接受队列消息
					Message msg = queue.receiveMessage(pollingWaitSeconds);
					
					// 取出mq数据
					String mesBody = msg.msgBody;
					
					//删除消息
					queue.deleteMessage(msg.receiptHandle);
					

					// 由于mq数据里面分两个大类，还是个string的json串。需要先将String转成map。从map里面取出两个大类。然后转成bean
					JSONObject json = JSON.parseObject(mesBody);

					// 通过判断json里的key来判断执行哪个方法
					if (json.containsKey(CmqConnConstants.CMQ_ORDER_UNDERWRITING)) {//核保生成订单
						insertOrderAndPolicy(json);
					} else {
						updateOrderAndPolicy(json);
					}

				} catch (CMQServerException e) {
					logger.error("CMQ消息错误：" + e);
				} catch (Exception e) {
					logger.error(e);
				}
				Date endDate = new Date();
				logger.info("车承保异步订单系统_" + this.getClass() + "_start()_耗时：" + (endDate.getTime()-startDate.getTime()));
			}

		};
		// 启动一个单线程任务调度器（3秒后开始执行调度，每interval秒执行一次）
		ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		// 第二个参数为首次执行的延时时间，第三个参数为定时执行的间隔时间
		service.scheduleAtFixedRate(runnable, 3, interval, TimeUnit.SECONDS);
		return null;
	}
	
	
	/**
	 * 插入订单方法
	 * @param json
	 */
	private void insertOrderAndPolicy(JSONObject json) {
		//捕获接口异常，用于存消息到数据库
		try {
			//插入订单方法
			QuickAppBaseBizAction quickAppBaseBizAction = new QuickAppBaseBizAction();
			quickAppBaseBizAction.initVhlOrderAndPolicy(json, "");
		} catch (Exception e) {
			logger.error("调用插入订单接口错误："+e);
			insertMessage(json);
		}
	}

	
	
	
	/**
	 * 更新订单方法
	 * @param json
	 */
	private void updateOrderAndPolicy(JSONObject json) {
		try {
			//修改订单方法
			PayseemoneyService payseemoneyService = new PayseemoneyService();
			payseemoneyService.writePolicyToUpdateOrder(json);
		} catch (Exception e) {
			logger.error("调用修改订单接口错误："+e);
			//捕获修改订单接口异常，用于存消息到数据库
			insertMessage(json);
		}
	}
	
	/**
	 * 插入消息方法
	 * @param vo
	 * @throws Exception
	 */
	private void insertMessage(JSONObject json) {
		try {
			CmqProcessErrorVo vo = new CmqProcessErrorVo();
			vo.setMessage(json.toJSONString());
			CmqProcessErrorTool.insert(vo);
		} catch (Exception e) {
			logger.error(e);
			logger.info("错误消息插入异常：" + json.toJSONString());
		}
	}


	public int getInterval() {
		return interval;
	}

	/**
	 * 设置从MQ取消息的间隔（秒）默认30s
	 * 
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
	 * 
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

}
