/**
 * webmagic-pipelineģ�飬ֱ���ڿ���̨������
 */
package cn.edu.seu.webmagic;

import us.codecraft.webmagic.ResultItems;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.pipeline.Pipeline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class ConsolePipeline implements Pipeline {
    //���˵��ַ�����<>��ǩ�е��ַ�����ʹdiv��a��ǩȥ������ʣ����ı�����
    public static String filter(String str){
        str = str.replace("</a>","");
        String[] seg = str.split("<");
        String result = "";

        for(int i=0;i<seg.length;i++){
            if(seg[i].contains(">")){
                result += seg[i].substring(seg[i].indexOf(">")+1,seg[i].length());
            }else{
                //������<�Ĳ���
                result += seg[i];
            }
        }

        //System.out.println(result);
        return result;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        System.out.println("get page: " + resultItems.getRequest().getUrl());
        //ȡ�����������Լ����
        String t = resultItems.get("title").toString();
        String l = filter(resultItems.get("lyrics").toString());

        //д���ļ�
        File f = new File("./��ȡ���/"+t+".txt");
        BufferedWriter bw = null;
        try {
            if(!f.exists()){
                f.createNewFile();
            }
            bw = new BufferedWriter(new FileWriter(f));
            bw.write(l);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            System.out.println("д�����ļ���"+t);
        }
    }
}
