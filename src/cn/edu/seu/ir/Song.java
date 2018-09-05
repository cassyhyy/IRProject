/**
 * ��Ÿ�����������Ӧ�ĵ�����Լ��������
 */
package cn.edu.seu.ir;

import cn.edu.seu.Global;

import java.io.*;

public class Song {
    public int title;
    public String lyrics;
    public double ranking;//������ģ���е�rankingֵ

    public Song(int t, String dir) throws IOException {
        title = t;
        lyrics = getLyrics(t,dir);
    }

    //�������ĵ���ţ���Ҫ��ȡ����ĵ��ļ����г�ʼ��
    public Song(int t, double r,String dir) throws IOException{
        title = t;
        lyrics = getLyrics(t,dir);
        ranking = r;
    }

    //�Զ�Ӧ���ĵ���ŵõ����
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
