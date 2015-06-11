/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.xray;

import cn.hongsong.xray.filter.GroupQuestionFilter;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主扫描器.
 *
 * @author MaYichao
 */
public class ScanerMain {

    public static void main(String[] args) throws Exception {
//        findGoodQuestions();
//        System.out.println(String.format"(%1$tM:%1$tS:%1$tL", new Long(00001l)));
//        outputResult(getAllGoods());
        syncQuestion(getAllGoods());
    }

    /**
     * 连接到源数据库.
     *
     * @return
     * @throws SQLException
     */
    private static Connection connectToSource() throws SQLException {
//             connection = DriverManager.getConnection("jdbc:mysql://192.168.10.113:3306/hs?zeroDateTimeBehavior=convertToNull", "root", "123456");
        Connection connection = DriverManager.getConnection("jdbc:mysql://61.155.238.81:3306/hongsong.cn?zeroDateTimeBehavior=convertToNull", "hongsong.cn", "tj8Mxf44dAP5BNnP");
        connection.setReadOnly(true);
        return connection;
    }

    /**
     * 连接到结果数据库.
     *
     * @return
     * @throws SQLException
     */
    private static Connection connectToResult() throws SQLException {
//             connection = DriverManager.getConnection("jdbc:mysql://192.168.10.113:3306/hs?zeroDateTimeBehavior=convertToNull", "root", "123456");
        Connection connection = DriverManager.getConnection("jdbc:mysql://192.168.10.113:3306/xray?characterEncoding=UTF-8", "root", "123456");
//        connection.setReadOnly(true);
        return connection;
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    /**
     * 测试找出不重复的题库.
     */
    public static void findGoodQuestions() throws Exception {
        int MAX = 200;
        long start = System.currentTimeMillis();
        Connection connection = null;
        XrayScaner scaner = new IKXrayScaner();
        try {
            //查询数据库.
            connection = connectToSource();
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
                System.out.println("" + (i * 100 / total) + "%");
                connection = connectToSource();
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
            GroupQuestionFilter groupScaner = new GroupQuestionFilter();
            groupScaner.setSource(allQ);
            groupScaner.setScaner(scaner);
            groupScaner.setOutput(new ResultOutputer<Map<String, String>>() {

                @Override
                public void onCompleted(List<Map<String, String>> result) {
                    try {
                        //将结果保存到数据库.
                        outputResult(result);
                    } catch (SQLException ex) {
                        System.out.println("save result failed!");
                        ex.printStackTrace();
                    }
                }
            });
            groupScaner.switchOn();

//             for (Map<String, String> good : goodQ) {
//                 System.out.println(String.format("[%s]\t|\t%s", good.get("id"),good.get("text")));
//             }
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private static String blobToString(Blob blob) {
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

    /**
     * 格式化文本用于SQL脚本. 主要是将"'"换为"\\'".
     *
     * @param s
     * @return
     */
    private static String formatToSql(String s) {
        return s.replaceAll("'", "\\'");
    }

    private static void outputResult(List<Map<String, String>> result) throws SQLException {
        long start = System.currentTimeMillis();
        Connection resultConn = null;
        try {
            //查询数据库.
//             connection = DriverManager.getConnection("jdbc:mysql://192.168.10.113:3306/hs?zeroDateTimeBehavior=convertToNull", "root", "123456");
            resultConn = connectToResult();

            //将结果id保存到数据库中
            saveResult(resultConn, result);

            resultConn.close();
            resultConn = null;

//             for (Map<String, String> good : goodQ) {
//                 System.out.println(String.format("[%s]\t|\t%s", good.get("id"),good.get("text")));
//             }
        } finally {
            if (resultConn != null) {
                resultConn.close();
            }
        }

        syncQuestion(result);
        System.out.println(String.format("结果保存用时%1$tM:%1$tS:%1$tL", System.currentTimeMillis() - start));
    }

    /**
     * 同步题目结果集.
     *
     * @param result
     */
    private static void syncQuestion(List<Map<String, String>> result) throws SQLException {
        long start = System.currentTimeMillis();
        Connection resultConn = null;
        Connection srcConn = null;
        try {
            //查询数据库.
//             connection = DriverManager.getConnection("jdbc:mysql://192.168.10.113:3306/hs?zeroDateTimeBehavior=convertToNull", "root", "123456");
            resultConn = connectToResult();

            srcConn = connectToSource();
//            connection.setReadOnly(true);

//             ResultSet count = connection.prepareCall("select count(1) from t_z_270e818e9e384f37884df6534f04b937_question where orginal_id is null limit 0 ," + MAX).executeQuery();
            //将优选题目保存到数据库中
            System.out.println("======================== 中国好题目 结果导入 ==================================");
            String update = "update question set content = '%s' where id = '%s'";
            int c = 0;

            for (Map<String, String> q : result) {
                c++;
                //每100次重置一下远程数据库连接.
                if (c % 100 == 0) {
                    srcConn.close();
                    System.out.println("" + (c * 100 / result.size()) + "%");
                    srcConn = connectToSource();

                }
                String id = q.get("id");
//               ResultSet result = connection.prepareCall("select * from t_z_270e818e9e384f37884df6534f04b937_question where orginal_id is null limit  " + i * MAX + " ," + MAX).executeQuery();
                ResultSet question = srcConn.prepareCall(String.format("select * from t_z_5ECB539B2C4A4C4397DB1188F2118143_question where id='%s'", id)).executeQuery();
                //根据题干生成xray
                while (question.next()) {
                    String content = formatToSql(blobToString(question.getBlob("content")));
                    try {
                        resultConn.prepareStatement(
                                String.format(update, content,
                                        id)).executeUpdate();
//                    } catch (MySQLSyntaxErrorException sQLException) {
//                        System.out.println(String.format("导入题目[%s]失败:\n{%s}",id,content));
//                        throw sQLException;
                    } catch (SQLException sQLException) {
                        System.out.println(String.format("导入题目[%s]失败:\n{%s}", id, content));
                        throw sQLException;
                    }
                }

//                connection.
            }

            resultConn.close();
            resultConn = null;
            srcConn.close();
            srcConn = null;
            System.out.println(String.format("结果保存用时%1$tM:%1$tS:%1$tL", System.currentTimeMillis() - start));

//             for (Map<String, String> good : goodQ) {
//                 System.out.println(String.format("[%s]\t|\t%s", good.get("id"),good.get("text")));
//             }
        } finally {
            if (srcConn != null) {
                srcConn.close();
            }
            if (resultConn != null) {
                resultConn.close();
            }
        }

    }

    /**
     * 保存过滤后的题目ID
     *
     * @param result
     */
    private static void saveResult(Connection conn, List<Map<String, String>> result) throws SQLException {
        String sql = "insert into question (id) values ('%s')";
        for (Map<String, String> q : result) {
            conn.prepareCall(String.format(sql, q.get("id"))).executeUpdate();
        }
//        conn.commit();
    }

    /**
     * 从结果数据库中,取出所有的优选数据库.
     *
     * @return
     * @throws SQLException
     */
    private static List<Map<String, String>> getAllGoods() throws SQLException {
        long start = System.currentTimeMillis();
        Connection resultConn = null;
        try {
            //查询数据库.
//             connection = DriverManager.getConnection("jdbc:mysql://192.168.10.113:3306/hs?zeroDateTimeBehavior=convertToNull", "root", "123456");
            resultConn = connectToResult();

            //将优选题目保存到数据库中
            System.out.println("find all good questions");
            //只查询出没有题干的内容.
            String find = "select * from question where content is null";
            int c = 0;
            ResultSet goods = resultConn.prepareStatement(find).executeQuery();
            List<Map<String, String>> list = new ArrayList<Map<String, String>>();
            while (goods.next()) {
                Map<String, String> row = new HashMap<String, String>();
                row.put("id", goods.getString("id"));
                list.add(row);
            }

            resultConn.close();
            resultConn = null;
            System.out.println(String.format("结果保存用时%1$tM:%1$tS:%1$tL", System.currentTimeMillis() - start));
            return list;

//             for (Map<String, String> good : goodQ) {
//                 System.out.println(String.format("[%s]\t|\t%s", good.get("id"),good.get("text")));
//             }
        } finally {
            if (resultConn != null) {
                resultConn.close();
            }
        }
    }
}
