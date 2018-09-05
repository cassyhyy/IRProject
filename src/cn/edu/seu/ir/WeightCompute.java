/**
 * 权值计算
 * 主要实现计算每个Term的idf,在每个文档中的权值wij
 */
package cn.edu.seu.ir;


import cn.edu.seu.Global;
import cn.edu.seu.storage.Database;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WeightCompute {
    private int n; //语料库文档总数目
    private Map<Integer,Integer> words; //存放每篇文档对应所有字词出现之和

    public WeightCompute(int n){
        this.n = n;
        words = new TreeMap<Integer, Integer>();
        words = Global.database.getWords();
    }

    //得到term的idf
    public double getIDF(Term t){
        return Math.log10((double)n / t.docs);
    }

    //得到term的tfij
    public int getTf(Term t, int j){
        return t.map.get(j);
    }

    //得到term的权值wij（未归一化）
    public double getWij(Term t, int j){
        return (double)getTf(t,j) * getIDF(t);
    }

    //得到term的wid
    public double getWid(Term t, int j){
        return (1+Math.log10((double)getTf(t,j)));
    }

    //得到term的权值wij（归一化后）
    public double getWijAfter(Term t, int j){
        return getWid(t,j)*getIDF(t);
    }

    //通过余弦计算2个文档之间的相似度（未归一化）
    public double getSIM(int doc1, int doc2){
        List<Term> t1 = Global.database.getTerms(doc1);
        List<Term> t2 = Global.database.getTerms(doc2);

        int i=0,j=0;
        double n= 0.0,d=0.0;//分子，分母

        //计算分子
        while(i<t1.size() && j<t2.size()){
            String s1 = t1.get(i).term;
            String s2 = t2.get(j).term;
            if(s1.equals(s2)){
                n += getWij(t1.get(i),doc1)*getWij(t2.get(j),doc2);
                i++;
                j++;
            }else if(s1.compareTo(s2) < 0){
                //s1比s2小
                i++;
            }else{
                j++;
            }
        }

        //计算分母
        double d1=0.0,d2=0.0;
        for(Term t : t1){
            d1+=getWij(t,doc1)*getWij(t,doc1);
        }
        d1 = Math.sqrt(d1);

        for(Term t : t2){
            d2+=getWij(t,doc2)*getWij(t,doc2);
        }
        d2 = Math.sqrt(d2);
        d = d1*d2;

        return n/d;
    }

    //通过余弦计算2个文档的相似度（归一化后）
    public double getSIMAfter(int doc1, int doc2){
        List<Term> t1 = Global.database.getTerms(doc1);
        List<Term> t2 = Global.database.getTerms(doc2);

        int i=0,j=0;
        double n= 0.0,d=0.0;//分子，分母

        //计算分子
        while(i<t1.size() && j<t2.size()){
            String s1 = t1.get(i).term;
            String s2 = t2.get(j).term;
            if(s1.equals(s2)){
                n += getWijAfter(t1.get(i),doc1)*getWijAfter(t2.get(j),doc2);
                i++;
                j++;
            }else if(s1.compareTo(s2) < 0){
                //s1比s2小
                i++;
            }else{
                j++;
            }
        }

        //计算分母
        double d1=0.0,d2=0.0;
        for(Term t : t1){
            d1+=getWijAfter(t,doc1)*getWijAfter(t,doc1);
        }
        d1 = Math.sqrt(d1);

        for(Term t : t2){
            d2+=getWijAfter(t,doc2)*getWijAfter(t,doc2);
        }
        d2 = Math.sqrt(d2);
        d = d1*d2;

        return n/d;
    }
}
