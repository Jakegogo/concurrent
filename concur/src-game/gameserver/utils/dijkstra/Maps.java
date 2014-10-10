package gameserver.utils.dijkstra;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 地图
 * @author jake
 * @date 2014-7-26-下午10:40:10
 * @param <T> 节点主键
 */
public class Maps<T> {
	
	/**
	 * 所有的节点集合
	 * 节点Id - 节点
	 */
	private Map<T, Node<T>> nodes = new HashMap<T, Node<T>>();
	
	/**
	 * 地图构建器
	 * 
	 * @author jake
	 * @date 2014-7-26-下午9:47:44
	 */
	public static class MapBuilder<T> {
		
		/**
		 * map实例
		 */
		private Maps<T> map = new Maps<T>();

		/**
		 * 构造MapBuilder
		 * 
		 * @return MapBuilder
		 */
		public MapBuilder<T> create() {
			return new MapBuilder<T>();
		}

		/**
		 * 添加节点
		 * 
		 * @param node 节点
		 * @return
		 */
		public MapBuilder<T> addNode(Node<T> node) {
			map.nodes.put(node.getId(), node);
			return this;
		}
		
		/**
		 * 添加节点
		 * 
		 * @param node 节点 主键
		 * @return
		 */
		public MapBuilder<T> addNode(T nodeId) {
			map.nodes.put(nodeId, Node.valueOf(nodeId));
			return this;
		}

		/**
		 * 添加路线
		 * 
		 * @param node1Id 节点Id
		 * @param node2Id  节点Id
		 * @param weight 权重
		 * @return
		 */
		public MapBuilder<T> addPath(T node1Id, T node2Id, int weight) {
			Node<T> node1 = map.nodes.get(node1Id);
			if (node1 == null) {
				throw new RuntimeException("无法找到节点:" + node1Id);
			}

			Node<T> node2 = map.nodes.get(node2Id);
			if (node2 == null) {
				throw new RuntimeException("无法找到节点:" + node2Id);
			}

			node1.getChilds().put(node2, weight);
			node2.getChilds().put(node1, weight);
			return this;
		}
		
		/**
		 * 构建map
		 * @return map
		 */
		public Maps<T> build() {
			return this.map;
		}

	}
	
	/**
	 * 节点
	 * 
	 * @author jake
	 * @date 2014-7-26-下午9:51:31
	 * @param <T> 节点主键类型
	 */
	public static class Node<T> {

		/**
		 * 节点主键
		 */
		private T id;

		/**
		 * 节点联通路径
		 * 相连节点 - 权重
		 */
		private Map<Node<T>, Integer> childs = new HashMap<Node<T>, Integer>();
		
		/**
		 * 构造方法
		 * @param id 节点主键
		 */
		public Node(T id) {
			this.id = id;
		}
		
		/**
		 * 获取实例
		 * @param id 主键
		 * @return
		 */
		public static <T> Node<T> valueOf(T id) {
			return new Node<T>(id);
		}
		
		/**
		 * 是否有效
		 * 用于动态变化节点的可用性
		 * @return
		 */
		public boolean validate() {
			return true;
		}
		
		
		public T getId() {
			return id;
		}

		public void setId(T id) {
			this.id = id;
		}

		public Map<Node<T>, Integer> getChilds() {
			return childs;
		}

		protected void setChilds(Map<Node<T>, Integer> childs) {
			this.childs = childs;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.id).append("[");
			for (Iterator<Entry<Node<T>, Integer>> it = childs.entrySet().iterator(); it.hasNext();) {
				Entry<Node<T>, Integer> next = it.next();
				sb.append(next.getKey().getId()).append("=").append(next.getValue());
				if (it.hasNext()) {
					sb.append(",");
				}
			}
			sb.append("]");
			return sb.toString();
		}
		
	}

	/**
	 * 获取地图的无向图节点
	 * @return 节点Id - 节点
	 */
	public Map<T, Node<T>> getNodes() {
		return nodes;
	}

}
