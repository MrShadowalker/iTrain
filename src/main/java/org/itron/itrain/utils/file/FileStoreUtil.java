package org.itron.itrain.utils.file;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.itron.itrain.utils.merkle.SimpleMerkleTree;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 模拟区块链文件存储方式：Guava 版本
 *
 * @author Shadowalker
 */
@Slf4j
public class FileStoreUtil {

    // 定义区块链文件大小
    private static final int FILE_SIZE = 1024; // KB

    /**
     * 将文件内容写入目标文件：Guava 方式
     *
     * @param targetFileName
     * @param content
     */
    public static void writeIntoTargetFile(String targetFileName, String content) {
        File newFile = new File(targetFileName);
        try {
            Files.write(content.getBytes(), newFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将文件内容向后追加写入目标文件：FileWriter 方式
     *
     * @param targetFileName
     * @param content
     */
    public static void appendToTargetFile(String targetFileName, String content) {
        try {
            // 打开一个写文件器，构造函数中的第二个参数 true，表示以追加形式写文件
            FileWriter writer = new FileWriter(targetFileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将文件内容向后追加写入目标文件：Guava 方式
     *
     * @param targetFileName
     * @param content
     */
    public static void appendToTargetFileByGuava(String targetFileName, String content) {
        File file = new File(targetFileName);
        try {
            Files.append(content, file, Charsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 模拟区块链内容写入文件
     *
     * @param content
     */
    public static void writeIntoFile(String content) {
        try {
            // 查看当前目录下是否有正在写入的 logging 文件，有则继续使用，无则创建
            File root = new File(".//blockchainLog//");
            //如果文件夹不存在则创建
            if (!root.exists() && !root.isDirectory()) {
                log.info("/blockchainLog 不存在，创建 /blockchainLog ……");
                root.mkdir();
            } else {
                log.info("/blockchainLog 目录存在，无需创建。");
            }
            // 获取当前文件夹下的所有文件
            File[] files = root.listFiles();
            if (files == null) {
                // 如果根目录下没有任何文件则创建新的文件
                String targetFileName = ".//blockchainLog//blockchain-" + System.currentTimeMillis() + ".logging";
                appendToTargetFileByGuava(targetFileName, content);
                return;
            }
            // 如果根目录下有文件则寻找是否存在后缀为 logging 的文件
            boolean has = false;
            for (File file : files) {
                String name = file.getName();
                if (name.endsWith(".logging") && name.startsWith("blockchain-")) {
                    // 有则继续写入
                    log.info(file.getPath());
                    appendToTargetFileByGuava(file.getPath(), content);
                    has = true;

                    // 写入后如果文件大小超过固定大小，则将 logging 后缀转为 log 后缀
                    if (file.length() >= FILE_SIZE) {
                        String logPath = file.getPath().replace("logging", "log");
                        File log = new File(logPath);
                        Files.copy(file, log);
                        // 删除已经写满的 logging 文件
                        file.delete();
                    }
                }
            }
            // 无则创建新的文件
            if (!has) {
                String targetFileName = ".//blockchainLog//blockchain-" + System.currentTimeMillis() + ".logging";
                appendToTargetFileByGuava(targetFileName, content);
                return;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * 模拟区块链内容的写入
     */
    public static void writeIntoBlockFile() {
        List<String> list = new ArrayList<>();
        list.add("AI");
        list.add("BlockChain");
        for (int i = 0; i < 20; i++) {
            list.add(generateVCode(6));
            writeIntoFile(SimpleMerkleTree.getTreeNodeHash(list) + "\n");
        }
    }

    /**
     * 根据 length 长度生成数字符串
     *
     * @param length
     * @return
     */
    private static String generateVCode(int length) {
        Long vCode = new Double((Math.random() + 1) * Math.pow(10, length - 1)).longValue();
        return vCode.toString();
    }

    public static void main(String[] args) {
        writeIntoBlockFile();
    }
}
