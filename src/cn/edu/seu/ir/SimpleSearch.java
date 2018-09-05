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
	public BplusTree termTree;//存放所有term项的B+树
	private BplusTree gramTree;//存放所有term项对应grams的B+树（key为对应gram,value为含有该gram的升序排序的单词链表）
	private Map<String,List<String>> grams;
	
	//初始化，包括从数据库中读数据terms并存入到B+树中、将terms依据Bigram建立相应索引
	public SimpleSearch(){
		List<Term> tlist = new ArrayList<Term>();
		
		//从数据库中一次性取出所有数据
		tlist = Global.database.select();

		//存入到B+树中
		termTree = new BplusTree(6);
		for(Term t : tlist)
			termTree.insertOrUpdate(t.term, t);
		
		NGramIndex gramIndex = new NGramIndex(tlist,2);
		grams = gramIndex.getGrams();
		gramTree = gramIndex.getGramTree();
	}


	//预处理：即将用户输入表达式转换为后缀表达式便于处理
	public static String preHandle(String str){
		str = str.toLowerCase();//先转换为小写

		String result = null;//存放处理结果
		Stack<String> stack = new Stack<String>();//栈  

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
				//遇到右括号，退栈至相应左括号
				//先输出temp
				if(temp != null && temp.length() >0){
					//截词算法.....
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
				stack.pop();//让左括号出栈
			}else if(str.charAt(i)==' '){
				//遇到空格，处理temp
				if(temp.equals("and") || temp.equals("or") || temp.equals("andnot")){
					//如果有左括号或者栈为空，先压栈
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
					//不是操作符的term
					//截词算法.....
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

		//将剩余操作符和term输出
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
		String before = null;//before代表前一个操作符，第一次扫描可以知道哪些是需要listMerge操作的
		int index = -1;//存放before前一个操作符所在位置，便于修改
		int num = 0;//存放before前一个操作符已经出现过几次


		//扫描处理，将有连续的在后面标记1,2,3,....没有的则没有标记
		for(int i=0;i<words.length;i++){
			switch(words[i]){
			case "and":
				if(before != null && before.equals("and")){
					if(num == 0){
						//第二次连续出现，给第一个标上and1
						num++;//num==1
						words[index] = "AND"+num;
					}
					if(num > 0){
						num++;
						words[i] = "AND"+num;
					}
				}else{
					//前一个操作符不是and
					num = 0;
				}
				words[i] = words[i].toUpperCase();
				index = i;
				before = "and";
				break;
			case "or":
				if(before != null && before.equals("or")){
					if(num == 0){
						//第二次连续出现，给第一个标上or1
						num++;//num==1
						words[index] = "OR"+num;
					}
					if(num > 0){
						num++;
						words[i] = "OR"+num;
					}
				}else{
					//前一个操作符不是or
					num = 0;
				}
				words[i] = words[i].toUpperCase();
				index = i;
				before = "or";
				break;
			case "andnot":
				if(before != null && before.equals("andnot")){
					if(num == 0){
						//第二次连续出现，给第一个标上andnot1
						num++;//num==1
						words[index] = "ANDNOT"+num;
					}
					if(num > 0){
						num++;
						words[i] = "ANDNOT"+num;
					}
				}else{
					//前一个操作符不是andnot
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
		System.out.println("预处理后："+result);

		return result;
	}

	
	//对有and/or/andnot操作符的输入字符串（通配符处理完毕，没有通配符，字符串中包含的都是正常的单词）进行相应操作，返回Term
	public Term search(String str){
		str = preHandle(str);//先预处理
		Term result = null;
		int k = 0;//表示是否进行过第一次的merge操作，为0的话直接将查找到的term项赋值给result
		String[] words = str.split(" ");//空格分割
		Stack<Term> stack = new Stack<Term>();


		int and=0,or=0,andnot=0;
		for(int i=0;i<words.length;i++){
			if(words[i].contains("AND") && !words[i].contains("ANDNOT")){
				//and操作符
				if(or > 0 || andnot > 0){
					//判断之前是否有连续的or或者andnot，进行处理
					Term temp = null;
					if(!(i>0 && (words[i-1].contains("AND")|| words[i-1].contains("OR"))))
						temp = stack.pop();//先存放栈顶对象
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
						stack.push(temp);//push进去，之后还要进行运算的
					or = 0;
					andnot = 0;
				}
				if(words[i].length() > 3){
					and = words[i].charAt(3) - '0';//第几次and连续操作	
				}else{
					//and不连续，可以退栈操作
					if(k == 0){
						Term t2 = stack.pop();
						Term t1 = stack.pop();
						result = SetOperation.andMerge(t1,t2);
						k=1;
					}else
						result = SetOperation.andMerge(result,stack.pop());
				}
			}else if(words[i].contains("OR")){
				//or操作符
				if(and > 0 || andnot > 0){
					//判断之前是否有连续的and或者andnot，进行处理
					Term temp = null;
					if(!(i>0 && (words[i-1].contains("AND")|| words[i-1].contains("OR"))))
						temp = stack.pop();//先存放栈顶对象
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
						stack.push(temp);//push进去，之后还要进行运算的
					and = 0;
					andnot = 0;
				}
				if(words[i].length() > 2){
					or = words[i].charAt(2) - '0';//第几次or连续操作	
				}else{
					//or不连续，可以退栈操作
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
					//判断之前是否有连续的or或者and，进行处理
					Term temp = null;
					if(!(i>0 && (words[i-1].contains("AND")|| words[i-1].contains("OR"))))
						temp = stack.pop();//先存放栈顶对象
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
						stack.push(temp);//push进去，之后还要进行运算的
					or = 0;
					and = 0;
				}
				if(words[i].length() > 6){
					andnot = words[i].charAt(6) - '0';//第几次and连续操作	
				}else{
					//System.out.println("ANDNOT:"+stack.peek().term);
					//and不连续，可以退栈操作
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
				//普通的term
				if(termTree.get(words[i]) == null)
					return null;//没有该term，返回Null结果
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
	
	
	//处理一个用户输入的语句，作集合操作并返回List<Term>结果（因可能有通配符，所以可能对应多个单词，即多个Term结果）
	public List<Term> handle(String str){
		//str = preHandle(str);//先预处理
		List<Term> result = new ArrayList<Term>();
		
		//如果用户输入的语句不包含and\or\andnot操作符，直接对该term项进行处理
		if( !(str.contains("and") || str.contains("or") || str.contains("andnot")) ){
			if(str.contains("*")){
				//包含通配符
				List<String> s = wildcardHandle(str);
				for(String term : s){
					//测试！！！！！
					System.out.println("可能的term:"+s);
					//都加入到result中
					result.add((Term)termTree.get(term));
				}
				return result;
			}else{
				//不包含通配符，直接返回
				result.add((Term)termTree.get(str));
				return result;
			}
		}
		
		
		//包含and/or/andnot操作符
		//不包含通配符，直接将str传入到search函数中处理
		Map<Integer,List<String>> possible = new TreeMap<Integer,List<String>>();//存放i,words[i]中term项所有可能的单词
		
		if(!str.contains("*")){
			result.add(search(str));
			return result;
		}
		//包含通配符
		String[] words = str.split(" ");
		for(int i=0; i<words.length; i++){
			if(words[i].contains("AND") || words[i].contains("OR"))
				continue;
			else if(words[i].contains("*")){
				//对每个包含通配符的term进行处理
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
		
		System.out.println("通配符处理后，搜索语句为："+search);
		result.add(search(search));

		return result;
	}

	
	
	/**
	 * 1.*abc  $*abc$
	 * 	*在第一个，不需要有$a，abc$
	 * 2.ab*c  $ab*c$
	 *  *不在末尾也不在第一个，必须要有$a，并且必须有c$，$ab*c$
	 * 3.abc*  $abc*$
	 * 	*在末尾，必须要有$a，不需要有c$，$abc
	 * 4.a*bc*d  $a*bc*d$
	 * 	必须要有$a,d$，不需要有$b,c$，$a*bc*d$
	 * 5.a*b*d   $a*b*d$
	 * 	必须要有$a,d$，b在ab-bz之间，$a*b*d$
	 * 经上述分析，可先在通配符单词开头和结尾加上$，$*与*$可消除
	 */
	//处理包含通配符的单词，返回所有可能单词的一个list
	public List<String> wildcardHandle(String str){
		String word = "$"+str+"$";//现在开头结尾加上$，便于之后处理
		word = word.replace("$*", "");
		word = word.replace("*$", "");//消除$*与*$
		String[] s = word.split("\\*");//先分割
		
		List<String> result = new ArrayList<String>();//存放结果
		int first = 0;
		
		for(int x=0; x<s.length; x++){
			if(s[x] == null || s[x].length() <= 0){
				
			}else if(s[x].length() == 1){
				//字符分割后长度为1
				String[] grams = NGramIndex.formGramsWithoutD(s[x],2);
				
				Set<String> temp = new TreeSet<String>();//不重复
				for(String g : grams){
					if((ArrayList<String>) gramTree.get(g)!=null){
						temp.addAll((ArrayList<String>) gramTree.get(g));
					}	
				}
				if(first == 0){
					//第一次做交集，result还为空，先把list全部add进去
					result.addAll(temp);
					first = 1;
					continue;
				}
				
				int j=0;
				//找2个list中的交集
				List<String> r = new ArrayList<String>();//用于暂时存放结果
				Iterator<String> it= temp.iterator();
				String k = it.next();
				while(j<result.size() && it.hasNext()){  
					if(result.get(j).equals(k)){
						r.add(result.get(j));
						j++;
						k = it.next();
					}
					else if(result.get(j).compareTo(k) < 0){
						//first字符串比temp的字符串要小，所以还可以继续比较
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
						//第一次做交集，result还为空，先把list全部add进去
						result.addAll((ArrayList<String>) gramTree.get(grams[i]));
						first = 1;
						continue;
					}
					
					
					//第一次之后，temp与result做交集
					List<String> temp = (ArrayList<String>) gramTree.get(grams[i]);
					List<String> r = new ArrayList<String>();//用于暂时存放结果
					if(temp != null){
						int j=0,k=0;
						
						//找2个list中的交集
						while(j<result.size() && k<temp.size()){
							if(result.get(j).equals(temp.get(k))){
								r.add(result.get(j));
								j++;
								k++;
							}
							else if(result.get(j).compareTo(temp.get(k)) < 0){
								//first字符串比temp的字符串要小，所以还可以继续比较
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
		
		//测试！！！！！
		System.out.println("通配符匹配结果：");
		for(String r : result){
			System.out.print(r+",");
		}
		System.out.println("\n");
		
		return result;
	}
	
	
	//测试
	public static void main(String[] args){
		SimpleSearch s = new SimpleSearch();
		String str = null;

		Scanner scan = new Scanner(System.in);
		// 从键盘接收数据

		// 判断是否还有输入
		if (scan.hasNextLine()) {
			str = scan.nextLine();
		}

		scan.close();

		List<Term> l = s.handle(str);
		System.out.println(System.getProperty("user.dir"));

		System.out.println("\n------------查询结果---------------");
		if(l == null)
			System.out.println("无查询结果！");
		else{
			try{
				File file = new File("result.txt"); 
				BufferedWriter bw = new BufferedWriter(new FileWriter(file));  

				for(Term t : l){
					bw.write("查询语句："+t.term+"\r\n");
					bw.write("------------查询结果---------------\r\n");
					bw.write("所在文件编号           出现次数\r\n");

					Set hs = new TreeSet<Integer>();  
					Iterator<Map.Entry<Integer,Integer>> it = t.map.entrySet().iterator();
					while(it.hasNext()){
						Entry<Integer,Integer> e = it.next();
						bw.write(e.getKey()+"                      "+e.getValue()+"\r\n");
						hs.add(e.getKey());
					}

					bw.write("\r\n\r\n------------结果集------------\r\n");
					//String dir = System.getProperty("user.dir")+"/歌词（未删停用词）/doc";
					String dir = "";
					String []dirs = System.getProperty("user.dir").split("\\\\");
					for(String d : dirs){
						dir += "/";
						dir += d;
					}
					dir = dir.substring(1,dir.length());
					dir += "/歌词（未删停用词）/doc";

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
				System.out.println("结果已输出在result.txt中");
			}
		}
	}
}
