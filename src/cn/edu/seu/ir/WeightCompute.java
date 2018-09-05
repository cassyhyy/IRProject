/**
 * Ȩֵ����
 * ��Ҫʵ�ּ���ÿ��Term��idf,��ÿ���ĵ��е�Ȩֵwij
 */
package cn.edu.seu.ir;


import cn.edu.seu.Global;
import cn.edu.seu.storage.Database;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class WeightCompute {
    private int n; //���Ͽ��ĵ�����Ŀ
    private Map<Integer,Integer> words; //���ÿƪ�ĵ���Ӧ�����ִʳ���֮��

    public WeightCompute(int n){
        this.n = n;
        words = new TreeMap<Integer, Integer>();
        words = Global.database.getWords();
    }

    //�õ�term��idf
    public double getIDF(Term t){
        return Math.log10((double)n / t.docs);
    }

    //�õ�term��tfij
    public int getTf(Term t, int j){
        return t.map.get(j);
    }

    //�õ�term��Ȩֵwij��δ��һ����
    public double getWij(Term t, int j){
        return (double)getTf(t,j) * getIDF(t);
    }

    //�õ�term��wid
    public double getWid(Term t, int j){
        return (1+Math.log10((double)getTf(t,j)));
    }

    //�õ�term��Ȩֵwij����һ����
    public double getWijAfter(Term t, int j){
        return getWid(t,j)*getIDF(t);
    }

    //ͨ�����Ҽ���2���ĵ�֮������ƶȣ�δ��һ����
    public double getSIM(int doc1, int doc2){
        List<Term> t1 = Global.database.getTerms(doc1);
        List<Term> t2 = Global.database.getTerms(doc2);

        int i=0,j=0;
        double n= 0.0,d=0.0;//���ӣ���ĸ

        //�������
        while(i<t1.size() && j<t2.size()){
            String s1 = t1.get(i).term;
            String s2 = t2.get(j).term;
            if(s1.equals(s2)){
                n += getWij(t1.get(i),doc1)*getWij(t2.get(j),doc2);
                i++;
                j++;
            }else if(s1.compareTo(s2) < 0){
                //s1��s2С
                i++;
            }else{
                j++;
            }
        }

        //�����ĸ
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

    //ͨ�����Ҽ���2���ĵ������ƶȣ���һ����
    public double getSIMAfter(int doc1, int doc2){
        List<Term> t1 = Global.database.getTerms(doc1);
        List<Term> t2 = Global.database.getTerms(doc2);

        int i=0,j=0;
        double n= 0.0,d=0.0;//���ӣ���ĸ

        //�������
        while(i<t1.size() && j<t2.size()){
            String s1 = t1.get(i).term;
            String s2 = t2.get(j).term;
            if(s1.equals(s2)){
                n += getWijAfter(t1.get(i),doc1)*getWijAfter(t2.get(j),doc2);
                i++;
                j++;
            }else if(s1.compareTo(s2) < 0){
                //s1��s2С
                i++;
            }else{
                j++;
            }
        }

        //�����ĸ
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
