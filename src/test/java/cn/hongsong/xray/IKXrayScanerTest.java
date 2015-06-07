/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.xray;

import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.wltea.analyzer.cfg.Configuration;
import org.wltea.analyzer.cfg.DefaultConfig;
import org.wltea.analyzer.dic.Dictionary;

/**
 *
 * @author MaYichao
 */
public class IKXrayScanerTest {
    
    public IKXrayScanerTest() {
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

    /**
     * Test of scan method, of class IKXrayScaner.
     */
    @org.junit.Test
    public void testScan() {
        System.out.println("scan");
        DefaultConfig df = (DefaultConfig)DefaultConfig.getInstance();
//        URL in = Dictionary.class.getResource(df.getMainDictionary());
//        URL in = Dictionary.class.getResource("/");
//        Dictionary d = Dictionary.initial(df);
        
        
//        String txt = "Java中怎样判断一个字符串是否是数字";
        String txt = "CDMA2000数据业务使用中，终端会在dormant状态、null状态、active状态之间跃迁。";
        IKXrayScaner instance = new IKXrayScaner();
        String expResult = "";
//        Map<String, Integer> result = instance.scan(txt);
        String result = instance.scan(txt);
//        assertEquals(expResult, result);
//        for (Map.Entry<String, Integer> entry : result.entrySet()) {
//            String key = entry.getKey();
//            Integer value = entry.getValue();
//            System.out.println(key + "|" + value);
//        }
        System.out.println(result);
    }

    /**
     * Test of checkSame method, of class IKXrayScaner.
     */
    @Test
    public void testCheckSame() {
        System.out.println("checkSame");
        String txt1 = "Java中怎样判断一个字符串是否是中文";
//        String txt1 = "Java中怎样判断一个字符串是否是数字";
//        String txt1 = "sdfsafsa";
        String txt2 = "Java中怎样判断一个字符串是否是中文";
        IKXrayScaner instance = new IKXrayScaner();
        String x1 = instance.scan(txt1);
        String x2 = instance.scan(txt2);
        Double expResult = null;
        Double result = instance.checkSame(x1, x2);
        assertEquals(expResult, result);
        
    }

    
    
}
