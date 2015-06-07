/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.xray;

/**
 * X射线扫描器. 用于检查数据内容的X向量值.
 *
 * @author MaYichao
 */
public interface XrayScaner {

    /**
     * 扫描一段文本.
     *
     * @param txt 要扫描的文本.
     * @return 文本X向量.
     */
    String scan(String txt);

    /**
     * 计算相似度.越大越相似.
     *
     * @param x1 向量1
     * @param x2 向量2
     * @return 相似度.完全相似为1.
     */
    Double checkSame(String x1, String x2);

}
