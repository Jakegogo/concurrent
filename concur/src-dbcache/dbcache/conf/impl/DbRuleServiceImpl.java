package dbcache.conf.impl;

import dbcache.conf.DbRuleService;
import dbcache.pkey.IdGenerator;
import dbcache.pkey.LongGenerator;
import dbcache.pkey.ServerEntityIdRule;
import dbcache.IEntity;
import dbcache.dbaccess.DbAccessService;
import dbcache.support.jdbc.ModelInfo;
import utils.reflect.GenericsUtils;
import utils.reflect.ReflectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.persistence.Entity;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

import static dbcache.conf.CfgConstants.*;

/**
 * 数据库规则服务接口实现类
 * 优先使用properties作为配置的值,其次使用spring配置文件,然后使用默认值
 * @author jake
 * @date 2014-8-1-下午9:39:17
 */
@Component
public class DbRuleServiceImpl implements DbRuleService {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(DbRuleServiceImpl.class);


	@Autowired
	private ApplicationContext applicationContext;

	/**
	 * 数据库存储服务
	 */
	@Autowired
	@Qualifier("jdbcDbAccessServiceImpl")
	private DbAccessService dbAccessService;

	/**
	 * 入库线程池大小
	 */
	@Autowired(required = false)
	@Qualifier("dbPoolSize")
	private int dbPoolSize;

	/**
	 * 实体缓存数量限制
	 */
	@Autowired(required = false)
	@Qualifier("entityCacheSize")
	private int entityCacheSize;

	/**
	 * 延迟入库时间
	 */
	@Autowired(required = false)
	@Qualifier("delayWaitTimmer")
	private long delayWaitTimmer;

	/**
	 * 实体扫描包
	 */
	@Autowired(required = false)
	@Qualifier("entityPackages")
	private String entityPackages = "dbcache";

	/**
	 * 配置文件位置
	 */
	@Autowired(required = false)
	@Qualifier("dbCachedCfgLocation")
	private String location = "classpath:ServerCfg.properties";

	/**
	 * 服标识ID列表
	 */
	private List<Integer> serverIdList;
	
	/**
	 * 自增id的位数
	 */
	private static final int INCREMENT_PART = 10;

	/**
	 * 默认延迟入库时间
	 */
	private static final long DEFAULT_DELAY_WAITTIMMER = 10000;

	/**
	 * 缺省实体缓存最大容量
	 */
	private static final int DEFAULT_MAX_CAPACITY_OF_ENTITY_CACHE = 1000000;

	/**
	 * 服标识部分ID基础值
	 */
	private final int ID_BASE_VALUE_OF_SERVER = 10000;

	/**
	 * 服标识部分ID最大值
	 */
	private final int ID_MAX_VALUE_OF_SERVER = 99999;

	/**
	 * 服标识最大值
	 */
	private final int SERVER_ID_MAX_VALUE = ID_MAX_VALUE_OF_SERVER - ID_BASE_VALUE_OF_SERVER;

	/**
	 * 服标识最小值
	 */
	private final int SERVER_ID_MIN_VALUE = 0;

	/**
	 * 自增部分的ID最大值(10个9)
	 */
	private final long ID_MAX_VALUE_OF_AUTOINCR = 9999999999L;
	
	/**
	 * 自增部分的ID最小值
	 */
	private final long ID_MIN_VALUE_OF_AUTOINCR = 0L;

	/**
	 * 自增部分的ID最小值补齐字符串
	 */
	private final String STR_VALUE_OF_AUTOINCR_ID_MIN_VALUE =
			String.format("%0" + INCREMENT_PART + "d", ID_MIN_VALUE_OF_AUTOINCR);

	/**
	 * 玩家id长度
	 */
	private final int MAX_LENGTH_OF_USER_ID = 15;

	/**
	 * 最小服Id
	 */
	private int minServerId = Integer.MAX_VALUE;
	
	/**
	 * 最大服Id
	 */
	private int maxServerId = Integer.MIN_VALUE;
	

	@PostConstruct
	public void init() {

		// 加载配置文件
		Resource resource = this.applicationContext.getResource(location);
		Properties properties = new Properties();
		try {
			properties.load(resource.getInputStream());
		} catch (IOException e) {
			FormattingTuple message = MessageFormatter.format(
					"DbCached 资源[{}]加载失败!", location);
			logger.error(message.getMessage(), e);
			throw new RuntimeException(message.getMessage(), e);
		}


		//初始化服ID列表
		if (properties.containsKey(KEY_SERVER_ID_SET)) {
			this.initServerIdSet(properties.getProperty(KEY_SERVER_ID_SET));
		}

		if (this.serverIdList == null || this.serverIdList.size() < 1) {
			FormattingTuple message = MessageFormatter.format(
					"DbCached [{}] 配置项 '服务器ID标识集合[{}]' 配置错误!", location, KEY_SERVER_ID_SET);
			logger.error(message.getMessage());
			throw new IllegalArgumentException(message.getMessage());
		}


		//入库线程池容量
		int dbPoolSize = Runtime.getRuntime().availableProcessors();
		try {
			dbPoolSize = Integer.parseInt(properties.getProperty(KEY_DB_POOL_CAPACITY));
		} catch (Exception ex) {
			logger.error("转换'{}'失败， 使用缺省值", KEY_DB_POOL_CAPACITY);
		}
		this.dbPoolSize = this.dbPoolSize > 0 ? this.dbPoolSize : dbPoolSize;


		//实体缓存最大容量
		int entityCacheSize = DEFAULT_MAX_CAPACITY_OF_ENTITY_CACHE;
		try {
			entityCacheSize = Integer.parseInt(properties.getProperty(MAX_QUEUE_SIZE_BEFORE_PERSIST));
		} catch (Exception ex) {
			logger.error("转换'{}'失败， 使用缺省值", MAX_QUEUE_SIZE_BEFORE_PERSIST);
		}
		this.entityCacheSize = this.entityCacheSize > 0? this.entityCacheSize : entityCacheSize;


		//实体缓存最大容量
		long delayWaitTimmer = DEFAULT_DELAY_WAITTIMMER;
		try {
			delayWaitTimmer = Integer.parseInt(properties.getProperty(DELAY_WAITTIMMER));
		} catch (Exception ex) {
			logger.error("转换'{}'失败， 使用缺省值", MAX_QUEUE_SIZE_BEFORE_PERSIST);
		}
		this.delayWaitTimmer = this.delayWaitTimmer > 0? this.delayWaitTimmer : delayWaitTimmer;

	}


	/**
	 * 初始化服标识集合
	 * @param serverIdSet String
	 */
	private void initServerIdSet(String serverIdSet) {
		if (serverIdSet == null || serverIdSet.trim().length() == 0) {
			return;
        }

		String[] serverIdArray = serverIdSet.trim().split(SPLIT);
		if (serverIdArray.length == 0) {
			return;
		}

		this.serverIdList = new ArrayList<Integer>(serverIdArray.length);

		try {
			for (String sid: serverIdArray) {
				int serverId = Integer.parseInt(sid.trim());
				//非法的服标识
				if (!ServerEntityIdRule.isLegalServerId(serverId)) {
					this.serverIdList = null;
					logger.error("服务器ID标识超出范围：{}", serverId);

					break;
				}

				if (serverId < minServerId) {
					minServerId = serverId;
				}

				if (serverId > maxServerId) {
					maxServerId = serverId;
				}

				if (!serverIdList.contains(serverId)) {
					serverIdList.add(serverId);
				}
			}

		} catch (Exception ex) {
			this.serverIdList = null;
			logger.error("DbCached 转换服务器ID标识集合错误： {}", ex.getMessage());
		}

	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void initIdGenerators(Class<? extends IEntity> clz, CacheConfig cacheConfig) {

		if (!clz.isAnnotationPresent(Entity.class)) {
			return;
		}

		if (serverIdList == null || serverIdList.size() == 0) { // 没有配置服标识
			cacheConfig.setIdGenerators(Collections.emptyMap());
			cacheConfig.setDefaultIdGenerator(new LongGenerator());
			return;
		}

		
		//获取可用主键类型
		Class<?> idType = GenericsUtils.getSuperClassGenricType(clz, 0);

		if (idType == null || idType != Long.class) {
			Field[] fields = ReflectionUtility.getDeclaredFieldsWith(clz, javax.persistence.Id.class);

			if(fields.length == 0) {
				return;
			}
			ReflectionUtils.makeAccessible(fields[0]);
			if (fields[0].getType() != Long.class) {
				return;
			}
		}


		//初始化主键Id生成器
		Map<Integer, IdGenerator<?>> idGenerators = new IdentityHashMap<Integer, IdGenerator<?>>();

		List<Integer> serverIdList = getServerIdList();

		for (int serverId: serverIdList) {

			long minValue = ServerEntityIdRule.getMinValueOfEntityId(serverId);
			long maxValue = ServerEntityIdRule.getMaxValueOfEntityId(serverId);

			//当前最大id
			long currMaxId = minValue;
			Object resultId = this.dbAccessService.loadMaxId(clz, minValue, maxValue);
			if (resultId != null) {
				currMaxId = (Long) resultId;
			}

			LongGenerator idGenerator = new LongGenerator(currMaxId);
			idGenerators.put(serverId, idGenerator);

			if (logger.isInfoEnabled()) {
				logger.info("服{}： {} 的当前自动增值ID：{}", new Object[] {serverId, clz.getName(), currMaxId});
			}
		}

		// 设置主键id生成器
		cacheConfig.setIdGenerators(idGenerators);

		// 默认的服Id
		Integer defaultSereverId = getDefaultServerId();
		IdGenerator<?> defaultIdGenerator = idGenerators.get(defaultSereverId);
		cacheConfig.setDefaultIdGenerator(defaultIdGenerator); // 设置默认Id主键生成器

	}
	
	
	@Override
	public void initIdGenerators(Class<?> cls, ModelInfo modelInfo) {
		
		if(Long.class == modelInfo.getPrimaryKeyInfo().getType()) {
			
			//初始化主键Id生成器
			Map<Integer, IdGenerator<?>> idGenerators = new HashMap<Integer, IdGenerator<?>>();

			List<Integer> serverIdList = getServerIdList();
			if (serverIdList != null && serverIdList.size() > 0) {//配置的服

				for (int serverId: serverIdList) {

					long minValue = ServerEntityIdRule.getMinValueOfEntityId(serverId);
					long maxValue = ServerEntityIdRule.getMaxValueOfEntityId(serverId);

					//当前最大id
					long currMaxId = minValue;
					Object resultId = this.dbAccessService.loadMaxId(cls, minValue, maxValue);
					if (resultId != null) {
						currMaxId = (Long) resultId;
					}

					LongGenerator idGenerator = new LongGenerator(currMaxId);
					idGenerators.put(serverId, idGenerator);

					if (logger.isInfoEnabled()) {
						logger.info("服{}： {} 的当前自动增值ID：{}",
								new Object[] {serverId, cls.getName(), currMaxId});
					}
				}
			}
			
			// 设置主键id生成器
			modelInfo.setIdGenerators(idGenerators);
			
			// 默认的服Id
			Integer defaultSereverId = getDefaultServerId();
			IdGenerator<?> defaultIdGenerator = idGenerators.get(defaultSereverId);
			modelInfo.setDefaultIdGenerator(defaultIdGenerator); // 默认Id主键生成器
		}
		
	}
	

	/**
	 * 判断是否是合法的服标识
	 * @param serverId 服标识id
	 * @return true/false
	 */
	public boolean isLegalServerId(int serverId) {
		return serverId >= SERVER_ID_MIN_VALUE && serverId <= SERVER_ID_MAX_VALUE;
	}

	/**
	 * 取得实体ID最大值
	 * @param serverId 服标识id
	 * @return long
	 */
	public long getMaxValueOfEntityId(int serverId) {
		int valueOfServer = ID_BASE_VALUE_OF_SERVER + serverId;
		String valueStr = String.valueOf(valueOfServer) +
				ID_MAX_VALUE_OF_AUTOINCR;
		return Long.parseLong(valueStr);
	}

	/**
	 * 取得实体ID最小值
	 * @param serverId  服标识id
	 * @return long
	 */
	public long getMinValueOfEntityId(int serverId) {
		int valueOfServer = ID_BASE_VALUE_OF_SERVER + serverId;
		String valueStr = String.valueOf(valueOfServer) +
				STR_VALUE_OF_AUTOINCR_ID_MIN_VALUE;
		return Long.parseLong(valueStr);
	}

	/**
	 * 从玩家id中取得服标识ID
	 * @param userId 玩家id
	 * @return int
	 */
	public int getServerIdFromUser(long userId) {
		String userIdString = String.valueOf(userId);
		if (userIdString.length() == MAX_LENGTH_OF_USER_ID) {
			return Integer.parseInt(userIdString.substring(0, 5)) - ID_BASE_VALUE_OF_SERVER;
		}

		return -1;
	}
	

	/**
	 * 从玩家id中取得自增部分
	 * @param userId 玩家id
	 * @return long
	 */
	public long getAutoIncrPartFromUser(long userId) {
		int serverId = getServerIdFromUser(userId);
		if (serverId >= 0) {
			long minValue = getMinValueOfEntityId(serverId);
			return userId - minValue;
		}
		return -1L;
	}


	@Override
	public List<Integer> getServerIdList() {
		if (this.serverIdList == null) {
			return Collections.singletonList(1);
		}
		return Collections.unmodifiableList(serverIdList);
	}


	@Override
	public Integer getDefaultServerId() {
		if (this.serverIdList == null || this.serverIdList.size() == 0) {
			return 1;
		}
		return this.serverIdList.get(0);
	}


	@Override
	public boolean isServerMerged() {
		return this.serverIdList != null && this.serverIdList.size() > 1;
	}

	@Override
	public boolean containsServerId(int serverId) {
		return this.serverIdList != null && this.serverIdList.contains(serverId);
	}

	@Override
	public int getDbPoolSize() {
		return dbPoolSize;
	}

	@Override
	public String getEntityPackages() {
		return entityPackages;
	}

	@Override
	public int getEntityCacheSize() {
		return entityCacheSize;
	}

	@Override
	public long getDelayWaitTimmer() {
		return delayWaitTimmer;
	}


}
