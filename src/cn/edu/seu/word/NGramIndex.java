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


//ͨ��N-gram�������������ڽ���ͨ���
public class NGramIndex {
	private Map<String,List<String>> grams;
	
	public NGramIndex(List<Term> terms, int ng){
		grams = new HashMap<String,List<String>>();
		for(Term t : terms){
			String[] g = formGrams(t.term,ng);
			for(String gram : g){
				if(grams.containsKey(gram)){
					//�������gram���ڣ��򽫸ô���ӵ���Ӧ�������
					List<String> l = grams.get(gram);
					l.add(t.term);
					l.sort(new ListComparator());//���ַ�����������
					grams.replace(gram, l);
				}else{
					//��һ���������gram
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
	
	//��N-gram����������B+���в�����
	public BplusTree getGramTree(){
		BplusTree b = new BplusTree(6);
		
		Iterator<Map.Entry<String,List<String>>> it= grams.entrySet().iterator();
		while(it.hasNext()){
			Entry<String,List<String>> e = it.next();
			b.insertOrUpdate(e.getKey(), e.getValue());
		}
		
		return b;
	}
	
	
	//��ԭ��N-gram�ֳ�n��һ�Σ��������飬ngһ��ȡ2
	public static String[] formGrams(String text, int ng){
		//ԭ�ʳ���
		int len = text.length();
		//�ַ�������(����Ϊԭ�ʳ��ȼ�ȥN�ĳ����ټ�3)
		String[] res = new String[len - ng + 3];

		res[0] = "$"+text.substring(0,ng-1);//�Ƚ�$..���������һ��

		for (int i = 0; i < len-ng+2-1; i++) {
			//��ȡԭ��
			res[i+1] = text.substring(i, i+ng);
		}

		res[len-ng+2] = text.substring(len-ng+1,len)+"$";//����ٽ�ĩβ$��������

		return res;
	}

	
	//��ԭ��N-gram�ֳ�n��һ�Σ���������ͷ��β��$���������飬ngһ��ȡ2
	public static String[] formGramsWithoutD(String text, int ng){
		//һ���ַ�����Ҫ������a-z��ɳ���Ϊ2��gram
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
		
		//ԭ�ʳ���
		int len = text.length();
		//�ַ�������(����Ϊԭ�ʳ��ȼ�ȥN�ĳ����ټ�1)
		String[] res = new String[len - ng + 1];
		
		for (int i = 0; i < len-ng+1; i++) {
			//��ȡԭ��
			res[i] = text.substring(i, i+ng);
		}
		return res;
	}

	
	//���ԣ�������
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
		
		
		
		String s = "$ch";//�ȷָ�
		for(String f : formGramsWithoutD(s,2)){
			System.out.println(f);
		}
		
		List<String> result = new ArrayList<String>();//��Ž��
		int first = 0;
		int index = 0;
		
		
		
		//���ԣ���������
		System.out.println("ͨ���ƥ������");
		for(String r : result){
			System.out.print(r+",");
		}
		System.out.println("\n");
		
		
		
	}
}


//�Ƚ������࣬����list���ַ�����������
class ListComparator implements Comparator<String>{
  @Override
  public int compare(String str1, String str2) {
      return str1.compareTo(str2);
  }
}