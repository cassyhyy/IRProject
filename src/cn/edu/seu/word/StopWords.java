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
			//ͣ�ô��ļ�
			FileInputStream sin = new FileInputStream("stopword.txt");
			InputStreamReader sisr = new InputStreamReader(sin,"UTF-8");
			BufferedReader swbr = new BufferedReader(sisr);
			
			//�������ļ�
			FileInputStream fin = new FileInputStream(infile);
			InputStreamReader fisr = new InputStreamReader(fin,"UTF-8");
			BufferedReader fbr = new BufferedReader(fisr);
			
			//����ı�
			FileOutputStream fout = new FileOutputStream(outfile);
			OutputStreamWriter osw = new OutputStreamWriter(fout, "UTF-8");
			BufferedWriter bw = new BufferedWriter(osw);
			
			String ss = null;
			String sa=null;
			//String saa=null;
			StringBuffer s2=new StringBuffer();
			StringBuffer s1=new StringBuffer();
			
			//��ȡͣ�ʱ�������s1��
			while((ss = swbr.readLine()) != null){
				s1.append(ss).append(" ");
			}
			String[] resultArray=(s1.toString()).split(" ");//�ִʣ�������ͣ�ʱ�����resultArray��
			
			//��ȡ�������ļ���������s2��
			while((sa = fbr.readLine()) != null){
				s2.append(sa).append(" ");
			}
			String[] srcArray=(s2.toString().toLowerCase()).split(" ");//�ִʣ����������ļ������е��ʱ�����srcArray��
			
			//ɨ��srcArray���Ƿ���ͣ�ʣ��о�ȥ��
			for(int i = 0; i < srcArray.length; i++){
				for(int j = 0; j < resultArray.length; j++){
					if(srcArray[i].equals(resultArray[j])){
						srcArray[i] = "";
					}
				}
			}
			
			//�������ĵ���д��Ŀ���ļ���
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
	
	//����
	public static void main(String[] args){
		String dir = "./��ʣ�δɾͣ�ôʣ�/";
		String out = "./���и��/";

		int i=1;

		File d = new File(dir);
		String[] files = d.list();
		for(String f : files){
			System.out.println(f);
			String file = dir+f;
			StopWords.remove(file,out+i+".txt");
			//new File(file).renameTo(new File(dir+i+".txt"));//������
			i++;
		}

		System.out.println("ͣ�ôʹ������");
	}
}


