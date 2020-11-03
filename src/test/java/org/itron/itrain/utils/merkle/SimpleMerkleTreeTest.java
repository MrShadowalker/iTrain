package org.itron.itrain.utils.merkle;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SimpleMerkleTree 的单元测试类
 *
 * @author Shadowalker
 */
public class SimpleMerkleTreeTest {

    @Test
    public void testGetMerkleNodeList() {
        // case1: List<String> contentList = null;
        List<String> contentList = null;
        Assert.assertEquals(SimpleMerkleTree.getMerkleNodeList(contentList).size(), 0);

        // case2: List<String> contentList = new ArrayList<>();
        contentList = new ArrayList<>();
        Assert.assertEquals(SimpleMerkleTree.getMerkleNodeList(contentList).size(), 0);

        // case3: contentList 有数据填充
        contentList = Arrays.asList("区块链", "人工智能", "脑科学");
        Assert.assertEquals(SimpleMerkleTree.getMerkleNodeList(contentList).size(), 2);
    }

    @Test
    public void testGetTreeNodeHash() {
        // case1: contentList = null;
        List<String> contentList = null;
        Assert.assertEquals(SimpleMerkleTree.getTreeNodeHash(contentList), null);

        // case2: List<String> contentList = new ArrayList<>();
        contentList = new ArrayList<>();
        Assert.assertEquals(SimpleMerkleTree.getTreeNodeHash(contentList), null);

        // case3: contentList 有数据填充
        contentList = Arrays.asList("区块链", "人工智能", "脑科学");
        Assert.assertEquals(SimpleMerkleTree.getTreeNodeHash(contentList), "7102ac2e1e54df6fa7617f99acb6bc0dd588775b0a755cdf2c18d9b7311ed746");
    }
}
