/**
 * webmagic-pipeline模块，直接在控制台输出结果
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
    //过滤掉字符串中<>标签中的字符串，使div中a标签去掉，仅剩歌词文本内容
    public static String filter(String str){
        str = str.replace("</a>","");
        String[] seg = str.split("<");
        String result = "";

        for(int i=0;i<seg.length;i++){
            if(seg[i].contains(">")){
                result += seg[i].substring(seg[i].indexOf(">")+1,seg[i].length());
            }else{
                //不包含<的部分
                result += seg[i];
            }
        }

        //System.out.println(result);
        return result;
    }

    @Override
    public void process(ResultItems resultItems, Task task) {
        System.out.println("get page: " + resultItems.getRequest().getUrl());
        //取出歌曲标题以及歌词
        String t = resultItems.get("title").toString();
        String l = filter(resultItems.get("lyrics").toString());

        //写入文件
        File f = new File("./扒取歌词/"+t+".txt");
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
            System.out.println("写出到文件："+t);
        }
    }
}
