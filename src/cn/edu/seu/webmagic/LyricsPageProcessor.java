/**
 * ����ʵ�֣�����webmagic��ܶ�https://www.lyrics.com�ϵĸ�ʽ��а�ȡ
 */
package cn.edu.seu.webmagic;

import us.codecraft.webmagic.*;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Map;
import java.util.Random;

public class LyricsPageProcessor implements PageProcessor{
    // ����һ��ץȡ��վ��������ã��������롢ץȡ��������Դ�����
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);


    @Override
    public void process(Page page){
        // ���ֶ���������γ�ȡҳ����Ϣ������������
        //��������
        page.putField("title", page.getHtml().$("#lyric-title-text","text").toString());
        //���
        page.putField("lyrics", page.getHtml().$("#lyric-body-text").toString());

        if (page.getResultItems().get("title") == null) {
            //skip this page
            page.setSkip(true);
        }
    }

    @Override
    public Site getSite() {
        return site;
    }



    public static void main(String[] args) {
        Random rand = new Random();
        int i = 0;

        while(i<=100) {
            //��ȡ���ض����ַ/lyric/XXXXXXXX
            String id = ShowHttpHeaders.getLocation("https://www.lyrics.com/random.php");

            Spider.create(new LyricsPageProcessor())
                    //��"https://www.lyrics.com/lyric/XXXXXXXX"��ʼץ
                    .addUrl("https://www.lyrics.com"+id)
                    //�����ʵ��ļ�
                    .addPipeline(new ConsolePipeline())
                    //����5���߳�ץȡ
                    .thread(5)
                    //��������
                    .run();
            i++;
        }
    }
}
