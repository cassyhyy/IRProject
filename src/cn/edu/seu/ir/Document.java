/**
 * �ĵ���
 */
package cn.edu.seu.ir;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Document {
    public int id;//�ĵ����
    public int words_num;//�������ʸ���
    public Map<BigramModel,Integer> sequence;//����ĵ���Bi-gram���е�Ƶ��
    public Map<String,Integer> terms;//����term��Ӧ��Ƶ


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
