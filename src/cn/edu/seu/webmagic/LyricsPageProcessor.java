/**
 * 爬虫实现，利用webmagic框架对https://www.lyrics.com上的歌词进行扒取
 */
package cn.edu.seu.webmagic;

import us.codecraft.webmagic.*;
import us.codecraft.webmagic.processor.PageProcessor;

import java.util.Map;
import java.util.Random;

public class LyricsPageProcessor implements PageProcessor{
    // 部分一：抓取网站的相关配置，包括编码、抓取间隔、重试次数等
    private Site site = Site.me().setRetryTimes(3).setSleepTime(1000);


    @Override
    public void process(Page page){
        // 部分二：定义如何抽取页面信息，并保存下来
        //歌曲标题
        page.putField("title", page.getHtml().$("#lyric-title-text","text").toString());
        //歌词
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
            //获取到重定向地址/lyric/XXXXXXXX
            String id = ShowHttpHeaders.getLocation("https://www.lyrics.com/random.php");

            Spider.create(new LyricsPageProcessor())
                    //从"https://www.lyrics.com/lyric/XXXXXXXX"开始抓
                    .addUrl("https://www.lyrics.com"+id)
                    //输出歌词到文件
                    .addPipeline(new ConsolePipeline())
                    //开启5个线程抓取
                    .thread(5)
                    //启动爬虫
                    .run();
            i++;
        }
    }
}
