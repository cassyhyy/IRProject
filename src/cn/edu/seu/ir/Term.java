package cn.edu.seu.ir;

import cn.edu.seu.Global;

import java.util.*;

public class Term implements Comparable<Term>{
	private static WeightCompute w = new WeightCompute(Global.doc_sum);//static对象，便于计算term的权值
	//private static LanguageModel lm = new LanguageModel(Global.doc_sum);

	public String term;//存放term的名称（即单词）
	public int docs;//存放该term在多少个文档中存在
	public int sum;//存放该term在所有文档中出现的次数
	public Map<Integer,Integer> map;//存放该term在文档i中出现的次数（map中有docs个对象）
	public double idf;//存放term的idf值
	public Map<Integer,Double> weight;//存放term在出现文档i中对应的权值（未归一化）
	public Map<Integer,Double> weight2;//存放term在出现文档i中对应的权值（归一化后）
	//public Map<Integer,Double> probility;//存放term在文档i中对应的生成概率
	//public double probilityAll;//存放term在文档集中的生成概率
	//public Map<Integer,Double> p;//存放term在文档i中对应的生成概率（平滑处理后）

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
	
	//按照词频降序排列
	@Override
	public int compareTo(Term t) {  
        int s = t.sum - this.sum;//先按照词频排序  
        if(s == 0){  
            return this.term.compareTo(t.term);//如果词频相等再用String升序排序  
        }  
        return s;  
    }  
}
