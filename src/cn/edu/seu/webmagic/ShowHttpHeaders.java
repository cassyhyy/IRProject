/**
 * 获取http响应头，便于GET请求处理
 */
package cn.edu.seu.webmagic;

/**
 * 获取get请求重定向后返回的loaction响应头，便于扒取处理
 */
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class ShowHttpHeaders {
    //传入http的get请求，若302返回重定向地址
    public static String getLocation(String get) {
        //发送get请求
        try {
            URL serverUrl = null;
            serverUrl = new URL(get);

            HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
            conn.setRequestMethod("GET");
            //必须设置false，否则会自动redirect到重定向后的地址
            conn.setInstanceFollowRedirects(false);
            conn.addRequestProperty("Accept-Charset", "UTF-8;");
            conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Firefox/3.6.8");
            conn.addRequestProperty("Referer", "http://matols.com/");
            conn.connect();

            //判定是否会进行302重定向
            if (conn.getResponseCode() == 302) {
                //如果会重定向，保存302重定向地址，以及Cookies,然后重新发送请求(模拟请求)
                String location = conn.getHeaderField("Location");
                return location;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //失败返回null
        return null;
    }

    //测试
    public static void main(String[] args){
        System.out.println(getLocation("https://www.lyrics.com/random.php"));
    }
}