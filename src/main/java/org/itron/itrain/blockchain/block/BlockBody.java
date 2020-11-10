package org.itron.itrain.blockchain.block;

import lombok.Data;

import java.util.List;

/**
 * 区块 body，存放交易的数组
 *
 * @author Shadowalker
 */
@Data
public class BlockBody {
    private List<ContentInfo> contentInfos;
}
