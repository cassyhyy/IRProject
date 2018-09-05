/**
 * 存放歌曲，包括对应文档编号以及歌词内容
 */
package cn.edu.seu.ir;

import cn.edu.seu.Global;

import java.io.*;

public class Song {
    public int title;
    public String lyrics;
    public double ranking;//在语言模型中的ranking值

    public Song(int t, String dir) throws IOException {
        title = t;
        lyrics = getLyrics(t,dir);
    }

    //仅传入文档编号，需要读取歌词文档文件进行初始化
    public Song(int t, double r,String dir) throws IOException{
        title = t;
        lyrics = getLyrics(t,dir);
        ranking = r;
    }

    //对对应的文档编号得到歌词
    public String getLyrics(int t,String dir) throws IOException {
        File file = new File(dir+t+".txt");

        if (!file.exists() || file.isDirectory())
            throw new FileNotFoundException();
        BufferedReader br = new BufferedReader(new FileReader(file));

        String temp = null;
        StringBuffer sb = new StringBuffer();
        temp = br.readLine();
        while (temp != null) {
            sb.append(temp + "\n");
            temp = br.readLine();
        }
        return sb.toString();
    }
}
