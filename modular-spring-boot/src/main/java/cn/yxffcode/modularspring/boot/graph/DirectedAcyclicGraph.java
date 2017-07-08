package cn.yxffcode.modularspring.boot.graph;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 有向无环图(DAG)的实现,使用邻接表表示法
 *
 * @author gaohang on 7/2/17.
 */
public class DirectedAcyclicGraph<E> {

  private Map<E, List<E>> graph = Maps.newHashMap();

  /**
   * 将添加节点与边
   *
   * @param node          图的节点
   * @param linkedToNodes 边连接到的节点
   */
  public void link(E node, Iterable<E> linkedToNodes) {
    checkNotNull(node);
    if (linkedToNodes == null) {
      graph.put(node, Collections.emptyList());
    } else {
      if (linkedToNodes instanceof List) {
        graph.put(node, (List<E>) linkedToNodes);
      } else {
        graph.put(node, Lists.newArrayList(linkedToNodes));
      }
    }
  }

  public List<E> topological() {
    if (graph.isEmpty()) {
      return Collections.emptyList();
    }
    final List<E> nodes = Lists.newArrayListWithCapacity(graph.size());
    final Set<E> marks = Sets.newHashSetWithExpectedSize(graph.size());
    //深度优先搜索
    for (Map.Entry<E, List<E>> en : graph.entrySet()) {
      if (!marks.contains(en.getKey())) {
        visit(en.getKey(), en.getValue(), marks, nodes);
      }
    }
    return nodes;
  }

  private void visit(final E node, final List<E> edges, final Set<E> marks, final List<E> result) {
    if (CollectionUtils.isEmpty(edges)) {
      result.add(node);
      marks.add(node);
      return;
    }
    for (E edge : edges) {
      if (!marks.contains(edge)) {
        visit(edge, graph.get(edge), marks, result);
      }
    }
    result.add(node);
    marks.add(node);
  }
}
