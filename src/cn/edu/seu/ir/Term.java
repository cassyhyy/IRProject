package cn.edu.seu.ir;

import cn.edu.seu.Global;

import java.util.*;

public class Term implements Comparable<Term>{
	private static WeightCompute w = new WeightCompute(Global.doc_sum);//static���󣬱��ڼ���term��Ȩֵ
	//private static LanguageModel lm = new LanguageModel(Global.doc_sum);

	public String term;//���term�����ƣ������ʣ�
	public int docs;//��Ÿ�term�ڶ��ٸ��ĵ��д���
	public int sum;//��Ÿ�term�������ĵ��г��ֵĴ���
	public Map<Integer,Integer> map;//��Ÿ�term���ĵ�i�г��ֵĴ�����map����docs������
	public double idf;//���term��idfֵ
	public Map<Integer,Double> weight;//���term�ڳ����ĵ�i�ж�Ӧ��Ȩֵ��δ��һ����
	public Map<Integer,Double> weight2;//���term�ڳ����ĵ�i�ж�Ӧ��Ȩֵ����һ����
	//public Map<Integer,Double> probility;//���term���ĵ�i�ж�Ӧ�����ɸ���
	//public double probilityAll;//���term���ĵ����е����ɸ���
	//public Map<Integer,Double> p;//���term���ĵ�i�ж�Ӧ�����ɸ��ʣ�ƽ�������

	public Term(String t, int d, int s, Map<Integer,Integer>m){
		term=t;
		docs=d;
		sum=s;
		map = new TreeMap<Integer,Integer>();
		map=m;

		idf=w.getIDF(this);
		weight = new TreeMap<Integer,Double>();
		setWeight();
		weight2 = new TreeMap<Integer, Double>();
		setWeightAfter();
		/*probility = new TreeMap<Integer,Double>();
		setProbility();
		probilityAll = setProbilityAll();
		p = new TreeMap<Integer,Double>();
		setProbilityAfter();*/
	}


	public void setWeight(){
		for(Map.Entry<Integer, Integer> entry : map.entrySet()) {
			int j = entry.getKey();
			weight.put(j, w.getWij(this,j));
		}
	}

	public void setWeightAfter(){
		for(Map.Entry<Integer, Integer> entry : map.entrySet()) {
			int j = entry.getKey();
			weight2.put(j, w.getWijAfter(this,j));
		}
	}
	
	/*public void setProbility() {
		for(Map.Entry<Integer, Integer> entry:map.entrySet()) {
			int j = entry.getKey();
			probility.put(j, lm.getProbility(this,j));
		}
	}
	
	public double setProbilityAll() {
		return lm.getProbilityAll(this);
	}

	public void setProbilityAfter() {
		for(Map.Entry<Integer, Integer> entry:map.entrySet()) {
			int j = entry.getKey();
			p.put(j, lm.LILM(this,j));
		}
	}*/
	
	//���մ�Ƶ��������
	@Override
	public int compareTo(Term t) {  
        int s = t.sum - this.sum;//�Ȱ��մ�Ƶ����  
        if(s == 0){  
            return this.term.compareTo(t.term);//�����Ƶ�������String��������  
        }  
        return s;  
    }  
}
