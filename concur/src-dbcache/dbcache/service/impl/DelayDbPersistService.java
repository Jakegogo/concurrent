package dbcache.service.impl;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dbcache.model.PersistAction;
import dbcache.service.DbAccessService;
import dbcache.service.DbPersistService;
import dbcache.service.DbRuleService;


/**
 * 延时入库实现类
 * <br/>单线程执行入库
 * @author Jake
 * @date 2014年8月13日上午12:31:06
 */
@Component("delayDbPersistService")
public class DelayDbPersistService implements DbPersistService {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(DelayDbPersistService.class);

	/**
	 * 更改实体队列
	 */
	private final ConcurrentLinkedQueue<UpdateAction> updateQueue1 = new ConcurrentLinkedQueue<UpdateAction>();

	/**
	 * 更改实体队列
	 */
	private final ConcurrentLinkedQueue<UpdateAction> updateQueue2 = new ConcurrentLinkedQueue<UpdateAction>();

	/**
	 * 正在Dump的队列
	 */
	private final AtomicReference<ConcurrentLinkedQueue<UpdateAction>> operattingQueue = new AtomicReference<ConcurrentLinkedQueue<UpdateAction>>(updateQueue1);

	/**
	 * 当前延迟更新的动作
	 */
	private volatile UpdateAction currentDelayUpdateAction;


	@Autowired
	private DbRuleService dbRuleService;


	@Autowired
	private DbAccessService dbAccessService;


	/**
	 * 延迟更新操作
	 * @author Jake
	 * @date 2014年9月17日上午12:38:21
	 */
	static class UpdateAction {

		PersistAction persistAction;

		long createTime = System.currentTimeMillis();

		public UpdateAction(PersistAction persistAction) {
			this.persistAction = persistAction;
		}

		public static UpdateAction valueOf(PersistAction persistAction) {
			return new UpdateAction(persistAction);
		}

	}

	/**
	 * 获取当前的更新队列
	 * @return
	 */
	private ConcurrentLinkedQueue<UpdateAction> getUpdateQueue() {
		return operattingQueue.get();
	}

	/**
	 * 获取Dump的更新队列
	 * @return
	 */
	private ConcurrentLinkedQueue<UpdateAction> getDumpQueue() {
		return operattingQueue.get() == updateQueue1?updateQueue2:updateQueue1;
	}


	@Override
	public void init() {

		//初始化延时入库检测线程
		final long delayWaitTimmer = dbRuleService.getDelayWaitTimmer();//延迟入库时间(毫秒)

		final long delayCheckTimmer = 1000;//延迟入库队列检测时间间隔(毫秒)

		// 初始化入库线程
		new Thread() {

			@Override
			public void run() {

				//循环定时检测入库,失败自动进入重试
				while(true) {

					UpdateAction updateAction = getDumpQueue().poll();
					try {

						long timeDiff = 0l;
						do {

							if(updateAction == null) {
								//等待下一个检测时间
								Thread.sleep(delayCheckTimmer);

								//切换更新队列
								operattingQueue.set(getDumpQueue());

							} else {

								timeDiff = System.currentTimeMillis() - updateAction.createTime;

								//未到延迟入库时间
								if(timeDiff < delayWaitTimmer) {

									//切换更新队列
									operattingQueue.set(getDumpQueue());

									currentDelayUpdateAction = updateAction;

									//等待
									Thread.sleep(delayWaitTimmer - timeDiff);
								}

								//执行入库
								PersistAction persistAction = updateAction.persistAction;
								if(persistAction.valid()) {
									persistAction.run();
								}
							}

							//获取下一个有效的操作元素
							updateAction = getDumpQueue().poll();
							while(!updateAction.persistAction.valid()) {
								updateAction = getDumpQueue().poll();
							}

						} while(true);

					} catch(Exception e) {
						e.printStackTrace();

						if(updateAction != null && updateAction.persistAction != null) {
							logger.error("执行入库时产生异常! 如果是主键冲突异常可忽略!" + updateAction.persistAction.getPersistInfo(), e);
						}
						//等待下一个检测时间重试入库
						try {
							Thread.sleep(delayCheckTimmer);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}.start();

	}


	@Override
	public void handlerPersist(PersistAction persistAction) {
		getUpdateQueue().add(UpdateAction.valueOf(persistAction));
	}


	@Override
	public void awaitTermination() {
		//刷新所有延时入库的实体到库中
		this.flushAllEntity();
	}


	/**
	 * 持久化所有实体
	 */
	public void flushAllEntity() {
		//入库延迟队列中的实体
		UpdateAction updateAction = this.updateQueue1.poll();
		while (updateAction != null) {
			//执行入库
			updateAction.persistAction.run();
			updateAction = this.updateQueue1.poll();
		}

		updateAction = this.updateQueue2.poll();
		while (updateAction != null) {
			//执行入库
			updateAction.persistAction.run();
			updateAction = this.updateQueue2.poll();
		}

		//入库正在延迟处理的实体
		if(currentDelayUpdateAction != null) {
			currentDelayUpdateAction.persistAction.run();
		}
	}



	@Override
	public void logHadNotPersistEntity() {
		UpdateAction updateAction = null;
		for (Iterator<UpdateAction> it = this.updateQueue1.iterator(); it.hasNext();) {
			updateAction = it.next();
			String persistInfo = updateAction.persistAction.getPersistInfo();
			if(!StringUtils.isBlank(persistInfo)) {
				logger.error("检测到可能未入库对象! " + persistInfo);
			}
		}

		for (Iterator<UpdateAction> it = this.updateQueue2.iterator(); it.hasNext();) {
			updateAction = it.next();
			String persistInfo = updateAction.persistAction.getPersistInfo();
			if(!StringUtils.isBlank(persistInfo)) {
				logger.error("检测到可能未入库对象! " + persistInfo);
			}
		}
	}


	@Override
	public ExecutorService getThreadPool() {
		return null;
	}


}
