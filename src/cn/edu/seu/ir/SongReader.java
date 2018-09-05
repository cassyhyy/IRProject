package cn.edu.seu.ir;

import java.io.BufferedReader;  
import java.io.File;  
import java.io.FileNotFoundException;  
import java.io.FileOutputStream;  
import java.io.FileReader;  
import java.io.IOException;  
import java.util.*;
import java.util.Map.Entry;

import cn.edu.seu.Global;
import cn.edu.seu.word.Stemming;

public class SongReader {
	public Map<String, Integer> map[];//存放每个文档对应单词词频
	public List<Term> index;//存放每个term的信息
	public List<Document> document;//存放文档相关信息
	
	public SongReader(){
		map = new Map[Global.doc_sum];
		for(int i=0;i<Global.doc_sum;i++)
			map[i]=new HashMap<String,Integer>();
		index = new ArrayList<Term>();
		document = new ArrayList<Document>();
	}
	
	public static String readFromFile(String filename) throws IOException {  
        File file = new File(filename); 
        if (!file.exists() || file.isDirectory())  
            throw new FileNotFoundException();  
        BufferedReader br = new BufferedReader(new FileReader(file));  
        String temp = null;  
        StringBuffer sb = new StringBuffer();  
        temp = br.readLine();  
        while (temp != null) {  
            // 读取的每一行内容后面加上一个空格用于拆分成语句  
            sb.append(temp + " ");  
            temp = br.readLine();  
        }  
        return sb.toString();  
    }  
	
	
	//将词存入map并记录出现次数
	public void saveInMap() throws IOException{
		String dir = "./所有歌词/";
		for(int i=1;i<=Global.doc_sum;i++)
		{
			Map<BigramModel,Integer> sequence = new HashMap<BigramModel,Integer>();//统计bi-gram频率信息
			String file=i+".txt";
			String song = readFromFile(dir+file);
			String[] words = song.split(" ");
			System.out.println("读取"+dir+file);//测试

			int before = 0;
			int find = 0;

			for(int j=0;j<words.length;j++){
				//去掉一些不必要的字符
				if(words[j].contains(","))
					words[j] = words[j].replace(",", "");
				if(words[j].contains("!"))
					words[j] = words[j].replace("!", "");
				if(words[j].contains("\""))
					words[j] = words[j].replace("\"", "");
				if(words[j].contains(":"))
					words[j] = words[j].replace(":", "");
				if(words[j].contains(" "))
					words[j] = words[j].replace(" ", "");
				if(words[j].contains("."))
					words[j] = words[j].replace(".", "");
				if(words[j].contains("("))
					words[j] = words[j].replace("(", "");
				if(words[j].contains(")"))
					words[j] = words[j].replace(")", "");
				if(words[j].contains("?"))
					words[j] = words[j].replace("?", "");
				if(words[j].contains("["))
					words[j] = words[j].replace("[", "");
				if(words[j].contains("]"))
					words[j] = words[j].replace("]", "");
				if(words[j].contains(";"))
					words[j] = words[j].replace(";", "");
				if(words[j].contains("-"))
					words[j] = words[j].replace("-", "");
				if(words[j] == " " || words[j].length() <=0)
					continue;



				//统一转换为小写
				words[j] = words[j].toLowerCase();
				//截词算法
				Stemming s = new Stemming();
				words[j] = s.stem(words[j]);

				if(find>0){
					BigramModel temp = new BigramModel(words[before],words[j]);
					if(sequence.containsKey(temp)){
						sequence.replace(temp,sequence.get(temp)+1);
					}else{
						sequence.put(temp,1);
					}
				}

				before=j;
				find++;

				//分词后，将每个单词放入文档对应map中，并记录出现次数
				if(map[i-1].containsKey(words[j]))
					map[i-1].replace(words[j], map[i-1].get(words[j])+1);
				else
					map[i-1].put(words[j], 1);
			}
			//按字符升序排序
			map[i-1]=sortMapByKey(map[i-1]);

			int sum=0;//统计该文档中单词个数
			Iterator<Map.Entry<String,Integer>> it=map[i-1].entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String, Integer> entry = it.next();
				sum += entry.getValue();
			}

			document.add(new Document(i,sum,sequence,map[i-1]));
		}
	}
	
	//map按string的字符升序排序
	public static Map<String, Integer> sortMapByKey(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, Integer> sortMap = new TreeMap<String, Integer>(new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;       
    }
	
	
	
	//将各个文档中统计的字频进行总统计（每个map都已经升序排好序），每个term用Term类处理
	public void getIndex(){
		for(int i=0;i<Global.doc_sum;i++){
			Iterator<Map.Entry<String,Integer>> it=map[i].entrySet().iterator();  
			while(it.hasNext()){  
				Map.Entry<String,Integer> entry=it.next();  
				String key = entry.getKey();
				int sum = entry.getValue();//term出现总数
				int docs = 1;//term在多少篇文章中出现过
				Map<Integer, Integer> m=new TreeMap<Integer, Integer>();
				m.put(i+1, entry.getValue());

				//在map[i+1]-[doc_sum]中寻找是否有相同的term
				for(int j=i+1;j<Global.doc_sum;j++){
					//System.out.println("term:"+key+";"+i+","+j);
					Iterator<Map.Entry<String,Integer>> it2=map[j].entrySet().iterator();  
					while(it2.hasNext()){     
						Map.Entry<String,Integer> en=it2.next();  
						//如果map[i+1]-[doc_sum]中有相同的term，则put到index的map中
						if(en.getKey().equals(key)){ 
							m.put(j+1,en.getValue());
							docs++;
							sum+=en.getValue();
							//删除在Map中的该元素，避免重复添加term
							it2.remove();
						}
						//因按升序排序，所以小的话继续扫描
						else if(en.getKey().compareTo(key) < 0)
							continue;
						else
							break;
					}
				}  
				Term t = new Term(key,docs,sum,m);
				index.add(t);
				//删除在Map中的该元素，避免重复添加term
				it.remove();
			}
		}
	}
	
	public static void main(String[] args) throws IOException{
		SongReader r = new SongReader();
		r.saveInMap();
		//测试
		/*for(int i=0;i<5;i++)
		{
			System.out.println(i);
			for(Entry<String, Integer> vo : r.map[1].entrySet()){
				  System.out.println(vo.getKey()+"  "+vo.getValue());
			}
		}*/
		r.getIndex();	
		
		for(int i=0;i<r.index.size()/2;i++)
		{
			Iterator<Map.Entry<Integer,Integer>> it=r.index.get(i).map.entrySet().iterator();  
			while(it.hasNext())
			{
				Entry<Integer,Integer> e = it.next();
				System.out.println(e.getKey()+" "+e.getValue()+";");
			}
		}
	}
}


//比较容器类，用于map按key排序
class MapKeyComparator implements Comparator<String>{
    @Override
    public int compare(String str1, String str2) {
        return str1.compareTo(str2);
    }
}
