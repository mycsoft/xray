/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.xray.filter;

import cn.hongsong.xray.XrayScaner;
import java.util.List;
import java.util.Map;

/**
 * 就近扫描器. 采用就近原则.只与最近的指定数量的的内容进行对比. 本扫描器,可用于原内容在扫描前已经经过排序.
 *
 * @author MaYichao
 */
public class NearQuestionFilter extends SimpleQuestionFilter {

    int near = 1000;

    public NearQuestionFilter(List<Map<String, String>> allQ, XrayScaner scaner) {
        super(allQ, scaner);
    }

    @Override
    protected boolean isGood(Map<String, String> row, List<Map<String, String>> goodQ, XrayScaner scaner) {
        int size = goodQ.size();

        if (size > near) {
            size = near;
            goodQ = goodQ.subList(0, size);
        }

        return super.isGood(row, goodQ, scaner); //To change body of generated methods, choose Tools | Templates.
    }

}
