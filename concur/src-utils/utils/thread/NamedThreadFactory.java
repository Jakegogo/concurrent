package utils.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 可命名线程工厂
 *
 * @author jake
 * @date 2014-8-2-下午5:14:10
 */
public class NamedThreadFactory implements ThreadFactory {

	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;

	public NamedThreadFactory(ThreadGroup group, String name) {
		this.group = group;
		namePrefix = group.getName() + ":" + name;
	}

	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix
				+ threadNumber.getAndIncrement(), 0);
		return t;
	}

}