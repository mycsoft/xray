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
 * 异步分组题目过滤器.
 * 本类是对分组题目过滤器{@link GroupQuestionFilter}的异步封装.
 * @author MaYichao
 */
public class AsynGroupQuestionFilter extends SimpleQuestionFilter implements Runnable {

    private GroupQuestionFilter parent;

    public AsynGroupQuestionFilter(List<Map<String, String>> allQ, XrayScaner scaner, GroupQuestionFilter parent) {

        this.allQ = allQ;
        this.scaner = scaner;
        this.parent = parent;
    }

    @Override
    public void run() {
        scan();
        //通知完成.
        parent.onScanComplete(this);
    }

}
