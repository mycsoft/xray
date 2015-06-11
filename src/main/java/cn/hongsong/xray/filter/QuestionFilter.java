/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.xray.filter;

import cn.hongsong.xray.ResultOutputer;
import java.util.List;

/**
 * 题目过滤网.
 * @author MaYichao
 */
public interface QuestionFilter<T> {
    /**
     * 返回结果.
     * @return 
     */
    List<T> getResult();
    /**
     * 设置源.
     * @param source 
     */
    void setSource(List<T> source);
    /**
     * 返回源对象.
     * @return
     */
    List<T> getSource();
    /**
     * 开闸.开始进行过滤.
     */
    void switchOn();
    /**
     * 加入结果生成器.
     * @param out 
     */
    void setOutput(ResultOutputer<T> out);
    /**
     * 取得结果输出器.
     * @return 
     */
    ResultOutputer<T> getOutput();
}
