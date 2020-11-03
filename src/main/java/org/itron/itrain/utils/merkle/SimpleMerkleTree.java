package org.itron.itrain.utils.merkle;

import org.itron.itrain.utils.hash.SHAUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 简化版 Merkle 树的根节点哈希值计算
 * 在密码学和计算机科学中，哈希树或 Merkle 树是一种树
 * 其中每个叶子节点都标记有数据块的哈希
 * 而每个非叶子节点都标记有其子节点标签的加密哈希
 * Merkle 树允许对大型数据结构的内容进行有效、安全的验证，是散列列表和散列链的泛化
 *
 * @author Shadowalker
 */
public class SimpleMerkleTree {
    // 按 Merkle 树思想计算根节点哈希值
    public static String getTreeNodeHash(List<String> hashsList) {
        if (hashsList == null || hashsList.size() == 0) {
            return null;
        }
        while (hashsList.size() != 1) {
            hashsList = getMerkleNodeList(hashsList);
        }
        // 最终只剩下根节点
        return hashsList.get(0);
    }

    /**
     * 按 Merkle 树思想计算根节点哈希值
     *
     * @param contentList
     * @return
     */
    public static List<String> getMerkleNodeList(List<String> contentList) {
        List<String> merkleNodeList = new ArrayList<>();
        if (contentList == null || contentList.size() == 0) {
            return merkleNodeList;
        }
        int index = 0;
        int length = contentList.size();
        while (index < length) {
            // 获取左孩子节点数据
            String left = contentList.get(index++);
            // 获取右孩子节点数据
            String right = "";
            if (index < length) {
                right = contentList.get(index++);
            }
            // 计算左右孩子节点的父节点的哈希值
            String sha2HexValue = SHAUtil.sha256BaseHutool(left + right);
            merkleNodeList.add(sha2HexValue);
        }
        return merkleNodeList;
    }
}
