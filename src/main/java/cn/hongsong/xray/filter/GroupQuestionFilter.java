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
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 分组题目过滤器.
 *
 * @author MaYichao
 */
public class GroupQuestionFilter extends SimpleQuestionFilter {

    long start = 0;
    int threadCount = 3;
    int groupSize = 200;
    int scanerCount = 0;
    int hasComplete = 0;
    int hasMerged = 0;
    Vector<QuestionFilter> subScaners = null;
    Vector<MergeGroupQuestionFilter> mergeScaners = null;
    //再分组上限组数.
    int groupCountMax = 5;
    int goodCount = 0;

    GroupQuestionFilter parent = null;

    public GroupQuestionFilter() {
    }

    public GroupQuestionFilter(List<Map<String, String>> allQ, XrayScaner scaner) {
        super(allQ, scaner);
    }

    @Override
    protected void scan() {
        start = System.currentTimeMillis();
        int total = allQ.size();
        if (total > groupSize) {
            //分解为多个子线程.每个1000个题目.
            int count = total / groupSize;
            if (total % groupSize > 0) {
                count++;
            }
            scanerCount = count;
            subScaners = new Vector<QuestionFilter>(count);
            System.out.println(String.format("共%s条记录,需要拆分为到%s个分赛场,进行海选.", total, count));
            for (int i = 0; i < count; i++) {
                int size = groupSize;
                if (i == count - 1) {
                    //最后一个.
                    int t = total % groupSize;
                    if (t > 0) {
                        size = t;
                    }
                }
                List<Map<String, String>> subQ = allQ.subList(i * groupSize, i * groupSize + size);
                AsynGroupQuestionFilter ss = new AsynGroupQuestionFilter(subQ, scaner, this);
                subScaners.add(ss);
            }
            System.out.println(String.format("分组完毕,各分赛场开始比赛.同时进行%s场比赛.", threadCount));
            //启动线程池.
//                ThreadGroup threadGroup = new ThreadGroup("scaner");
//                Thread[] ts = new Thread[threadCount];
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            for (QuestionFilter ss : subScaners) {
                pool.execute((Runnable) ss);
//                    Future<AsynGroupQuestionFilter> f = pool.submit((Callable<AsynGroupQuestionFilter>)ss);

            }
            pool.shutdown();

            //合并各个优选做新的全集.
            //计算最后优选集.
            //分出优选题库与重复题库.
//            for (Map<String, String> q : allQ) {
//                if (isGood(q, goodQ, scaner)) {
//                    goodQ.add(q);
//                } else {
////                     badQ.add(q);
//                }
//            }
        } else {
            super.scan(); //To change body of generated methods, choose Tools | Templates.
            outputResult();
        }

    }

    /**
     * 监听分会场完成海选.
     *
     * @param scaner
     */
    public synchronized void onScanComplete(QuestionFilter<Map<String, String>> scaner) {

        hasComplete++;
        List<Map<String, String>> result = scaner.getResult();
        List<Map<String, String>> src = scaner.getSource();
        goodCount += result.size();

        System.out.println("完成" + (hasComplete * 100 / scanerCount) + "%");
        System.out.println(String.format("总共%d条,优选%d条,排除率%s", src.size(), result.size(), 100 - (result.size() * 100) / src.size() + "%"));
        if (hasComplete == scanerCount) {
            mergScaner();
        }
    }

    /**
     * 监听子分组完成海选.
     *
     * @param scaner
     */
    private synchronized void onSubGroupScanComplete(QuestionFilter scaner) {

//            goodCount += scaner.goodQ.size();
//            System.out.println("完成" + (hasComplete * 100 / scanerCount) + "%");
        goodQ = scaner.getResult();
//            System.out.println(String.format("总共%d条,优选%d条,排除率%s", scaner.allQ.size(), scaner.goodQ.size(), 100 - (scaner.goodQ.size() * 100) / scaner.allQ.size() + "%"));
//            if (hasComplete == scanerCount) {
//                mergScaner();
//            }
        outputResult();
    }

    private void mergScaner() {
        //当分组太多时再分成小组处理.
        if (subScaners.size() > groupCountMax) {
            int c = subScaners.size() / groupCountMax;
            if (subScaners.size() % groupCountMax > 0) {
                c++;
            }
            mergeScaners = new Vector<MergeGroupQuestionFilter>(c);
            for (int i = 0; i < c; i++) {
                int s = groupCountMax;
                if (i == c - 1) {
                    s = subScaners.size() % groupCountMax;
                }
                MergeGroupQuestionFilter ms = new MergeGroupQuestionFilter(subScaners.subList(i * groupCountMax, i * groupCountMax + s), scaner);
                mergeScaners.add(ms);
            }
            System.out.println("=================================================================");
            System.out.println(String.format("总共%s组%s记录,拆分为%s个组集合,每组包含%s个单元.", subScaners.size(), goodCount, c, groupCountMax));
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);
            for (MergeGroupQuestionFilter ms : mergeScaners) {
                pool.execute(ms);
            }
            pool.shutdown();
        } else {
            goodQ = mergScan(subScaners, scaner);
            outputResult();
        }
    }

    /**
     * 输出结果信息.
     */
    private void outputResult() {
        System.out.println("优先:" + goodQ.size());
        System.out.println("排除:" + (allQ.size() - goodQ.size()));
        System.out.println(String.format("本轮用时:%1$tM:%1$tS:%1$tL", (System.currentTimeMillis() - start)));
//            System.out.println("============== 优先题库 ===================");

        if (parent == null) {
            if (output != null) {
                output.onCompleted(goodQ);
            } else {
                System.exit(0);
            }
        } else {
            parent.onSubGroupScanComplete(this);
        }
    }

    private synchronized void onMergeComplete(MergeGroupQuestionFilter scaner) {
        hasMerged++;
        if (hasMerged == mergeScaners.size()) {
            for (MergeGroupQuestionFilter ms : mergeScaners) {
                goodQ.addAll(ms.getResult());
            }
            if (goodQ.size() > groupCountMax * groupSize && goodQ.size() * 100 / allQ.size() < 80) {
                System.out.println("======================== 中国好题目 <加时赛>  ==================================");
                System.out.println(String.format("当前还有%s条题目,轮共排除了%s的重复内容.还有必要再进行一轮海选.", goodQ.size(), goodQ.size() * 100 / allQ.size() + "%"));
                //内容过大,且还有缩小的空间(本次缩小了20%).
                //再次分组.
//                    hasComplete = 0;
//                    scanerCount = 1;
                GroupQuestionFilter groupScaner = new GroupQuestionFilter(goodQ, this.scaner);
                groupScaner.parent = this;
                groupScaner.scan();
            } else {
                //进行最后一次比对.
                long start2 = System.currentTimeMillis();
                System.out.println(String.format("下级赛场用时%1$tM:%1$tS:%1$tL", start2 - start));
                if (parent == null) {
                    System.out.println("======================== 中国好题目<总决赛> ==================================");
                } else {
                    System.out.println("======================== 中国好题目<半决赛> ==================================");
                }
                System.out.println(String.format("进行PK,参赛人数%s", goodQ.size()));
                NearQuestionFilter simpleScaner = new NearQuestionFilter(goodQ, this.scaner);
                simpleScaner.scan();
                System.out.println(String.format("决赛场用时%1$tM:%1$tS:%1$tL", System.currentTimeMillis() - start2));
                goodQ = simpleScaner.goodQ;
                outputResult();

            }
        }
    }

    private List<Map<String, String>> mergScan(List<QuestionFilter> subList, XrayScaner scaner) {
        //将子会场的优选合并为主场的新的全集.
        ArrayList<Map<String, String>> all2 = new ArrayList<Map<String, String>>();
        for (QuestionFilter sub : subList) {
            all2.addAll(sub.getResult());
        }
        if (all2.isEmpty()) {
            return new ArrayList<Map<String, String>>();
        }
        System.out.println(String.format("优选了%d条", all2.size()));
        SimpleQuestionFilter simpleScaner = new SimpleQuestionFilter();
        simpleScaner.setSource(all2);
        simpleScaner.scaner = scaner;
        simpleScaner.scan();
        return simpleScaner.goodQ;
    }

    class MergeGroupQuestionFilter extends SimpleQuestionFilter implements Runnable {

        List<QuestionFilter> subList;
//            List<Map<String,String>> goodQ;

        public MergeGroupQuestionFilter(List<QuestionFilter> subList, XrayScaner scaner) {
            super(null, scaner);
            this.subList = subList;
        }

        @Override
        public void run() {
            goodQ = mergScan(subList, GroupQuestionFilter.this.scaner);
            onMergeComplete(this);
        }

    }

}
