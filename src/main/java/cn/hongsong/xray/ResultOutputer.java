/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cn.hongsong.xray;

import java.util.List;

/**
 * 结果输出器.
 * @author MaYichao
 */
public interface ResultOutputer<T> {
    
    void onCompleted(List<T> result);
}
