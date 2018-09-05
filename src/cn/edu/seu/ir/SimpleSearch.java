package cn.edu.seu.ir;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import cn.edu.seu.Global;
import cn.edu.seu.storage.BplusTree;
import cn.edu.seu.storage.Database;
import cn.edu.seu.word.NGramIndex;
import cn.edu.seu.word.Stemming;

public class SimpleSearch {
	public BplusTree termTree;//�������term���B+��
	private BplusTree gramTree;//�������term���Ӧgrams��B+����keyΪ��Ӧgram,valueΪ���и�gram����������ĵ�������
	private Map<String,List<String>> grams;
	
	//��ʼ�������������ݿ��ж�����terms�����뵽B+���С���terms����Bigram������Ӧ����
	public SimpleSearch(){
		List<Term> tlist = new ArrayList<Term>();
		
		//�����ݿ���һ����ȡ����������
		tlist = Global.database.select();

		//���뵽B+����
		termTree = new BplusTree(6);
		for(Term t : tlist)
			termTree.insertOrUpdate(t.term, t);
		
		NGramIndex gramIndex = new NGramIndex(tlist,2);
		grams = gramIndex.getGrams();
		gramTree = gramIndex.getGramTree();
	}


	//Ԥ���������û�������ʽת��Ϊ��׺���ʽ���ڴ���
	public static String preHandle(String str){
		str = str.toLowerCase();//��ת��ΪСд

		String result = null;//��Ŵ�����
		Stack<String> stack = new Stack<String>();//ջ  

		String temp = "";
		for(int i=0;i<str.length();i++){
			if(str.charAt(i) == '('){
				if(temp!=null && temp.length() >0){
					stack.push(temp);
					temp = "";
				}
				stack.push("(");
			}

			else if(str.charAt(i) == ')'){
				//���������ţ���ջ����Ӧ������
				//�����temp
				if(temp != null && temp.length() >0){
					//�ش��㷨.....
					Stemming s = new Stemming();
					temp = s.stem(temp);

					result += " ";
					result += temp;
					temp = "";
				}
				while(!stack.peek().equals("(")){
					result += " ";
					Stemming s = new Stemming();
					result += s.stem(stack.pop());
					//result += stack.pop();
				}
				stack.pop();//�������ų�ջ
			}else if(str.charAt(i)==' '){
				//�����ո񣬴���temp
				if(temp.equals("and") || temp.equals("or") || temp.equals("andnot")){
					//����������Ż���ջΪ�գ���ѹջ
					if(stack.isEmpty() || stack.contains("(")){
						stack.push(temp);
						temp = "";
					}
					else{
						result += " ";
						result += stack.pop();
						stack.push(temp);
						temp = "";
					}	
				}else{
					//���ǲ�������term
					//�ش��㷨.....
					Stemming s = new Stemming();
					if(temp != null && temp.length() > 0){
						result += " ";
						result += s.stem(temp);
						temp = "";
					}
				}
			}
			else{
				temp += str.charAt(i);
			}
		}

		//��ʣ���������term���
		if(temp != null && temp.length() > 0){
			result += " ";
			Stemming s = new Stemming();
			temp = s.stem(temp);
			result += temp;
		}
		while(!stack.isEmpty()){
			result += " ";
			Stemming s = new Stemming();
			result += s.stem(stack.pop());
		}

		result = result.replaceAll("null", "");
		result = result.substring(1, result.length());

		String[] words = result.split(" ");
		String before = null;//before����ǰһ������������һ��ɨ�����֪����Щ����ҪlistMerge������
		int index = -1;//���beforeǰһ������������λ�ã������޸�
		int num = 0;//���beforeǰһ���������Ѿ����ֹ�����


		//ɨ�账�������������ں�����1,2,3,....û�е���û�б��
		for(int i=0;i<words.length;i++){
			switch(words[i]){
			case "and":
				if(before != null && before.equals("and")){
					if(num == 0){
						//�ڶ����������֣�����һ������and1
						num++;//num==1
						words[index] = "AND"+num;
					}
					if(num > 0){
						num++;
						words[i] = "AND"+num;
					}
				}else{
					//ǰһ������������and
					num = 0;
				}
				words[i] = words[i].toUpperCase();
				index = i;
				before = "and";
				break;
			case "or":
				if(before != null && before.equals("or")){
					if(num == 0){
						//�ڶ����������֣�����һ������or1
						num++;//num==1
						words[index] = "OR"+num;
					}
					if(num > 0){
						num++;
						words[i] = "OR"+num;
					}
				}else{
					//ǰһ������������or
					num = 0;
				}
				words[i] = words[i].toUpperCase();
				index = i;
				before = "or";
				break;
			case "andnot":
				if(before != null && before.equals("andnot")){
					if(num == 0){
						//�ڶ����������֣�����һ������andnot1
						num++;//num==1
						words[index] = "ANDNOT"+num;
					}
					if(num > 0){
						num++;
						words[i] = "ANDNOT"+num;
					}
				}else{
					//ǰһ������������andnot
					num = 0;
				}
				words[i] = words[i].toUpperCase();
				index = i;
				before = "andnot";
				break;
			default:
				break;
			}
		}

		result = "";
		for(String w : words){
			result += " ";
			result += w;
		}

		result = result.replaceAll("null", "");
		result = result.substring(1, result.length());
		System.out.println("Ԥ�����"+result);

		return result;
	}

	
	//����and/or/andnot�������������ַ�����ͨ���������ϣ�û��ͨ������ַ����а����Ķ��������ĵ��ʣ�������Ӧ����������Term
	public Term search(String str){
		str = preHandle(str);//��Ԥ����
		Term result = null;
		int k = 0;//��ʾ�Ƿ���й���һ�ε�merge������Ϊ0�Ļ�ֱ�ӽ����ҵ���term�ֵ��result
		String[] words = str.split(" ");//�ո�ָ�
		Stack<Term> stack = new Stack<Term>();


		int and=0,or=0,andnot=0;
		for(int i=0;i<words.length;i++){
			if(words[i].contains("AND") && !words[i].contains("ANDNOT")){
				//and������
				if(or > 0 || andnot > 0){
					//�ж�֮ǰ�Ƿ���������or����andnot�����д���
					Term temp = null;
					if(!(i>0 && (words[i-1].contains("AND")|| words[i-1].contains("OR"))))
						temp = stack.pop();//�ȴ��ջ������
					List<Term> terms = new ArrayList<Term>();

					if(or > 0){
						for(int j=0;j<or;j++)
							terms.add(stack.pop());
						if(k==0){
							terms.add(stack.pop());
							result = SetOperation.orListMerge(terms);
							k=1;
						}else{
							terms.add(result);
							result = SetOperation.orListMerge(terms);
						}
					}else{
						for(int j=0;j<andnot;j++)
							terms.add(0,stack.pop());
						if(k==0){
							terms.add(0,stack.pop());
							result = SetOperation.andnotListMerge(terms);
							k=1;
						}else{
							terms.add(0,result);
							result = SetOperation.andnotListMerge(terms);
						}
					}
					if(temp != null)
						stack.push(temp);//push��ȥ��֮��Ҫ���������
					or = 0;
					andnot = 0;
				}
				if(words[i].length() > 3){
					and = words[i].charAt(3) - '0';//�ڼ���and��������	
				}else{
					//and��������������ջ����
					if(k == 0){
						Term t2 = stack.pop();
						Term t1 = stack.pop();
						result = SetOperation.andMerge(t1,t2);
						k=1;
					}else
						result = SetOperation.andMerge(result,stack.pop());
				}
			}else if(words[i].contains("OR")){
				//or������
				if(and > 0 || andnot > 0){
					//�ж�֮ǰ�Ƿ���������and����andnot�����д���
					Term temp = null;
					if(!(i>0 && (words[i-1].contains("AND")|| words[i-1].contains("OR"))))
						temp = stack.pop();//�ȴ��ջ������
					List<Term> terms = new ArrayList<Term>();

					if(and > 0){
						for(int j=0;j<and;j++)
							terms.add(stack.pop());
						if(k==0){
							terms.add(stack.pop());
							result = SetOperation.andListMerge(terms);
							k=1;
						}else{
							terms.add(result);
							result = SetOperation.andListMerge(terms);
						}
					}else{
						for(int j=0;j<andnot;j++)
							terms.add(0,stack.pop());
						if(k==0){
							terms.add(0,stack.pop());
							result = SetOperation.andnotListMerge(terms);
							k=1;
						}else{
							terms.add(0,result);
							result = SetOperation.andnotListMerge(terms);
						}
					}
					if(temp!=null)
						stack.push(temp);//push��ȥ��֮��Ҫ���������
					and = 0;
					andnot = 0;
				}
				if(words[i].length() > 2){
					or = words[i].charAt(2) - '0';//�ڼ���or��������	
				}else{
					//or��������������ջ����
					if(k == 0){
						Term t2 = stack.pop();
						Term t1 = stack.pop();
						//System.out.println("OR:"+t1.term+","+t2.term);
						result = SetOperation.orMerge(t1,t2);
						k=1;
					}else
						result = SetOperation.orMerge(result,stack.pop());
				}
			}else if(words[i].contains("ANDNOT")){
				if(or > 0 || and > 0){
					//�ж�֮ǰ�Ƿ���������or����and�����д���
					Term temp = null;
					if(!(i>0 && (words[i-1].contains("AND")|| words[i-1].contains("OR"))))
						temp = stack.pop();//�ȴ��ջ������
					List<Term> terms = new ArrayList<Term>();

					if(or > 0){
						for(int j=0;j<or;j++)
							terms.add(stack.pop());
						if(k==0){
							terms.add(stack.pop());
							result = SetOperation.orListMerge(terms);
							k=1;
						}else{
							terms.add(result);
							result = SetOperation.orListMerge(terms);
						}
					}else{
						for(int j=0;j<and;j++)
							terms.add(stack.pop());
						if(k==0){
							terms.add(stack.pop());
							result = SetOperation.andListMerge(terms);
							k=1;
						}else{
							terms.add(result);
							result = SetOperation.andListMerge(terms);
						}
					}
					if(temp!=null)
						stack.push(temp);//push��ȥ��֮��Ҫ���������
					or = 0;
					and = 0;
				}
				if(words[i].length() > 6){
					andnot = words[i].charAt(6) - '0';//�ڼ���and��������	
				}else{
					//System.out.println("ANDNOT:"+stack.peek().term);
					//and��������������ջ����
					if(k == 0){
						Term t2 = stack.pop();
						Term t1 = stack.pop();
						result = SetOperation.andnotMerge(t1,t2);
						k=1;
					}else{
						if(words[i-1].contains("AND") || words[i-1].contains("OR"))
							result = SetOperation.andnotMerge(stack.pop(),result);
						else
							result = SetOperation.andnotMerge(result,stack.pop());
					}

				}
			}else{
				//��ͨ��term
				if(termTree.get(words[i]) == null)
					return null;//û�и�term������Null���
				else
					stack.push((Term)termTree.get(words[i]));
			}
		}

		while(!stack.isEmpty()){
			List<Term> t = new ArrayList<Term>();
			if(and > 0){
				for(int j=0;j<and;j++)
					t.add(stack.pop());
				if(k==0){
					t.add(stack.pop());
					result = SetOperation.andListMerge(t);
					k=1;
				}else{
					t.add(result);
					result = SetOperation.andListMerge(t);
				}
			}else if(or > 0){
				for(int j=0;j<or;j++)
					t.add(stack.pop());
				if(k==0){
					t.add(stack.pop());
					result = SetOperation.orListMerge(t);
					k=1;
				}else{
					t.add(result);
					result = SetOperation.orListMerge(t);
				}
			}else{
				for(int j=0;j<andnot;j++)
					t.add(0,stack.pop());
				if(k==0){
					t.add(0,stack.pop());
					result = SetOperation.andnotListMerge(t);
					k=1;
				}else{
					t.add(0, result);
					result = SetOperation.andnotListMerge(t);
				}
			}
		}

		return result;
	}
	
	
	//����һ���û��������䣬�����ϲ���������List<Term>������������ͨ��������Կ��ܶ�Ӧ������ʣ������Term�����
	public List<Term> handle(String str){
		//str = preHandle(str);//��Ԥ����
		List<Term> result = new ArrayList<Term>();
		
		//����û��������䲻����and\or\andnot��������ֱ�ӶԸ�term����д���
		if( !(str.contains("and") || str.contains("or") || str.contains("andnot")) ){
			if(str.contains("*")){
				//����ͨ���
				List<String> s = wildcardHandle(str);
				for(String term : s){
					//���ԣ���������
					System.out.println("���ܵ�term:"+s);
					//�����뵽result��
					result.add((Term)termTree.get(term));
				}
				return result;
			}else{
				//������ͨ�����ֱ�ӷ���
				result.add((Term)termTree.get(str));
				return result;
			}
		}
		
		
		//����and/or/andnot������
		//������ͨ�����ֱ�ӽ�str���뵽search�����д���
		Map<Integer,List<String>> possible = new TreeMap<Integer,List<String>>();//���i,words[i]��term�����п��ܵĵ���
		
		if(!str.contains("*")){
			result.add(search(str));
			return result;
		}
		//����ͨ���
		String[] words = str.split(" ");
		for(int i=0; i<words.length; i++){
			if(words[i].contains("AND") || words[i].contains("OR"))
				continue;
			else if(words[i].contains("*")){
				//��ÿ������ͨ�����term���д���
				String word = words[i];
				List<String> l = wildcardHandle(words[i]);
				possible.put(i, l);
			}
		}
		
		int k = possible.size();
		String search = "";
		for(int i=0; i<words.length; i++){
			if(possible.containsKey(i)){
				List<String> l = possible.get(i);
				for(int j=0; j<l.size(); j++){
					if(words[i].contains("(")){
						int p = 0;
						while(words[i].charAt(p)=='('){
							search+="(";
							p++;
						}
					}
					if(j > 0)
						search += " or ";
					else
						search += " ";
					search += l.get(j);
					if(words[i].contains(")")){
						int p = words[i].length()-1;
						while(words[i].charAt(p)=='('){
							search+=")";
							p--;
						}
					}
				}
			}else{
				search += " ";
				search += words[i];
			}
		}
		if(search.charAt(0) == ' ')
			search = search.substring(1,search.length());
		
		System.out.println("ͨ���������������Ϊ��"+search);
		result.add(search(search));

		return result;
	}

	
	
	/**
	 * 1.*abc  $*abc$
	 * 	*�ڵ�һ��������Ҫ��$a��abc$
	 * 2.ab*c  $ab*c$
	 *  *����ĩβҲ���ڵ�һ��������Ҫ��$a�����ұ�����c$��$ab*c$
	 * 3.abc*  $abc*$
	 * 	*��ĩβ������Ҫ��$a������Ҫ��c$��$abc
	 * 4.a*bc*d  $a*bc*d$
	 * 	����Ҫ��$a,d$������Ҫ��$b,c$��$a*bc*d$
	 * 5.a*b*d   $a*b*d$
	 * 	����Ҫ��$a,d$��b��ab-bz֮�䣬$a*b*d$
	 * ������������������ͨ������ʿ�ͷ�ͽ�β����$��$*��*$������
	 */
	//�������ͨ����ĵ��ʣ��������п��ܵ��ʵ�һ��list
	public List<String> wildcardHandle(String str){
		String word = "$"+str+"$";//���ڿ�ͷ��β����$������֮����
		word = word.replace("$*", "");
		word = word.replace("*$", "");//����$*��*$
		String[] s = word.split("\\*");//�ȷָ�
		
		List<String> result = new ArrayList<String>();//��Ž��
		int first = 0;
		
		for(int x=0; x<s.length; x++){
			if(s[x] == null || s[x].length() <= 0){
				
			}else if(s[x].length() == 1){
				//�ַ��ָ�󳤶�Ϊ1
				String[] grams = NGramIndex.formGramsWithoutD(s[x],2);
				
				Set<String> temp = new TreeSet<String>();//���ظ�
				for(String g : grams){
					if((ArrayList<String>) gramTree.get(g)!=null){
						temp.addAll((ArrayList<String>) gramTree.get(g));
					}	
				}
				if(first == 0){
					//��һ����������result��Ϊ�գ��Ȱ�listȫ��add��ȥ
					result.addAll(temp);
					first = 1;
					continue;
				}
				
				int j=0;
				//��2��list�еĽ���
				List<String> r = new ArrayList<String>();//������ʱ��Ž��
				Iterator<String> it= temp.iterator();
				String k = it.next();
				while(j<result.size() && it.hasNext()){  
					if(result.get(j).equals(k)){
						r.add(result.get(j));
						j++;
						k = it.next();
					}
					else if(result.get(j).compareTo(k) < 0){
						//first�ַ�����temp���ַ���ҪС�����Ի����Լ����Ƚ�
						j++;
					}else{
						k = it.next();
					}
				}
				result.clear();
				result.addAll(r);
			}else{
				String[] grams = NGramIndex.formGramsWithoutD(s[x],2);
				
				for(int i=0; i<grams.length; i++){		
					if(first == 0){
						//��һ����������result��Ϊ�գ��Ȱ�listȫ��add��ȥ
						result.addAll((ArrayList<String>) gramTree.get(grams[i]));
						first = 1;
						continue;
					}
					
					
					//��һ��֮��temp��result������
					List<String> temp = (ArrayList<String>) gramTree.get(grams[i]);
					List<String> r = new ArrayList<String>();//������ʱ��Ž��
					if(temp != null){
						int j=0,k=0;
						
						//��2��list�еĽ���
						while(j<result.size() && k<temp.size()){
							if(result.get(j).equals(temp.get(k))){
								r.add(result.get(j));
								j++;
								k++;
							}
							else if(result.get(j).compareTo(temp.get(k)) < 0){
								//first�ַ�����temp���ַ���ҪС�����Ի����Լ����Ƚ�
								j++;
							}else{
								k++;
							}
						}
						result.clear();
						result.addAll(r);
					}
				}
			}
			
		}
		
		//���ԣ���������
		System.out.println("ͨ���ƥ������");
		for(String r : result){
			System.out.print(r+",");
		}
		System.out.println("\n");
		
		return result;
	}
	
	
	//����
	public static void main(String[] args){
		SimpleSearch s = new SimpleSearch();
		String str = null;

		Scanner scan = new Scanner(System.in);
		// �Ӽ��̽�������

		// �ж��Ƿ�������
		if (scan.hasNextLine()) {
			str = scan.nextLine();
		}

		scan.close();

		List<Term> l = s.handle(str);
		System.out.println(System.getProperty("user.dir"));

		System.out.println("\n------------��ѯ���---------------");
		if(l == null)
			System.out.println("�޲�ѯ�����");
		else{
			try{
				File file = new File("result.txt"); 
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));  

				for(Term t : l){
					bw.write("��ѯ��䣺"+t.term+"\r\n");
					bw.write("------------��ѯ���---------------\r\n");
					bw.write("�����ļ����           ���ִ���\r\n");

					Set hs = new TreeSet<Integer>();  
					Iterator<Map.Entry<Integer,Integer>> it = t.map.entrySet().iterator();
					while(it.hasNext()){
						Entry<Integer,Integer> e = it.next();
						bw.write(e.getKey()+"                      "+e.getValue()+"\r\n");
						hs.add(e.getKey());
					}

					bw.write("\r\n\r\n------------�����------------\r\n");
					//String dir = System.getProperty("user.dir")+"/��ʣ�δɾͣ�ôʣ�/doc";
					String dir = "";
					String []dirs = System.getProperty("user.dir").split("\\\\");
					for(String d : dirs){
						dir += "/";
						dir += d;
					}
					dir = dir.substring(1,dir.length());
					dir += "/��ʣ�δɾͣ�ôʣ�/doc";

					Iterator sit = hs.iterator();  
					while(sit.hasNext()) {  
						int doc_id = (int)sit.next();
						int doc = (doc_id % 4 == 0) ? doc_id/4 : doc_id/4+1;
						int seg = (doc_id % 4 == 0) ? 4 : doc_id%4;
						String fn = doc+"-"+seg+".txt";
						bw.write("doc"+doc+"-"+seg+":"+"\r\n");
						bw.write(SongReader.readFromFile(dir+fn)+"\r\n\r\n");
					}  
				}
				
				bw.close();
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				System.out.println("����������result.txt��");
			}
		}
	}
}
