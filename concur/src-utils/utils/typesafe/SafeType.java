package utils.typesafe;


/**
 * 线程安全类型
 * @author Jake
 */
public abstract class SafeType {
	
	/** 最后一次执行的Actor 使用UNSAFE进行处理 */
	private volatile SafeRunner tail;
	
	/**
	 * 获取尾节点
	 * @return
	 */
	public SafeRunner getTail() {
		return tail;
	}
	
	/**
	 * 追加到尾节点
	 * @param oldSafeRunnable
	 * @param safeRunner
	 * @return
	 */
	boolean casTail(SafeRunner oldSafeRunnable, SafeRunner safeRunner) {
		return UNSAFE.compareAndSwapObject(this, tailOffset, oldSafeRunnable, safeRunner);
	}
	
	
	// Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long tailOffset;
    static {
        try {
            UNSAFE = getUnsafe();
            Class<?> sk = SafeType.class;
            tailOffset = UNSAFE.objectFieldOffset
                (sk.getDeclaredField("tail"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    /**
     * Returns a sun.misc.Unsafe.  Suitable for use in a 3rd party package.
     * Replace with a simple call to Unsafe.getUnsafe when integrating
     * into a jdk.
     *
     * @return a sun.misc.Unsafe
     */
    private static sun.misc.Unsafe getUnsafe() {
        try {
            return sun.misc.Unsafe.getUnsafe();
        } catch (SecurityException se) {
            try {
                return java.security.AccessController.doPrivileged
                    (new java.security
                        .PrivilegedExceptionAction<sun.misc.Unsafe>() {
                        public sun.misc.Unsafe run() throws Exception {
                            java.lang.reflect.Field f = sun.misc
                                .Unsafe.class.getDeclaredField("theUnsafe");
                            f.setAccessible(true);
                            return (sun.misc.Unsafe) f.get(null);
                        }});
            } catch (java.security.PrivilegedActionException e) {
                throw new RuntimeException("Could not initialize intrinsics",
                    e.getCause());
            }
        }
    }

}
