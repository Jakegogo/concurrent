/**
 * 
 */
package utils.thread;

import utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程工具类
 * @author fansth
 *
 */
public abstract class ThreadUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(ThreadUtils.class);
	
	/**
	 * 关闭线程池
	 * @param threadPool 需要关闭的线程池
	 * @param shutdownNow true-立即关闭放弃当前执行的任务  false-等待所提交的任务都完成后再最初
	 */
	public static void shundownThreadPool(ExecutorService threadPool, boolean shutdownNow){
		if(shutdownNow){
			try {
				threadPool.shutdownNow();
			}catch (Exception e) {
				if(!(e instanceof InterruptedException)){
					logger.error("关闭线程池时出错!", e);
				}
			}
		} else {
			threadPool.shutdown();
			boolean taskComplete = false;
			for(int i = 0; i < 30; i++){//最多等待30秒
				
				logger.error("正在第 [{}] 次尝试关闭线程池!", i+1);
				
				try {
					taskComplete = threadPool.awaitTermination(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					if(!taskComplete){
						continue;
					}
				}
				
				if(taskComplete){
					break;
				} else {
					if(threadPool instanceof ThreadPoolExecutor){
						Queue<?> taskQueue = getTaskQueue((ThreadPoolExecutor)threadPool);
						if(taskQueue != null){
							logger.error("当前正在关闭的线程池尚有 [{}] 个任务排队等待处理!", taskQueue.size());
						}
					}
					
				}
			}
			
			if(!taskComplete){
				logger.error("线程池非正常退出!");
			} else {
				logger.error("线程池正常退出!");
			}
		}
	}
	
	
	
	/**
	 * 获取线程池的任务队列
	 * @param threadPoolExecutor
	 * @return
	 */
	private static BlockingQueue<?> getTaskQueue(ThreadPoolExecutor threadPoolExecutor){
		BlockingQueue<?> queue = null;
		try {
			queue = threadPoolExecutor.getQueue();
		} catch (Exception e1) {
			try {
				Field field = ThreadPoolExecutor.class.getDeclaredField("workQueue");
				field.setAccessible(true);
				queue = (BlockingQueue<?>)field.get(threadPoolExecutor);
			} catch (Exception e2) {
			}
		}
		return queue;
	}
	
	/**
	 * 获取线程池的任务队列
	 * @param threadPoolExecutor
	 * @return
	 */
	private static BlockingQueue<?> getTaskQueue(utils.thread.ThreadPoolExecutor threadPoolExecutor){
		BlockingQueue<?> queue = null;
		try {
			queue = threadPoolExecutor.getQueue();
		} catch (Exception e1) {
			try {
				Field field = utils.thread.ThreadPoolExecutor.class.getDeclaredField("workQueue");
				field.setAccessible(true);
				queue = (BlockingQueue<?>)field.get(threadPoolExecutor);
			} catch (Exception e2) {
			}
		}
		return queue;
	}
	
	/**
	 * dump出线程池情况
	 * @param poolname
	 * @param threadPool
	 * @return
	 */
	public static String dumpThreadPool(String poolname , ExecutorService threadPool){
		
		if(threadPool instanceof ThreadPoolExecutor){
			ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)threadPool;
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("线程池名称" , poolname);
			
			map.put("当前队列上排队的任务数量", "(无法获取)");
			BlockingQueue<?> queue = getTaskQueue(threadPoolExecutor);
			if(queue != null){
				map.put("当前队列上排队的任务数量", queue.size());
			}
			
			map.put("当前池内总的线程数量", threadPoolExecutor.getPoolSize());
			map.put("当前正在执行任务的线程数", threadPoolExecutor.getActiveCount()); 
			map.put("历史执行过的任务数量", threadPoolExecutor.getCompletedTaskCount()); 
			map.put("配置的核心大小", threadPoolExecutor.getCorePoolSize()); 
			map.put("配置的最大线程数量", threadPoolExecutor.getMaximumPoolSize()); 
			map.put("历史最大峰值线程数量", threadPoolExecutor.getLargestPoolSize()); 
			return JsonUtils.object2JsonString(map);
		} else if(threadPool instanceof utils.thread.ThreadPoolExecutor){
			utils.thread.ThreadPoolExecutor threadPoolExecutor = (utils.thread.ThreadPoolExecutor)threadPool;
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("线程池名称" , poolname);
			
			map.put("当前队列上排队的任务数量", "(无法获取)");
			BlockingQueue<?> queue = getTaskQueue(threadPoolExecutor);
			if(queue != null){
				map.put("当前队列上排队的任务数量", queue.size());
			}
			
			map.put("当前池内总的线程数量", threadPoolExecutor.getPoolSize());
			map.put("当前正在执行任务的线程数", threadPoolExecutor.getActiveCount()); 
			map.put("历史执行过的任务数量", threadPoolExecutor.getCompletedTaskCount()); 
			map.put("配置的核心大小", threadPoolExecutor.getCorePoolSize()); 
			map.put("配置的最大线程数量", threadPoolExecutor.getMaximumPoolSize()); 
			map.put("历史最大峰值线程数量", threadPoolExecutor.getLargestPoolSize()); 
			return JsonUtils.object2JsonString(map);
		}
		
		return "无法内省的线程池 [" + poolname + "]";
		
	}
	
	
	
}
