package cn.edu.seu.ir;

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque; 
import java.util.Deque;
import java.util.HashSet;
import java.util.Collections;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import cn.edu.seu.word.Stemming;
import cn.edu.seu.storage.*;


//集合运算，包括2个term的AND\OR，以及多个term的AND\OR
public class SetOperation {
	public SetOperation(){
	}

	
	//2个term的AND操作，返回Term
	public static Term andMerge(Term t1, Term t2){
		Map<Integer,Integer>m1 = t1.map;
		Map<Integer,Integer>m2 = t2.map;
		Map<Integer,Integer>result = new TreeMap<Integer,Integer>();

		Iterator<Map.Entry<Integer,Integer>> it1 = m1.entrySet().iterator();
		Iterator<Map.Entry<Integer,Integer>> it2 = m2.entrySet().iterator();

		Entry<Integer,Integer> e1 = it1.next();
		Entry<Integer,Integer> e2 = it2.next();

		int docs = 0, sum = 0;
		while(e1 != null && e2 != null)
		{
			//System.out.println("比较："+e1.getKey()+","+e2.getKey());//测试
			
			if(e1.getKey() == e2.getKey()){
				result.put(e1.getKey(), e1.getValue()+e2.getValue());//put文档编号以及出现次数（注意这里的出现次数代表2个term在该文档分别出现次数之和）
				//result.put(e2.getKey(), e2.getValue());
				docs++;
				sum += e1.getValue();
				sum += e2.getValue();
				e1 = it1.hasNext() ? it1.next() : null;
				e2 = it2.hasNext() ? it2.next() : null;
			}
			else if(e1.getKey() < e2.getKey())
				e1= it1.hasNext() ? it1.next() : null;
			else 
				e2= it2.hasNext() ? it2.next() : null;
		}
		return new Term(t1.term+" AND "+t2.term,docs,sum,result);
	}
	
	
	//2个以上term的AND操作，返回Term
	public static Term andListMerge(List<Term>terms){
		if(terms.size() <= 2)
			return null;
		
		Collections.sort(terms); //先按词频降序排列
		
		Term t1 = terms.get(0);
		Term t2 = terms.get(1);
		Term result = andMerge(t1,t2);
		
		for(int i=2;i<terms.size();i++)
			result = andMerge(result,terms.get(i));
		
		return result;
	}
	
	
	//2个term的OR操作，返回Term
	public static Term orMerge(Term t1, Term t2){
		Map<Integer,Integer>m1 = t1.map;
		Map<Integer,Integer>m2 = t2.map;
		Map<Integer,Integer>result = new TreeMap<Integer,Integer>();

		Iterator<Map.Entry<Integer,Integer>> it1 = m1.entrySet().iterator();
		Iterator<Map.Entry<Integer,Integer>> it2 = m2.entrySet().iterator();

		Entry<Integer,Integer> e1 = it1.next();
		Entry<Integer,Integer> e2 = it2.next();

		int docs = 0, sum = 0;
		while(e1 != null && e2 != null)
		{
			if(e1.getKey() == e2.getKey()){
				result.put(e1.getKey(), e1.getValue()+e2.getValue());
				//result.put(e2.getKey(), e2.getValue());
				docs++;
				sum += e1.getValue();
				sum += e2.getValue();
				e1 = it1.hasNext() ? it1.next() : null;
				e2 = it2.hasNext() ? it2.next() : null;
			}
			else if(e1.getKey() < e2.getKey()){
				result.put(e1.getKey(), e1.getValue());
				docs++;
				sum += e1.getValue();
				e1 = it1.hasNext() ? it1.next() : null;
			}
			else{
				result.put(e2.getKey(), e2.getValue());
				docs++;
				sum += e2.getValue();
				e2 = it2.hasNext() ? it2.next() : null;
			}
		}
		
		
		if(e1!=null){
			result.put(e1.getKey(), e1.getValue());
			docs++;
			sum+=e1.getValue();
		}
		if(e2!=null){
			result.put(e2.getKey(), e2.getValue());
			docs++;
			sum+=e2.getValue();
		}
		//将剩余元素put进去
		while(it1.hasNext()){
			e1=it1.next();
			result.put(e1.getKey(), e1.getValue());
			docs++;
			sum += e1.getValue();
		}
		while(it2.hasNext()){
			e2=it2.next();
			result.put(e2.getKey(), e2.getValue());
			docs++;
			sum += e2.getValue();
		}
			
		return new Term(t1.term+" OR "+t2.term,docs,sum,result);
	}
	
	
	//2个以上term的OR操作，返回Term
	public static Term orListMerge(List<Term>terms){
		if(terms.size() <= 2)
			return null;

		Collections.sort(terms); //先按词频降序排列

		Term t1 = terms.get(0);
		Term t2 = terms.get(1);
		Term result = orMerge(t1,t2);

		for(int i=2;i<terms.size();i++)
			result = orMerge(result,terms.get(i));

		return result;
	}
	
	
	//2个term的AND NOT操作，返回Term
	public static Term andnotMerge(Term t1, Term t2){
		Map<Integer,Integer>m1 = t1.map;
		Map<Integer,Integer>m2 = t2.map;
		Map<Integer,Integer>result = new TreeMap<Integer,Integer>();

		Iterator<Map.Entry<Integer,Integer>> it1 = m1.entrySet().iterator();
		Iterator<Map.Entry<Integer,Integer>> it2 = m2.entrySet().iterator();
		
		Entry<Integer,Integer> e1 = it1.next();
		Entry<Integer,Integer> e2 = it2.next();

		int docs = 0, sum = 0;
		while(e1 != null && e2 != null)
		{
			if(e1.getKey() == e2.getKey()){
				e1 = it1.hasNext() ? it1.next() : null;
				e2 = it2.hasNext() ? it2.next() : null;
			}
			else if(e1.getKey() < e2.getKey()){
				result.put(e1.getKey(), e1.getValue());
				docs++;
				sum += e1.getValue();
				e1 = it1.hasNext() ? it1.next() : null;
			}
			else
				e2 = it2.hasNext() ? it2.next() : null;
		}
		
		
		if(e1!=null){
			result.put(e1.getKey(), e1.getValue());
			docs++;
			sum+=e1.getValue();
		}
		//将term1中剩余元素put进去
		while(it1.hasNext()){
			e1=it1.next();
			result.put(e1.getKey(), e1.getValue());
			docs++;
			sum += e1.getValue();
		}
		
		return new Term(t1.term+" AND NOT "+t2.term,docs,sum,result);
	}
	
	
	//AND NOT操作逻辑上可能还有问题
	//2个以上term的AND NOT操作，返回Term
	public static Term andnotListMerge(List<Term>terms){
		if(terms.size() <= 2)
			return null;

		//Collections.sort(terms); //先按词频降序排列,andnot不需排序

		Term t1 = terms.get(0);
		Term t2 = terms.get(1);
		Term result = andnotMerge(t1,t2);

		for(int i=2;i<terms.size();i++)
			result = andnotMerge(result,terms.get(i));

		return result;
	}
}
