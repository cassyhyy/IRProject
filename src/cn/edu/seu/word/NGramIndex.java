package cn.edu.seu.word;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import cn.edu.seu.ir.Term;
import cn.edu.seu.storage.BplusTree;


//通过N-gram建立索引，便于解析通配符
public class NGramIndex {
	private Map<String,List<String>> grams;
	
	public NGramIndex(List<Term> terms, int ng){
		grams = new HashMap<String,List<String>>();
		for(Term t : terms){
			String[] g = formGrams(t.term,ng);
			for(String gram : g){
				if(grams.containsKey(gram)){
					//已有这个gram存在，则将该次添加到对应的链表后
					List<String> l = grams.get(gram);
					l.add(t.term);
					l.sort(new ListComparator());//按字符串升序排序
					grams.replace(gram, l);
				}else{
					//第一次遇到这个gram
					List<String> l = new ArrayList<String>();
					l.add(t.term);
					grams.put(gram, l);
				}
			}
		}
	}
	
	
	public Map<String,List<String>> getGrams(){
		return grams;
	}
	
	//将N-gram索引保存在B+树中并返回
	public BplusTree getGramTree(){
		BplusTree b = new BplusTree(6);
		
		Iterator<Map.Entry<String,List<String>>> it= grams.entrySet().iterator();
		while(it.hasNext()){
			Entry<String,List<String>> e = it.next();
			b.insertOrUpdate(e.getKey(), e.getValue());
		}
		
		return b;
	}
	
	
	//将原词N-gram分成n个一段，返回数组，ng一般取2
	public static String[] formGrams(String text, int ng){
		//原词长度
		int len = text.length();
		//字符串数组(长度为原词长度减去N的长度再加3)
		String[] res = new String[len - ng + 3];

		res[0] = "$"+text.substring(0,ng-1);//先将$..放在数组第一个

		for (int i = 0; i < len-ng+2-1; i++) {
			//截取原词
			res[i+1] = text.substring(i, i+ng);
		}

		res[len-ng+2] = text.substring(len-ng+1,len)+"$";//最后再将末尾$放入数组

		return res;
	}

	
	//将原词N-gram分成n个一段，不包含开头结尾的$，返回数组，ng一般取2
	public static String[] formGramsWithoutD(String text, int ng){
		//一个字符，需要将其与a-z组成长度为2的gram
		if(text.length() == 1 && ng==2){
			String[] res = new String[52];
			for(int i=0; i<26; i++){
				res[i] = text+(char)(97+i);
			}
			for(int i=26; i<52; i++){
				res[i] = (char)(97-26+i)+text;
			}
			return res;
		}
		
		//原词长度
		int len = text.length();
		//字符串数组(长度为原词长度减去N的长度再加1)
		String[] res = new String[len - ng + 1];
		
		for (int i = 0; i < len-ng+1; i++) {
			//截取原词
			res[i] = text.substring(i, i+ng);
		}
		return res;
	}

	
	//测试！！！！
	public static void main(String[] args){
		List<Term> t = new ArrayList<Term>();
		Map<Integer,Integer> m = new HashMap<Integer,Integer>();
		m.put(1, 1);
		m.put(2, 2);
		t.add(new Term("abc",3,3,m));
		t.add(new Term("ab",3,2,m));
		t.add(new Term("bb",3,3,m));
		
		NGramIndex in = new NGramIndex(t,2);
		Map<String,List<String>> map = in.getGrams();
		
		
		Iterator<Map.Entry<String,List<String>>> it = map.entrySet().iterator();  
		while(it.hasNext()){  
			Map.Entry<String,List<String>> entry=it.next();  
			String key = entry.getKey();
			System.out.print(key+":");
			List<String> l = entry.getValue();
			l.sort(new ListComparator());
			for(String s : l)
				System.out.print(s+",");
			System.out.println("--------");
		}
		
		BplusTree b = in.getGramTree();
		
		
		
		String s = "$ch";//先分割
		for(String f : formGramsWithoutD(s,2)){
			System.out.println(f);
		}
		
		List<String> result = new ArrayList<String>();//存放结果
		int first = 0;
		int index = 0;
		
		
		
		//测试！！！！！
		System.out.println("通配符匹配结果：");
		for(String r : result){
			System.out.print(r+",");
		}
		System.out.println("\n");
		
		
		
	}
}


//比较容器类，用于list按字符串升序排序
class ListComparator implements Comparator<String>{
  @Override
  public int compare(String str1, String str2) {
      return str1.compareTo(str2);
  }
}