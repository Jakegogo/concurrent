package gameserver.utils.dijkstra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 验证地图的所有点是否联通
 * <br/>每次开始新的搜索需要创建此类对象
 * 优化内容:寻找到最短路径后停止搜索。
 * 基于迪杰斯特拉(Dijkstra)算法
 * @param <T> 节点的主键类型
 * @author jake
 * @date 2014-7-26-下午9:45:07
 */
public class MapValidator<T> {
	
	/**
	 * 开放的节点
	 */
	Set<Maps.Node<T>> open = new HashSet<Maps.Node<T>>();
	/**
	 * 关闭的节点
	 */
	Set<Maps.Node<T>> close = new HashSet<Maps.Node<T>>();
	/**
	 * 最短路径距离
	 */
	Map<Maps.Node<T>, Integer> path = new HashMap<Maps.Node<T>, Integer>();
	/**
	 * 最短路径
	 */
	Map<T, List<T>> pathInfo = new HashMap<T, List<T>>();
	
	/**
	 * 初始化起始点
	 * <br/>初始时，S只包含起点s；U包含除s外的其他顶点，且U中顶点的距离为"起点s到该顶点的距离"
	 * [例如，U中顶点v的距离为(s,v)的长度，然后s和v不相邻，则v的距离为∞]。
	 * @param source 起始节点的Id
	 * @param map 全局地图
	 * @param closeSet 已经关闭的节点列表
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public void init(T source, Maps<T> map, Set<T> closeSet) {
		
		Map<T, Maps.Node<T>> nodeMap = map.getNodes();
		Maps.Node<T> startNode = nodeMap.get(source);
		//将初始节点放到close
		close.add(startNode);
		//将其他节点放到open
		for(Maps.Node<T> node : nodeMap.values()) {
			if(!closeSet.contains(node.getId()) && !node.getId().equals(source)) {
				this.open.add(node);
			}
		}
		
		// 初始路径
		T startNodeId = startNode.getId();
		for(Entry<Maps.Node<T>, Integer> entry : startNode.getChilds().entrySet()) {
			Maps.Node<T> node = entry.getKey();
			if(open.contains(node)) {
				T nodeId = node.getId();
				path.put(node, entry.getValue());
				pathInfo.put(nodeId, new ArrayList<T>(Arrays.asList(startNodeId, nodeId)));
			}
		}
		
		for(Maps.Node<T> node : nodeMap.values()) {
			if(open.contains(node) && !path.containsKey(node)) {
				path.put(node, Integer.MAX_VALUE);
				pathInfo.put(node.getId(), new ArrayList<T>(Arrays.asList(startNodeId)));
			}
		}
		//开始计算
		this.computePath(startNode);
	}
	
	
	/**
	 * 递归Dijkstra
	 * @param start 已经选取的最近节点
	 */
	protected void computePath(Maps.Node<T> start) {
		// 从U中选出"距离最短的顶点k"，并将顶点k加入到S中；同时，从U中移除顶点k。
		Maps.Node<T> nearest = getShortestPath(start);
		if (nearest == null) {
			return;
		}
		//更新U中各个顶点到起点s的距离。
		//之所以更新U中顶点的距离，是由于上一步中确定了k是求出最短路径的顶点，从而可以利用k来更新其它顶点的距离；
		//例如，(s,v)的距离可能大于(s,k)+(k,v)的距离。
		close.add(nearest);
		open.remove(nearest);
		Map<Maps.Node<T>, Integer> childs = nearest.getChilds();
		for (Map.Entry<Maps.Node<T>, Integer> entry : childs.entrySet()) {
			Maps.Node<T> child = entry.getKey();
			if (open.contains(child)) {// 如果子节点在open中
				Integer newCompute = path.get(nearest) + entry.getValue();
				if (path.get(child) > newCompute) {// 之前设置的距离大于新计算出来的距离
					path.put(child, newCompute);

					List<T> path = new ArrayList<T>(pathInfo.get(nearest.getId()));
					path.add(child.getId());
					pathInfo.put(child.getId(), path);
				}
			}
		}
//		computePath(start);// 重复执行自己,确保所有子节点被遍历
		computePath(nearest);// 向外一层层递归,直至所有顶点被遍历
	}
	
	/**
	 * 获取与node最近的子节点
	 */
	private Maps.Node<T> getShortestPath(Maps.Node<T> node) {
		Maps.Node<T> res = null;
		int minDis = Integer.MAX_VALUE;
		for (Map.Entry<Maps.Node<T>, Integer> entry : path.entrySet()) {
			Maps.Node<T> node1 = entry.getKey();
			if (open.contains(node1)) {
				int distance = entry.getValue();
				if (distance < minDis) {
					minDis = distance;
					res = node1;
				}
			}
		}
		return res;
	}
	
	
	/**
	 * 打印出所有点的最短路径
	 */
	public void printPathInfo() {
		Set<Map.Entry<T, List<T>>> pathInfos = pathInfo.entrySet();
		for (Map.Entry<T, List<T>> pathInfo : pathInfos) {
			System.out.println(pathInfo.getKey() + ":" + pathInfo.getValue());
		}
	}
	
	
	/**
	 * 获取无法到达的节点
	 * @return 节点ID列表
	 */
	public List<T> listUnreachableNode() {
		List<T> result = new ArrayList<T>();
		Set<Map.Entry<T, List<T>>> pathInfos = pathInfo.entrySet();
		for (Map.Entry<T, List<T>> pathInfo : pathInfos) {
			List<T> path = pathInfo.getValue();
			T nodeId = path.get(path.size() - 1);
			if(!nodeId.equals(pathInfo.getKey())) {
				result.add(pathInfo.getKey());
			}
		}
		return result;
	}
	
	
	/**
	 * 获取可到达的节点
	 * @return 节点ID列表
	 */
	public List<T> listReachableNode() {
		List<T> result = new ArrayList<T>();
		Set<Map.Entry<T, List<T>>> pathInfos = pathInfo.entrySet();
		for (Map.Entry<T, List<T>> pathInfo : pathInfos) {
			List<T> path = pathInfo.getValue();
			T nodeId = path.get(path.size() - 1);
			if(nodeId.equals(pathInfo.getKey())) {
				result.add(pathInfo.getKey());
			}
		}
		return result;
	}
	
}