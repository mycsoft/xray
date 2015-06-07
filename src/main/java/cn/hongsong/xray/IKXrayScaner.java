/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.xray;

import cn.hongsong.xray.util.TfIdfAlgorithm;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import net.sf.json.JSONObject;

/**
 * IK分词扫描器. 使用ik分词模型进行向量的扫描.
 *
 * @author MaYichao
 */
public class IKXrayScaner implements XrayScaner {

    @Override
    public String scan(String txt) {
        Map<String, Integer> xrays = doScan(txt);
        return mapToString(xrays);
    }

    @Override
    public Double checkSame(String x1, String x2) {
        return calculateCos2(new LinkedHashMap<String, Integer>(stringToMap(x1)),
                new LinkedHashMap<String, Integer>(stringToMap(x2)));
    }

    private Map<String, Integer> doScan(String txt) {
        return TfIdfAlgorithm.segStr(txt);
    }

    /**
     * 将map格式的向量改为String.
     *
     * @param map
     * @return
     */
    private static String mapToString(Map<String, Integer> map) {
        //去除分词中的null字符.
        Queue<String> nullKeys = new ArrayDeque<String>();
        for (String name : map.keySet()) {
            if (name.toUpperCase().equals("NULL")) {
                nullKeys.add(name);
            }
        }
        while (nullKeys.size() > 0) {
            String name = nullKeys.poll();
            map.remove(name);

        }
        JSONObject j = JSONObject.fromObject(map);
        return j.toString();
    }

    /**
     * 将map格式的向量改为String.
     *
     * @param map
     * @return
     */
    private static Map<String, Integer> stringToMap(String js) {
        JSONObject j = JSONObject.fromObject(js);
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : (Set<Map.Entry<String, Integer>>) j.entrySet()) {
            map.put(entry.getKey(), entry.getValue());

        }
//        for (int i = 0; i < j.size(); i++) {
//            JSONObject row = j.getJSONObject(i);
//        }
        return map;
    }

    private static Double cosSimilarityByString2(String first, String second) {
        try {
            Map<String, Integer> firstTfMap = TfIdfAlgorithm.segStr(first);
            Map<String, Integer> secondTfMap = TfIdfAlgorithm.segStr(second);
//			if(firstTfMap.size() < secondTfMap.size()){
//				Map<String, Integer> temp=firstTfMap;
//				firstTfMap=secondTfMap;
//				secondTfMap=temp;
//			}
            return calculateCos2((LinkedHashMap<String, Integer>) firstTfMap, (LinkedHashMap<String, Integer>) secondTfMap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0d;
    }

    private static Double calculateCos2(LinkedHashMap<String, Integer> first, LinkedHashMap<String, Integer> second) {
        if (first.size() < second.size()) {
            LinkedHashMap<String, Integer> temp = first;
            first = second;
            second = temp;
        }

        List<Map.Entry<String, Integer>> firstList = new ArrayList<Map.Entry<String, Integer>>(first.entrySet());
        List<Map.Entry<String, Integer>> secondList = new ArrayList<Map.Entry<String, Integer>>(second.entrySet());
        //计算相似度  
        double vectorFirstModulo = 0.00;//向量1的模  
        double vectorSecondModulo = 0.00;//向量2的模  
        double vectorProduct = 0.00; //向量积  
        int secondSize = second.size();
        for (int i = 0; i < firstList.size(); i++) {
            vectorFirstModulo += firstList.get(i).getValue().doubleValue() * firstList.get(i).getValue().doubleValue();
            if (i < secondSize) {
                vectorSecondModulo += secondList.get(i).getValue().doubleValue() * secondList.get(i).getValue().doubleValue();
                String key = secondList.get(i).getKey();
                if (first.get(key) != null) {
                    vectorProduct += first.get(key).doubleValue() * second.get(key).doubleValue();
                }
            }
        }

//		for(Entry<String, Integer> entry : first.entrySet()) {
//        	String key = entry.getKey();
//        	vectorFirstModulo += entry.getValue().doubleValue() * entry.getValue().doubleValue();
//        	if(second.get(key) != null) {
//    			vectorProduct += entry.getValue().doubleValue()*second.get(key).doubleValue();
//    		}
//        }
//        
//        for(Entry<String, Integer> entry : second.entrySet()) {
//        	vectorSecondModulo+=entry.getValue().doubleValue()*entry.getValue().doubleValue();
//        }
        return vectorProduct / (Math.sqrt(vectorFirstModulo) * Math.sqrt(vectorSecondModulo));
    }

}
