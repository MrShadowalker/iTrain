package org.itron.itrain.utils.merkle;

import lombok.Data;
import org.itron.itrain.utils.hash.SHAUtil;

/**
 * 定义 Merkle 树中节点数据结构
 *
 * @author Shadowalker
 */
@Data
public class TreeNode {
    // 二叉树左孩子
    private TreeNode left;
    // 二叉树右孩子
    private TreeNode right;
    // 二叉树中孩子节点的数据
    private String data;
    // 二叉树中孩子节点的数据对应的 hash 值，此处采取 SHA-256 算法处理
    private String hash;
    // 节点名称
    private String name;

    // 构造函数
    public TreeNode() {

    }

    // 构造函数
    public TreeNode(String data) {
        this.data = data;
        this.hash = SHAUtil.sha256BaseHutool(data);
        this.name = "[节点：" + data + "]";
    }
}
