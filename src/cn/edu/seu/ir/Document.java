/**
 * 文档类
 */
package cn.edu.seu.ir;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Document {
    public int id;//文档编号
    public int words_num;//所含单词个数
    public Map<BigramModel,Integer> sequence;//存放文档中Bi-gram序列的频率
    public Map<String,Integer> terms;//所含term对应词频


    public Document(int i, int s, Map<BigramModel,Integer> se, Map<String,Integer> t){
        id = i;
        words_num = s;
        sequence = new HashMap<BigramModel,Integer>();
        sequence = se;
        terms = new TreeMap<String,Integer>();
        terms = t;
    }

    public Document(int i, int s, Map<BigramModel,Integer> se){
        id = i;
        words_num = s;
        sequence = new HashMap<BigramModel,Integer>();
        sequence = se;
        terms = new TreeMap<String,Integer>();
    }

}
