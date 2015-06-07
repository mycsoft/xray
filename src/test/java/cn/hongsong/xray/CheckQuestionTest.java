/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.xray;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author MaYichao
 */
public class CheckQuestionTest {

    public CheckQuestionTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    public static void main(String[] args) throws Exception {
        new CheckQuestionTest().findGoodQuestions();
//        System.out.println(String.format"(%1$tM:%1$tS:%1$tL", new Long(00001l)));
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    /**
     * 测试找出不重复的题库.
     */
    @Test
    public void findGoodQuestions() throws Exception {
        System.out.println("test findGoodQuestions");
        int MAX = 200;
        long start = System.currentTimeMillis();
        Connection connection = null;
        XrayScaner scaner = new IKXrayScaner();
        try {
            //查询数据库.
//             connection = DriverManager.getConnection("jdbc:mysql://192.168.10.113:3306/hs?zeroDateTimeBehavior=convertToNull", "root", "123456");
            connection = DriverManager.getConnection("jdbc:mysql://61.155.238.81:3306/hongsong.cn?zeroDateTimeBehavior=convertToNull", "hongsong.cn", "tj8Mxf44dAP5BNnP");
            connection.setReadOnly(true);

//             ResultSet count = connection.prepareCall("select count(1) from t_z_270e818e9e384f37884df6534f04b937_question where orginal_id is null limit 0 ," + MAX).executeQuery();
            ResultSet count = connection.prepareCall("select count(1) from t_z_5ECB539B2C4A4C4397DB1188F2118143_question where content is not null and orginal_id is null limit 0 ," + MAX).executeQuery();
            count.next();
            int total = count.getInt(1);
//            int total = 200;
            System.out.println("total:" + MAX + "/" + total);
            List<Map<String, String>> allQ = new ArrayList<Map<String, String>>(total);
            List<Map<String, String>> goodQ = new ArrayList<Map<String, String>>(total);
            List<Map<String, String>> badQ = new ArrayList<Map<String, String>>(total);
            System.out.println("==============================================================================");
            System.out.println("======================== 中国好题目 盛大开幕 ==================================");
            System.out.println("==============================================================================");
            for (int i = 0; i < total / MAX + (total % MAX > 0 ? 1 : 0); i++) {
//             ResultSet result = connection.prepareCall("select * from t_z_270e818e9e384f37884df6534f04b937_question where orginal_id is null limit  " + i * MAX + " ," + MAX).executeQuery();
                ResultSet result = connection.prepareCall("select * from t_z_5ECB539B2C4A4C4397DB1188F2118143_question where content is not null and orginal_id is null order by content desc limit " + i * MAX + " ," + MAX).executeQuery();
                //根据题干生成xray
                while (result.next()) {
                    //缓存在队列中.
                    Map<String, String> row = new HashMap<String, String>();
                    row.put("id", result.getString("id"));
                    Blob b = result.getBlob("content");
                    String t = blobToString(b);
                    row.put("text", t);
                    allQ.add(row);

                }
                System.out.println((i + 1) * 100.0 * MAX / total + "%");
//                connection.
                connection.close();
                connection = DriverManager.getConnection("jdbc:mysql://61.155.238.81:3306/hongsong.cn?zeroDateTimeBehavior=convertToNull", "hongsong.cn", "tj8Mxf44dAP5BNnP");
            }

            connection.close();
            connection = null;
            System.out.println(String.format("数据载入用时%1$tM:%1$tS:%1$tL", System.currentTimeMillis() - start));
            System.out.println(String.format("原始数据载入完毕,共%s条.开始为每位选手编号.", allQ.size()));
            for (Map<String, String> q : allQ) {
                //批量测试队列中的向量.
                String t = q.get("text");
                try {
                    q.put("x", scaner.scan(t));
                } catch (Exception ex) {
                    System.out.println("解析失败:" + ex.getLocalizedMessage());
                    System.out.println("解析内容:" + t);
                    ex.printStackTrace();
                    continue;
                }

            }
            System.out.println("编号完成,开始海选.");
            GroupScaner groupScaner = new GroupScaner();
            groupScaner.allQ = allQ;
            groupScaner.scaner = scaner;
            groupScaner.scan();

//             for (Map<String, String> good : goodQ) {
//                 System.out.println(String.format("[%s]\t|\t%s", good.get("id"),good.get("text")));
//             }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private String blobToString(Blob blob) {
        InputStream in = null;
        try {
            in = blob.getBinaryStream();
            BufferedInputStream bi = new BufferedInputStream(in);
            StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[1024];
            while (bi.read(buffer) > -1) {
                sb.append(new String(buffer, "utf-8"));
            }
            return sb.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        }
    }

//    /**
//     * 检查是否是优选.
//     *
//     * @param row
//     * @param goodQ
//     * @param scaner
//     * @return
//     */
//    private boolean isGood(Map<String, String> row, List<Map<String, String>> goodQ, XrayScaner scaner) {
//        String x1 = row.get("x");
//        for (Map<String, String> good : goodQ) {
//            //计算相似度.
//            double d = scaner.checkSame(x1, good.get("x"));
//            if (d > 0.7) {
////                 System.out.println("----------------------------------------------------------");
////                 System.out.println("相似度"+ d);
////                 System.out.println(String.format("[%s]\t|\t%s", row.get("id"),row.get("text")));
////                 System.out.println(String.format("[%s]\t|\t%s", good.get("id"),good.get("text")));
//                return false;
//            }
//        }
//        return true;
//    }
    /**
     * 海选分会场.
     */
    class SubScaner extends SimpleScaner implements Runnable{

        GroupScaner parent;

        public SubScaner(List<Map<String, String>> allQ, XrayScaner scaner, GroupScaner parent) {

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

    /**
     * 默认扫描器.
     */
    class SimpleScaner {

        protected List<Map<String, String>> goodQ = new ArrayList<Map<String, String>>();
        protected List<Map<String, String>> allQ;
        protected XrayScaner scaner;

        public SimpleScaner() {
        }

        public SimpleScaner(List<Map<String, String>> allQ, XrayScaner scaner) {
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

    }

    /**
     * 就近扫描器. 采用就近原则.只与最近的指定数量的的内容进行对比. 本扫描器,可用于原内容在扫描前已经经过排序.
     */
    class NearScaner extends SimpleScaner {

        int near = 1000;

        public NearScaner(List<Map<String, String>> allQ, XrayScaner scaner) {
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

    /**
     * 分组扫描器.
     */
    class GroupScaner extends SimpleScaner {

        long start = 0;
        int threadCount = 3;
        int groupSize = 200;
        int scanerCount = 0;
        int hasComplete = 0;
        int hasMerged = 0;
        Vector<SimpleScaner> subScaners = null;
        Vector<MergeScaner> mergeScaners = null;
        //再分组上限组数.
        int groupCountMax = 5;
        int goodCount = 0;

        GroupScaner parent = null;

        public GroupScaner() {
        }

        public GroupScaner(List<Map<String, String>> allQ, XrayScaner scaner) {
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
                subScaners = new Vector<SimpleScaner>(count);
                System.out.println(String.format("共%s条记录,需要拆分为到%s个分赛场,进行海选.", total, count));
                for (int i = 0; i < count; i++) {
                    int size = groupSize;
                    if (i == count - 1) {
                        //最后一个.
                        size = total % groupSize;
                    }
                    List<Map<String, String>> subQ = allQ.subList(i * groupSize, i * groupSize + size);
                    SubScaner ss = new SubScaner(subQ, scaner, this);
                    subScaners.add(ss);
                }
                System.out.println(String.format("分组完毕,各分赛场开始比赛.同时进行%s场比赛.", threadCount));
                //启动线程池.
//                ThreadGroup threadGroup = new ThreadGroup("scaner");
//                Thread[] ts = new Thread[threadCount];
                ExecutorService pool = Executors.newFixedThreadPool(threadCount);
                for (SimpleScaner ss : subScaners) {
                    pool.execute((Runnable) ss);
//                    Future<SubScaner> f = pool.submit((Callable<SubScaner>)ss);

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
                printResult();
            }

        }

        /**
         * 监听分会场完成海选.
         *
         * @param scaner
         */
        private synchronized void onScanComplete(SimpleScaner scaner) {

            hasComplete++;
            goodCount += scaner.goodQ.size();
            System.out.println("完成" + (hasComplete * 100 / scanerCount) + "%");
            System.out.println(String.format("总共%d条,优选%d条,排除率%s", scaner.allQ.size(), scaner.goodQ.size(), 100 - (scaner.goodQ.size() * 100) / scaner.allQ.size() + "%"));
            if (hasComplete == scanerCount) {
                mergScaner();
            }
        }

        /**
         * 监听子分组完成海选.
         *
         * @param scaner
         */
        private synchronized void onSubGroupScanComplete(SimpleScaner scaner) {

//            goodCount += scaner.goodQ.size();
//            System.out.println("完成" + (hasComplete * 100 / scanerCount) + "%");
            goodQ = scaner.goodQ;
//            System.out.println(String.format("总共%d条,优选%d条,排除率%s", scaner.allQ.size(), scaner.goodQ.size(), 100 - (scaner.goodQ.size() * 100) / scaner.allQ.size() + "%"));
//            if (hasComplete == scanerCount) {
//                mergScaner();
//            }
            printResult();
        }

        private void mergScaner() {
            //当分组太多时再分成小组处理.
            if (subScaners.size() > groupCountMax) {
                int c = subScaners.size() / groupCountMax;
                if (subScaners.size() % groupCountMax > 0) {
                    c++;
                }
                mergeScaners = new Vector<MergeScaner>(c);
                for (int i = 0; i < c; i++) {
                    int s = groupCountMax;
                    if (i == c - 1) {
                        s = subScaners.size() % groupCountMax;
                    }
                    MergeScaner ms = new MergeScaner(subScaners.subList(i * groupCountMax, i * groupCountMax + s), scaner);
                    mergeScaners.add(ms);
                }
                System.out.println("=================================================================");
                System.out.println(String.format("总共%s组%s记录,拆分为%s个组集合,每组包含%s个单元.", subScaners.size(), goodCount, c, groupCountMax));
                ExecutorService pool = Executors.newFixedThreadPool(threadCount);
                for (MergeScaner ms : mergeScaners) {
                    pool.execute(ms);
                }
                pool.shutdown();
            } else {
                goodQ = mergScan(subScaners, scaner);
                printResult();
            }
        }

        private void printResult() {
            System.out.println("优先:" + goodQ.size());
            System.out.println("排除:" + (allQ.size() - goodQ.size()));
            System.out.println(String.format("本轮用时:%1$tM:%1$tS:%1$tL", (System.currentTimeMillis() - start)));
//            System.out.println("============== 优先题库 ===================");

            if (parent == null) {
                System.exit(0);
            } else {
                parent.onSubGroupScanComplete(this);
            }
        }

        private synchronized void onMergeComplete(MergeScaner scaner) {
            hasMerged++;
            if (hasMerged == mergeScaners.size()) {
                for (MergeScaner ms : mergeScaners) {
                    goodQ.addAll(ms.goodQ);
                }
                if (goodQ.size() > groupCountMax * groupSize && goodQ.size() * 100 / allQ.size() < 80) {
                    System.out.println("======================== 中国好题目 <加时赛>  ==================================");
                    System.out.println(String.format("当前还有%s条题目,轮共排除了%s的重复内容.还有必要再进行一轮海选.", goodQ.size(), goodQ.size() * 100 / allQ.size() + "%"));
                    //内容过大,且还有缩小的空间(本次缩小了20%).
                    //再次分组.
//                    hasComplete = 0;
//                    scanerCount = 1;
                    GroupScaner groupScaner = new GroupScaner(goodQ, this.scaner);
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
                    NearScaner simpleScaner = new NearScaner(goodQ, this.scaner);
                    simpleScaner.scan();
                    System.out.println(String.format("决赛场用时%1$tM:%1$tS:%1$tL", System.currentTimeMillis() - start2));
                    goodQ = simpleScaner.goodQ;
                    printResult();

                }
            }
        }

        private List<Map<String, String>> mergScan(List<SimpleScaner> subList, XrayScaner scaner) {
            //将子会场的优选合并为主场的新的全集.
            ArrayList<Map<String, String>> all2 = new ArrayList<Map<String, String>>();
            for (SimpleScaner sub : subList) {
                all2.addAll(sub.goodQ);
            }
            if (all2.isEmpty()) {
                return new ArrayList<Map<String, String>>();
            }
            System.out.println(String.format("优选了%d条", all2.size()));
            SimpleScaner simpleScaner = new SimpleScaner();
            simpleScaner.allQ = all2;
            simpleScaner.scaner = scaner;
            simpleScaner.scan();
            return simpleScaner.goodQ;

//                printResult();
        }

        class MergeScaner extends SimpleScaner implements Runnable {

            List<SimpleScaner> subList;
//            List<Map<String,String>> goodQ;

            public MergeScaner(List<SimpleScaner> subList, XrayScaner scaner) {
                super(null, scaner);
                this.subList = subList;
            }

            @Override
            public void run() {
                goodQ = mergScan(subList, GroupScaner.this.scaner);
                onMergeComplete(this);
            }

        }

    }

}
