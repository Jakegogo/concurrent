package dbcache.refcache;

import java.lang.ref.Reference;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * 支持检索的完全并发和更新的所期望可调整并发并且键值对支持引用类型的哈希表。尽管所有操作都是线程安全的，但检索操作不 必锁定，并且不
 * 支持以某种防止所有访问的方式锁定整个表。
 * <p>
 * 检索操作（包括 get）通常不会受阻塞，因此，可能与更新操作交迭（包括 put 和 remove）。检索会影响最近完成的
 * 更新操作的结果。对于一些聚合操作，比如 putAll 和
 * clear，并发检索可能只影响某些条目的插入和移除。类似地，在创建迭代器/枚举时或自此之后，Iterators 和 Enumerations
 * 返回在某一时间点上影响哈希表状态的元素。它们不会 抛出
 * ConcurrentModificationException。不过，迭代器被设计成每次仅由一个线程使用。
 * </p>
 * <p>
 * 这允许通过可选的 concurrencyLevel 构造方法参数（默认值为
 * 16）来引导更新操作之间的并发，该参数用作内部调整大小的一个提示。表是在内部进行分区的
 * ，试图允许指示无争用并发更新的数量。因为哈希表中的位置基本上是随意的，
 * 所以实际的并发将各不相同。理想情况下，应该选择一个尽可能多地容纳并发修改该表的线程的值
 * 。使用一个比所需要的值高很多的值可能会浪费空间和时间，而使用一个显然低很多的值可能导致线程争用
 * 。对数量级估计过高或估计过低通常都会带来非常显著的影响。当仅有一个线程将执行修改操作
 * ，而其他所有线程都只是执行读取操作时，才认为某个值是合适的。此外，重新调整此类或其他任何种类哈希表的大小都是一个相对较慢的操作
 * ，因此，在可能的时候，提供构造方法中期望表大小的估计值是一个好主意。
 * </p>
 * <p>
 * 引用类型请参考 {@link java.lang.ref}与 {@link cn.agrael.ref} 的描述。
 * </p>
 * <p>
 * 在第一次调用 put/putAll 方法时(仅仅是在同一个类加载器中的第一次
 * ，如果为同一类加载器，那么在put/putAll后，新创建的实例的put/putAll也不算做第一次)，会有一个后台线程随之创建并启动
 * ，该后台线程主要作用和垃圾回收交互，进行回收对象的清理。在清理 键 的同时，会一同清理掉 键的引用包装器 以及 值 和 值的引用包装器 以及 印射
 * ，在清理 值 时 ，会一同清理掉 值的引用包装器，然后从 Map 中移除印射到自身的 键 以及 印射 。
 * </p>
 * <p>
 * <strong>该清理是 异步 且 “即时” 的,会在垃圾回收的同时清理，不需要在下次对该 Map 操作时才清理包装引用的实例。</strong>
 * </p>
 * <p>
 * 此类及其视图和迭代器实现了 {@link ConcurrentMap} 和 {@link Iterator} 接口的所有可选 方法。
 * </p>
 * <p>
 * 该实现不支持参数为 null。如果传入 null 则会抛出 {@link NullPointerException}。
 * </p>
 *
 * @author <a href="http://www.agrael.cn">李翔</a>
 *
 * @param <K>
 *            此映射维护的键类型
 * @param <V>
 *            映射值的类型
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ConcurrentReferenceMap<K, V> extends ConcurrentHashMap {

	/** 版本号 */
	private static final long serialVersionUID = -6254917146703229097L;

	private static final FinalizableReferenceQueue FINALIZABLE_REFERENCE_QUEUE = new FinalizableReferenceQueue("ConcurrentReferenceMapReferenceQueue");

	private final ReferenceKeyType keyReferenceType;

	private final ReferenceValueType valueReferenceType;


	/**
	 * 通过指定的 key引用类型 和 value引用类型 创建一个带有默认初始容量、加载因子和 concurrencyLevel 的新的空映射。
	 * @param keyReferenceType key引用类型 。
	 * @param valueReferenceType value引用类型。
	 */
	public ConcurrentReferenceMap(ReferenceKeyType keyReferenceType, ReferenceValueType valueReferenceType) {
		super();
		this.keyReferenceType = keyReferenceType;
		this.valueReferenceType = valueReferenceType;
	}

	/**
	 * 通过指定的 key引用类型 和 value引用类型 创建一个带有指定初始容量、默认加载因子和 concurrencyLevel 的新的空映射。
	 * @param keyReferenceType key引用类型。
	 * @param valueReferenceType value引用类型。
	 * @param initialCapacity 初始容量。该实现执行内部大小调整，以容纳这些元素。
	 */
	public ConcurrentReferenceMap(ReferenceKeyType keyReferenceType, ReferenceValueType valueReferenceType,
			int initialCapacity) {
		super(initialCapacity);
		this.keyReferenceType = keyReferenceType;
		this.valueReferenceType = valueReferenceType;
	}

	/**
	 * 通过指定的 key引用类型 和 value引用类型 创建一个带有指定初始容量、加载因子和并发级别的新的空映射。
	 * @param keyReferenceType key引用类型。
	 * @param valueReferenceType value引用类型。
	 * @param initialCapacity 初始容量。该实现执行内部大小调整，以容纳这些元素。
	 * @param loadFactor 加载因子阈值，用来控制重新调整大小。在每 bin 中的平均元素数大于此阈值时，可能要重新调整大小。
	 * @param concurrencyLevel 当前更新线程的估计数。该实现将执行内部大小调整，以尽量容纳这些线程。
	 */
	public ConcurrentReferenceMap(ReferenceKeyType keyReferenceType, ReferenceValueType valueReferenceType,
			int initialCapacity, float loadFactor, int concurrencyLevel) {
		super(initialCapacity, loadFactor, concurrencyLevel);
		this.keyReferenceType = keyReferenceType;
		this.valueReferenceType = valueReferenceType;
	}

//	@Override
//	public Set<K> keySet() {
//		if (this.keyReferenceType == ReferenceKeyType.STRONG) {
//			// 如果为强引用，则 map 中的 keySet 就是需要的
//			return (Set<K>) super.keySet();
//		}
//		// 为其他引用
//		return new AbstractSet<K>() {
//
//			public Iterator<K> iterator() {
//				return new Iterator<K>() {
//					private Iterator<K> iterator = ConcurrentReferenceMap.super.keySet().iterator();
//					public boolean hasNext() {
//						return this.iterator.hasNext();
//					}
//
//					public K next() {
//						return getKey(this.iterator.next());
//					}
//
//					public void remove() {
//						this.iterator.remove();
//					}
//
//				};
//			}
//
//			public int size() {
//				return ConcurrentReferenceMap.super.size();
//			}
//
//		};
//	}


	@Override
	public void clear() {
		super.clear();
	}

	@Override
	public V remove(Object key) {
		notNull(key);
		Object keyReference = createKeyReferenceWrapper(key);
		Object returnValueReference = super.remove(keyReference);
		return getValue(returnValueReference);
	}

	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(key);
	}

	@Override
	public int size() {
		return super.size();
	}

	@Override
	public boolean isEmpty() {
		return super.isEmpty();
	}

	private V getValue(Object valueReference) {
		return (V) (this.valueReferenceType == ReferenceValueType.STRONG ? valueReference : valueReference == null ? null : ((Reference<V>) valueReference).get());
	}


	private K getKey(Object keyReference) {
		return (K) (this.keyReferenceType == ReferenceKeyType.STRONG ? keyReference : keyReference == null ? null : ((Reference<K>) keyReference).get());
	}

	/**
	 * 创建 Key 的印射包装器。
	 * @param key 要创建印射包装器的 key。
	 * @return 如果是强引用，返回 key 本身，否则返回  Key 的印射包装器。
	 */
	private Object createKeyReferenceWrapper(Object key) {
		return this.keyReferenceType == ReferenceKeyType.STRONG ? key : new KeyReferenceWrapper(key);
	}

	/**
	 * 创建 Value 的印射包装器。
	 * @param value 要创建印射包装器的 value。
	 * @return 如果是强引用，返回 value 本身，否则返回  Value 的印射包装器。
	 */
	private Object createValueReferenceWrapper(Object value) {
		return this.valueReferenceType == ReferenceValueType.STRONG ? value : new ValueReferenceWrapper(value);
	}

	/**
	 * 得到 key 在内存中的地址的 hashCode。
	 * @param key 要得到在内存中的地址的 hashCode 的 key。
	 * @return key 在内存中的地址的 hashCode。
	 * @see System#identityHashCode(Object)
	 */
	private int keyHashCode(Object key) {
		return System.identityHashCode(key);
	}


	private boolean referenceEquals(Reference<?> reference, Object object) {
		if (reference == object) {
			// 直接引用相同
			return true;
		}
		if (reference == null) {
			// 一方为 null 则返回 false（因为上个判断结合这个判断，object 一定不为 null）
			return false;
		}
		if (object instanceof Reference<?>) {
			// object 为引用类型，则比较引用类型中的值的引用
			Object referenceValue = ((Reference<?>) object).get();
			return referenceValue != null && referenceValue.equals(reference.get());
		}
		if (object instanceof ReferenceWrapper) {
			// object为 引用包装类型
			return ((ReferenceWrapper) object).getReference().equals(reference.get());
		}
		return false;
	}

	@Override
	public V get(Object key) {
		notNull(key);
		Object returnValueReference = super.get(key);
		return getValue(returnValueReference);
	}

	@Override
	public V put(Object key, Object value) {
		notNull(key);
		notNull(value);
		Object keyReference = wrapKey(key);
		Object valueReference = wrapValue(keyReference, value);
		Object returnValueReference = super.put(keyReference, valueReference);
		if (returnValueReference == null) {
			return null;
		}
		return getValue(returnValueReference);
	}

	public V putIfAbsent(Object key, Object value) {
		notNull(key);
		notNull(value);
		Object keyReference = wrapKey(key);
		Object valueReference = wrapValue(keyReference, value);
		Object returnValueReference = super.putIfAbsent(keyReference, valueReference);
		if (returnValueReference == null) {
			return null;
		}
		return getValue(returnValueReference);
	}

	public boolean remove(Object key, Object value) {
		notNull(key);
		Object keyReference = createKeyReferenceWrapper(key);
		Object valueReference = createValueReferenceWrapper(value);
		return super.remove(keyReference, valueReference);
	}

	public boolean replace(Object key, Object oldValue, Object newValue) {
		notNull(key);
		notNull(oldValue);
		notNull(newValue);
		Object keyReference = wrapKey(key);
		Object oldValueReference = createValueReferenceWrapper(oldValue);
		Object valueReference = wrapValue(keyReference, newValue);
		return super.replace(keyReference, oldValueReference, valueReference);
	}

	public V replace(Object key, Object value) {
		notNull(key);
		notNull(value);
		Object keyReference = wrapKey(key);
		Object valueReference = wrapValue(keyReference, value);
		Object returnValueReference = super.replace(keyReference, valueReference);
		if (returnValueReference == null) {
			return null;
		}
		return getValue(returnValueReference);
	}

	private Object wrapKey(Object key) {
		Object keyReference = null;
		switch (this.keyReferenceType) {
		case STRONG:
			keyReference = key;
			break;
		case SOFT:
			keyReference = new FinalizableKeySoftReference(key);
			break;
		case WEAK:
			keyReference = new FinalizableKeyWeakReference(key);
			break;
		case PHANTOM:
			keyReference = new FinalizableKeyPhantomReference(key);
			break;
		}
		return keyReference;
	}

	private Object wrapValue(Object keyReference, Object value) {
		Object valueReference = null;
		switch (this.valueReferenceType) {
		case STRONG:
			valueReference = value;
			break;
		case SOFT:
			valueReference = new FinalizableValueSoftReference(keyReference ,value);
			break;
		case WEAK:
			valueReference = new FinalizableValueWeakReference(keyReference ,value);
			break;
		case PHANTOM:
			valueReference = new FinalizableValuePhantomReference(keyReference ,value);
			break;
		}
		return valueReference;
	}

	public Set<Entry<K, V>> entrySet() {
		return new AbstractSet<Entry<K, V>>() {

			public Iterator<Entry<K, V>> iterator() {
				return new Iterator<Entry<K, V>>() {
					private Iterator<Entry<Object, Object>> iterator = ConcurrentReferenceMap.super.entrySet().iterator();

					public boolean hasNext() {
						return this.iterator.hasNext();
					}

					public Entry<K, V> next() {
						final Entry<Object, Object> entry = this.iterator.next();
						return new Entry<K, V>() {

							public K getKey() {
								return ConcurrentReferenceMap.this.getKey(entry.getKey());
							}

							public V getValue() {
								return ConcurrentReferenceMap.this.getValue(entry.getValue());
							}

							public V setValue(V value) {
								throw new UnsupportedOperationException("不支持的操作。");
							}

						};
					}

					public void remove() {
						this.iterator.remove();
					}

				};
			}

			public int size() {
				return ConcurrentReferenceMap.super.size();
			}

		};
	}

	/**
	 * 弱引用对象的键类。
	 * @author <a href="http://www.agrael.cn">李翔</a>
	 *
	 * @param <T> 引用类型
	 */
	protected class FinalizableKeyWeakReference<T> extends FinalizableWeakReference<T> implements FinalizableReference<T> {

		private final int hashCode;

		/**
		 * 构造一个新的 弱引用对象的键类 实例。
		 * @param referent 键。
		 */
		protected FinalizableKeyWeakReference(T referent) {
			super(referent, FINALIZABLE_REFERENCE_QUEUE);
			this.hashCode = keyHashCode(referent);
		}

		public void finalizeReferent() {
			ConcurrentReferenceMap.super.remove(this);
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}

		@Override
		public boolean equals(Object object) {
			return referenceEquals(this, object);
		}
	}

	/**
	 * 弱引用对象的值类。
	 * @author <a href="http://www.agrael.cn">李翔</a>
	 *
	 * @param <T> 引用类型
	 */
	protected class FinalizableValueWeakReference<T> extends FinalizableWeakReference<T> implements FinalizableReference<T> {

		private Object keyReference;

		/**
		 * 构造一个新的 弱引用对象的值类 实例。
		 * @param keyReference 与此值关联的 键 。
		 * @param referent 值。
		 */
		protected FinalizableValueWeakReference(Object keyReference, T referent) {
			super(referent, FINALIZABLE_REFERENCE_QUEUE);
			this.keyReference = keyReference;
		}

		public void finalizeReferent() {
			ConcurrentReferenceMap.super.remove(this.keyReference, this);
		}

		@Override
		public int hashCode() {
			T v = get();
			return v == null ? 0 : v.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ConcurrentReferenceMap<?, ?>.FinalizableValueWeakReference<?>)) {
				return false;
			}
			T v = get();
			Object objV = ((FinalizableValueWeakReference<?>) obj).get();

			if (v == null) {
				if (v == objV) {
					return true;
				}
				return false;
			} else {
				return v.equals(objV);
			}
		}
	}

	/**
	 * 软引用对象的键类。
	 * @author <a href="http://www.agrael.cn">李翔</a>
	 *
	 * @param <T> 引用类型
	 */
	protected class FinalizableKeySoftReference<T> extends FinalizableSoftReference<T> implements FinalizableReference<T> {

		private final int hashCode;

		/**
		 * 构造一个新的 软引用对象的键类 实例。
		 * @param referent 键。
		 */
		protected FinalizableKeySoftReference(T referent) {
			super(referent, FINALIZABLE_REFERENCE_QUEUE);
			this.hashCode = keyHashCode(referent);
		}

		public void finalizeReferent() {
			ConcurrentReferenceMap.super.remove(this);
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}

		@Override
		public boolean equals(Object object) {
			return referenceEquals(this, object);
		}
	}

	/**
	 * 软引用对象的值类。
	 * @author <a href="http://www.agrael.cn">李翔</a>
	 *
	 * @param <T> 引用类型
	 */
	protected class FinalizableValueSoftReference<T> extends FinalizableSoftReference<T> implements FinalizableReference<T> {

		private Object keyReference;

		/**
		 * 构造一个新的 软引用对象的值类 实例。
		 * @param keyReference 与此值关联的 键 。
		 * @param referent 值。
		 */
		protected FinalizableValueSoftReference(Object keyReference ,T referent) {
			super(referent, FINALIZABLE_REFERENCE_QUEUE);
			this.keyReference = keyReference;
		}

		public void finalizeReferent() {
			ConcurrentReferenceMap.super.remove(this.keyReference, this);
		}

		@Override
		public int hashCode() {
			T v = get();
			return v == null ? 0 : v.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ConcurrentReferenceMap<?, ?>.FinalizableValueSoftReference<?>)) {
				return false;
			}
			T v = get();
			Object objV = ((FinalizableValueSoftReference<?>) obj).get();
			if (v == null) {
				if (v == objV) {
					return true;
				}
				return false;
			} else {
				return v.equals(objV);
			}
		}
	}



	/**
	 * 虚引用对象的键类。
	 * @author <a href="http://www.agrael.cn">李翔</a>
	 *
	 * @param <T> 引用类型
	 */
	protected class FinalizableKeyPhantomReference<T> extends FinalizablePhantomReference<T> implements FinalizableReference<T> {

		private final int hashCode;

		/**
		 * 构造一个新的 虚引用对象的键类 实例。
		 * @param referent 键。
		 */
		protected FinalizableKeyPhantomReference(T referent) {
			super(referent, FINALIZABLE_REFERENCE_QUEUE);
			this.hashCode = keyHashCode(referent);
		}

		public void finalizeReferent() {
			ConcurrentReferenceMap.super.remove(this);
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}

		@Override
		public boolean equals(Object object) {
			return referenceEquals(this, object);
		}
	}

	/**
	 * 虚引用对象的值类。
	 * @author <a href="http://www.agrael.cn">李翔</a>
	 *
	 * @param <T> 引用类型
	 */
	protected class FinalizableValuePhantomReference<T> extends FinalizablePhantomReference<T> implements FinalizableReference<T> {

		private Object keyReference;

		/**
		 * 构造一个新的 虚引用对象的值类 实例。
		 * @param keyReference 与此值关联的 键 。
		 * @param referent 值。
		 */
		protected FinalizableValuePhantomReference(Object keyReference ,T referent) {
			super(referent, FINALIZABLE_REFERENCE_QUEUE);
			this.keyReference = keyReference;
		}

		public void finalizeReferent() {
			ConcurrentReferenceMap.super.remove(this.keyReference, this);
		}

		@Override
		public int hashCode() {
			return 0;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof ConcurrentReferenceMap<?, ?>.FinalizableValuePhantomReference<?>)) {
				return false;
			}
			Object v = ((FinalizableValuePhantomReference<?>) obj).get();
			if (v == null) {
				return true;
			}
			return v.equals(get());
		}
	}

	/**
	 * 引用类型中的对象的另一个包装器，该包装器实现为引用类型中的对象保持 put 与 get 时用到的 hashCode 和 equals 方法的一致性。
	 */
	protected static class ReferenceWrapper {

		protected final Object reference;

		/**
		 * 使用一个引用类型中的对象实例构造一个新的包装器。
		 * @param reference 要包装的引用类型中的对象实例。
		 */
		protected ReferenceWrapper(Object reference) {
			this.reference = reference;
		}

		/**
		 * 得到被包装的对象。
		 * @return 被包装的对象。
		 */
		protected Object getReference() {
			return this.reference;
		}

		/**
		 * 调用包装对象的 hashCode 方法。
		 * @return 包装对象的 hashCode。
		 */
		@Override
		public int hashCode() {
			return this.reference.hashCode();
		}

		/**
		 * 调用包装对象的  equals 方法比较。
		 * @param obj 要与之比较的对象。
		 * @return 如果传入的对象和包装对象“相等”则返回 true，否则返回 false。
		 */
		@Override
		public boolean equals(Object obj) {
			if(obj == this) {
				return true;
			}
			if(obj == null) {
				return false;
			}
			if(!(obj instanceof ReferenceWrapper)) {
				return false;
			}
			ReferenceWrapper target = (ReferenceWrapper) obj;
			// 使用包装对象的 equals
			return target.reference.equals(this.reference);
		}
	}

	/**
	 * 专用于  key 的引用类型中的对象的另一个包装器。该包装器重写了 hashCode 方法，使用 {@link System#identityHashCode(Object)} 取得 hash值 。
	 * @author <a href="http://www.agrael.cn">李翔</a>
	 *
	 */
	protected static class KeyReferenceWrapper extends ReferenceWrapper {

		/**
		 * 使用一个引用类型中的 key对象实例 构造一个新的包装器。
		 * @param reference 要包装的引用类型中的key对象实例。
		 */
		protected KeyReferenceWrapper(Object reference) {
			super(reference);
		}

		/**
		 * 返回包装的 Key 的内存里的 hashCode。
		 * @return 包装的 Key 的内存里的 hashCode。
		 * @see System#identityHashCode(Object)
		 */
		@Override
		public int hashCode() {
			// 这里要认为同一内存地址的才是相同对象，所以取得内存地址中的 HASH码
			return System.identityHashCode(this.reference);
		}
	}

	/**
	 * 专用于  value 的引用类型中的对象的另一个包装器。该包装器重写了 hashCode 方法，使用 {@link System#identityHashCode(Object)} 取得 hash值 。
	 * @author <a href="http://www.agrael.cn">李翔</a>
	 *
	 */
	protected static class ValueReferenceWrapper extends ReferenceWrapper {

		/**
		 * 使用一个引用类型中的 key对象实例 构造一个新的包装器。
		 * @param reference 要包装的引用类型中的key对象实例。
		 */
		protected ValueReferenceWrapper(Object reference) {
			super(reference);
		}

	}

	/**
	 * 用作 K 的引用类型。
	 * @author <a href="http://www.agrael.cn">李翔</a>
	 *
	 */
	public static enum ReferenceKeyType {

		/** 强引用 */
		STRONG,

		/** 软引用 */
		SOFT,

		/** 弱引用 */
		WEAK,

		/** 虚引用 */
		PHANTOM;

	}

	/**
	 * 用作 V 的引用类型。
	 * @author <a href="http://www.agrael.cn">李翔</a>
	 *
	 */
	public static enum ReferenceValueType {

		/** 强引用 */
		STRONG,

		/** 软引用 */
		SOFT,

		/** 弱引用 */
		WEAK,

		/** 虚引用 */
		PHANTOM;
	}

	static void notNull(Object obj) {
		if (obj == null) {
			throw new NullPointerException();
		}
	}

}
