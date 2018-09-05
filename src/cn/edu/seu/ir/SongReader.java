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
	public Map<String, Integer> map[];//���ÿ���ĵ���Ӧ���ʴ�Ƶ
	public List<Term> index;//���ÿ��term����Ϣ
	public List<Document> document;//����ĵ������Ϣ
	
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
            // ��ȡ��ÿһ�����ݺ������һ���ո����ڲ�ֳ����  
            sb.append(temp + " ");  
            temp = br.readLine();  
        }  
        return sb.toString();  
    }  
	
	
	//���ʴ���map����¼���ִ���
	public void saveInMap() throws IOException{
		String dir = "./���и��/";
		for(int i=1;i<=Global.doc_sum;i++)
		{
			Map<BigramModel,Integer> sequence = new HashMap<BigramModel,Integer>();//ͳ��bi-gramƵ����Ϣ
			String file=i+".txt";
			String song = readFromFile(dir+file);
			String[] words = song.split(" ");
			System.out.println("��ȡ"+dir+file);//����

			int before = 0;
			int find = 0;

			for(int j=0;j<words.length;j++){
				//ȥ��һЩ����Ҫ���ַ�
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



				//ͳһת��ΪСд
				words[j] = words[j].toLowerCase();
				//�ش��㷨
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

				//�ִʺ󣬽�ÿ�����ʷ����ĵ���Ӧmap�У�����¼���ִ���
				if(map[i-1].containsKey(words[j]))
					map[i-1].replace(words[j], map[i-1].get(words[j])+1);
				else
					map[i-1].put(words[j], 1);
			}
			//���ַ���������
			map[i-1]=sortMapByKey(map[i-1]);

			int sum=0;//ͳ�Ƹ��ĵ��е��ʸ���
			Iterator<Map.Entry<String,Integer>> it=map[i-1].entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry<String, Integer> entry = it.next();
				sum += entry.getValue();
			}

			document.add(new Document(i,sum,sequence,map[i-1]));
		}
	}
	
	//map��string���ַ���������
	public static Map<String, Integer> sortMapByKey(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        Map<String, Integer> sortMap = new TreeMap<String, Integer>(new MapKeyComparator());
        sortMap.putAll(map);
        return sortMap;       
    }
	
	
	
	//�������ĵ���ͳ�Ƶ���Ƶ������ͳ�ƣ�ÿ��map���Ѿ������ź��򣩣�ÿ��term��Term�ദ��
	public void getIndex(){
		for(int i=0;i<Global.doc_sum;i++){
			Iterator<Map.Entry<String,Integer>> it=map[i].entrySet().iterator();  
			while(it.hasNext()){  
				Map.Entry<String,Integer> entry=it.next();  
				String key = entry.getKey();
				int sum = entry.getValue();//term��������
				int docs = 1;//term�ڶ���ƪ�����г��ֹ�
				Map<Integer, Integer> m=new TreeMap<Integer, Integer>();
				m.put(i+1, entry.getValue());

				//��map[i+1]-[doc_sum]��Ѱ���Ƿ�����ͬ��term
				for(int j=i+1;j<Global.doc_sum;j++){
					//System.out.println("term:"+key+";"+i+","+j);
					Iterator<Map.Entry<String,Integer>> it2=map[j].entrySet().iterator();  
					while(it2.hasNext()){     
						Map.Entry<String,Integer> en=it2.next();  
						//���map[i+1]-[doc_sum]������ͬ��term����put��index��map��
						if(en.getKey().equals(key)){ 
							m.put(j+1,en.getValue());
							docs++;
							sum+=en.getValue();
							//ɾ����Map�еĸ�Ԫ�أ������ظ����term
							it2.remove();
						}
						//��������������С�Ļ�����ɨ��
						else if(en.getKey().compareTo(key) < 0)
							continue;
						else
							break;
					}
				}  
				Term t = new Term(key,docs,sum,m);
				index.add(t);
				//ɾ����Map�еĸ�Ԫ�أ������ظ����term
				it.remove();
			}
		}
	}
	
	public static void main(String[] args) throws IOException{
		SongReader r = new SongReader();
		r.saveInMap();
		//����
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


//�Ƚ������࣬����map��key����
class MapKeyComparator implements Comparator<String>{
    @Override
    public int compare(String str1, String str2) {
        return str1.compareTo(str2);
    }
}
