package cn.edu.seu.word;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;


public class StopWords {
	public static void remove(String infile, String outfile){
		try{
			//停用词文件
			FileInputStream sin = new FileInputStream("stopword.txt");
			InputStreamReader sisr = new InputStreamReader(sin,"UTF-8");
			BufferedReader swbr = new BufferedReader(sisr);
			
			//待处理文件
			FileInputStream fin = new FileInputStream(infile);
			InputStreamReader fisr = new InputStreamReader(fin,"UTF-8");
			BufferedReader fbr = new BufferedReader(fisr);
			
			//输出文本
			FileOutputStream fout = new FileOutputStream(outfile);
			OutputStreamWriter osw = new OutputStreamWriter(fout, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			
			String ss = null;
			String sa=null;
			//String saa=null;
			StringBuffer s2=new StringBuffer();
			StringBuffer s1=new StringBuffer();
			
			//读取停词表，保存在s1中
			while((ss = swbr.readLine()) != null){
				s1.append(ss).append(" ");
			}
			String[] resultArray=(s1.toString()).split(" ");//分词，将所有停词保存在resultArray中
			
			//读取待处理文件，保存在s2中
			while((sa = fbr.readLine()) != null){
				s2.append(sa).append(" ");
			}
			String[] srcArray=(s2.toString().toLowerCase()).split(" ");//分词，将待处理文件中所有单词保存在srcArray中
			
			//扫描srcArray中是否有停词，有就去掉
			for(int i = 0; i < srcArray.length; i++){
				for(int j = 0; j < resultArray.length; j++){
					if(srcArray[i].equals(resultArray[j])){
						srcArray[i] = "";
					}
				}
			}
			
			//将处理后的单词写入目标文件中
			StringBuffer sb = new StringBuffer();
			for(int i = 0; i < srcArray.length; i++){
				if(srcArray[i] != null){
					sb = sb.append(srcArray[i]).append(" ");
				}
			}
			bw.write(sb.toString());
			bw.newLine();
			bw.flush();
			bw.close();
			swbr.close();
			fbr.close();
		}
		catch(FileNotFoundException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
	}
	
	//测试
	public static void main(String[] args){
		String dir = "./歌词（未删停用词）/";
		String out = "./所有歌词/";

		int i=1;

		File d = new File(dir);
		String[] files = d.list();
		for(String f : files){
			System.out.println(f);
			String file = dir+f;
			StopWords.remove(file,out+i+".txt");
			//new File(file).renameTo(new File(dir+i+".txt"));//重命名
			i++;
		}

		System.out.println("停用词过滤完毕");
	}
}


