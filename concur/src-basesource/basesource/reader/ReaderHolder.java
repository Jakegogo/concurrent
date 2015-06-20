package basesource.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 资源读取器持有者
 * @author frank
 */
@Component
public class ReaderHolder implements ApplicationContextAware {
	
	@SuppressWarnings("unused")
	private final static Logger logger = LoggerFactory.getLogger(ReaderHolder.class);
	
	@PostConstruct
	protected void initialize() {
		for (String name : this.applicationContext.getBeanNamesForType(ResourceReader.class)) {
			ResourceReader reader = this.applicationContext.getBean(name, ResourceReader.class);
			this.register(reader);
		}
	}
	
	private ConcurrentHashMap<String, ResourceReader> readers = 
		new ConcurrentHashMap<String, ResourceReader>();

	/**
	 * 获取指定格式的 {@link ResourceReader}
	 * @param format
	 * @return
	 */
	public ResourceReader getReader(String format) {
		return readers.get(format);
	}

	/**
	 * 注册指定的 {@link ResourceReader}
	 * @param reader
	 * @return
	 */
	public ResourceReader register(ResourceReader reader) {
		return readers.putIfAbsent(reader.getFormat(), reader);
	}
	
	// 实现 {@link ApplicationContextAware}

	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
