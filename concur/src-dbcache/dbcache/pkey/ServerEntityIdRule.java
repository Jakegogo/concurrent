package dbcache.pkey;



/**
 * 约定服实体Id生成规则：类型Long, 15位十进制, 由 服标识部分(5位)+自增部分(10位) 组成
 * <p>服标识部分:10000 ~ 99999, 即 10000 + 服标识(int)
 * <p>自增部分： 0 ~ 10个9
 */
public class ServerEntityIdRule {
	
	/**
	 * 服标识部分ID基础值
	 */
	private static final int ID_BASE_VALUE_OF_SERVER = 10000;
	
	/**
	 * 服标识部分ID最大值
	 */
	private static final int ID_MAX_VALUE_OF_SERVER = 99999;
	
	/**
	 * 服标识最大值
	 */
	private static final int SERVER_ID_MAX_VALUE = ID_MAX_VALUE_OF_SERVER - ID_BASE_VALUE_OF_SERVER;
	
	/**
	 * 服标识最小值
	 */
	private static final int SERVER_ID_MIN_VALUE = 0;
	
	/**
	 * 自增部分的ID最大值(10个9)
	 */
	private static final long ID_MAX_VALUE_OF_AUTOINCR = 9999999999L;
	
	/**
	 * 自增部分的ID最小值
	 */
	private static final long ID_MIN_VALUE_OF_AUTOINCR = 0L;
	
	/**
	 * 自增部分的ID最小值补齐字符串
	 */
	public static final String STR_VALUE_OF_AUTOINCR_ID_MIN_VALUE = String.format("%010d", ID_MIN_VALUE_OF_AUTOINCR);
	
	/**
	 * 玩家id长度
	 */
	private static final int MAX_LENGTH_OF_USER_ID = 15;
	
	
	
	/**
	 * 判断是否是合法的服标识
	 * @param serverId 服标识id
	 * @return true/false
	 */
	public static boolean isLegalServerId(int serverId) {
		return serverId >= SERVER_ID_MIN_VALUE && serverId <= SERVER_ID_MAX_VALUE;
	}
	
	/**
	 * 取得实体ID最大值
	 * @param serverId 服标识id
	 * @return long
	 */
	public static long getMaxValueOfEntityId(int serverId) {
		int valueOfServer = ID_BASE_VALUE_OF_SERVER + serverId;
		String valueStr = new StringBuffer().append(valueOfServer)
											.append(ID_MAX_VALUE_OF_AUTOINCR)
											.toString();
		return Long.valueOf(valueStr).longValue();
	}
	
	/**
	 * 取得实体ID最小值
	 * @param serverId  服标识id
	 * @return long
	 */
	public static long getMinValueOfEntityId(int serverId) {
		int valueOfServer = ID_BASE_VALUE_OF_SERVER + serverId;
		String valueStr = new StringBuffer().append(valueOfServer)
											.append(STR_VALUE_OF_AUTOINCR_ID_MIN_VALUE)
											.toString();
		return Long.valueOf(valueStr).longValue();
	}
	
	/**
	 * 从玩家id中取得服标识ID
	 * @param userId 玩家id
	 * @return int
	 */
	public static int getServerIdFromUser(long userId) {
		String userIdString = String.valueOf(userId);
		if (userIdString.length() == MAX_LENGTH_OF_USER_ID) {
			return Integer.parseInt(userIdString.substring(0, 5)) - ID_BASE_VALUE_OF_SERVER;
		}
		
		return -1;
	}
	
	/**
	 * 从玩家id中取得自增部分
	 * @param userId 玩家id
	 * @return int
	 */
	public static long getAutoIncrPartFromUser(long userId) {
		int serverId = getServerIdFromUser(userId);
		if (serverId >= 0) {
			long minValue = getMinValueOfEntityId(serverId);
			return userId - minValue;
		}
		return -1L;
	}
}
