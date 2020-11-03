package org.itron.itrain.utils.merkle;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * MerkleTree 的单元测试类
 *
 * @author Shadowalker
 */
public class MerkleTreeTest {

    @Test
    public void testMerkleTree() {

        // case1: List<String> contents = null
        List<String> contents = null;
        Assert.assertEquals(new MerkleTree(contents).getList(), null);
        Assert.assertEquals(new MerkleTree(contents).getRoot(), null);

        // case2: contents = new ArrayList<>();
        contents = new ArrayList<>();
        Assert.assertEquals(new MerkleTree(contents).getList(), null);
        Assert.assertEquals(new MerkleTree(contents).getRoot(), null);

        // case3: contents 有内容
        contents = Arrays.asList("区块链", "人工智能", "脑科学");
        Assert.assertEquals(new MerkleTree(contents).getRoot().getHash(), "9dba1de8a8173bf676310204089a687eed2c6b095238be699786e7176425e04f");
        Assert.assertEquals(new MerkleTree(contents).getRoot().getName(), "(([节点：区块链]和[节点：人工智能]的父节点和(继承节点{[节点：脑科学]}成为父节点的父节点");

        new MerkleTree(contents).traverseTreeNodes();
    }
}