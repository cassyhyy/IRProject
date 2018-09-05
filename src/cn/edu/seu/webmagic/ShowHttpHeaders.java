/**
 * ��ȡhttp��Ӧͷ������GET������
 */
package cn.edu.seu.webmagic;

/**
 * ��ȡget�����ض���󷵻ص�loaction��Ӧͷ�����ڰ�ȡ����
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
    //����http��get������302�����ض����ַ
    public static String getLocation(String get) {
        //����get����
        try {
            URL serverUrl = null;
            serverUrl = new URL(get);

            HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
            conn.setRequestMethod("GET");
            //��������false��������Զ�redirect���ض����ĵ�ַ
            conn.setInstanceFollowRedirects(false);
            conn.addRequestProperty("Accept-Charset", "UTF-8;");
            conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Firefox/3.6.8");
            conn.addRequestProperty("Referer", "http://matols.com/");
            conn.connect();

            //�ж��Ƿ�����302�ض���
            if (conn.getResponseCode() == 302) {
                //������ض��򣬱���302�ض����ַ���Լ�Cookies,Ȼ�����·�������(ģ������)
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
        //ʧ�ܷ���null
        return null;
    }

    //����
    public static void main(String[] args){
        System.out.println(getLocation("https://www.lyrics.com/random.php"));
    }
}