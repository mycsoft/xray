/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.xray.filter;

import cn.hongsong.xray.ResultOutputer;
import cn.hongsong.xray.XrayScaner;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 简单默认过滤器.
 *
 * @author MaYichao
 */
public class SimpleQuestionFilter implements QuestionFilter<Map<String, String>> {

    /**
     * 结果输出器.
     */
    ResultOutputer<Map<String, String>> output;
    /**
     * 过滤后保留的题目结果.
     */
    protected List<Map<String, String>> goodQ = new ArrayList<Map<String, String>>();
    protected List<Map<String, String>> allQ;
    protected XrayScaner scaner;

    public SimpleQuestionFilter() {
    }

    public SimpleQuestionFilter(List<Map<String, String>> allQ, XrayScaner scaner) {
        this.allQ = allQ;
        this.scaner = scaner;
    }

    protected void scan() {
        //分出优选题库与重复题库.
        if (allQ.isEmpty()) {
            return;
        }
        long t = System.currentTimeMillis();
        int c = 0;
        for (Map<String, String> q : allQ) {
            c++;
            if (isGood(q, goodQ, scaner)) {
                goodQ.add(q);
            } else {
//                     badQ.add(q);
            }
            if (c % 500 == 0) {
                System.out.println("============" + c);
            }
        }
        t = System.currentTimeMillis() - t;
        t = t / allQ.size();
        System.out.println("tttt=" + t);
    }

    /**
     * 检查是否是优选.
     *
     * @param row
     * @param goodQ
     * @param scaner
     * @return
     */
    protected boolean isGood(Map<String, String> row, List<Map<String, String>> goodQ, XrayScaner scaner) {
        String x1 = row.get("x");
        for (Map<String, String> good : goodQ) {
            //计算相似度.
            double d = scaner.checkSame(x1, good.get("x"));
            if (d > 0.7) {
//                 System.out.println("----------------------------------------------------------");
//                 System.out.println("相似度"+ d);
//                 System.out.println(String.format("[%s]\t|\t%s", row.get("id"),row.get("text")));
//                 System.out.println(String.format("[%s]\t|\t%s", good.get("id"),good.get("text")));
                return false;
            }
        }
        return true;
    }

    /**
     * 过滤后保留的题目结果.
     *
     * @return the goodQ
     */
    public List<Map<String, String>> getGoodQ() {
        return goodQ;
    }

    /**
     * 过滤后保留的题目结果.
     *
     * @param goodQ the goodQ to set
     */
    public void setGoodQ(List<Map<String, String>> goodQ) {
        this.goodQ = goodQ;
    }

    /**
     * @return the allQ
     */
    public List<Map<String, String>> getAllQ() {
        return allQ;
    }

    /**
     * @param allQ the allQ to set
     */
    public void setAllQ(List<Map<String, String>> allQ) {
        this.allQ = allQ;
    }

    /**
     * @return the scaner
     */
    public XrayScaner getScaner() {
        return scaner;
    }

    /**
     * @param scaner the scaner to set
     */
    public void setScaner(XrayScaner scaner) {
        this.scaner = scaner;
    }

    @Override
    public List<Map<String, String>> getResult() {
        return getGoodQ();
    }

    @Override
    public void setSource(List<Map<String, String>> source) {
        setAllQ(source);
    }

    @Override
    public void switchOn() {
        scan();
    }

    @Override
    public List<Map<String, String>> getSource() {
        return getAllQ();
    }

    @Override
    public void setOutput(ResultOutputer<Map<String, String>> out) {
        output = out;
    }

    @Override
    public ResultOutputer<Map<String, String>> getOutput() {
        return output;
    }

}
