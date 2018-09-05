/**
 * 相似度查询Servlet
 */
package cn.edu.seu.servlet;

import cn.edu.seu.Global;
import cn.edu.seu.ir.BigramSearch;
import cn.edu.seu.ir.Song;
import cn.edu.seu.ir.WeightCompute;
import cn.edu.seu.json.DataFactory;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SIMSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private WeightCompute w;

    public void init() throws ServletException {
        System.out.println("服务器初始化。。");
        w = new WeightCompute(Global.doc_sum);
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request,response);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");

        Gson gson=new Gson();
        Map<String,String> resultMap=new HashMap<String,String>();
        BufferedReader br = request.getReader();

        String str="",data= "";
        while ((str = br.readLine()) != null) {
            data += str;
        }

        System.out.println(data);
        if(!"".equals(data)) {
            Map<String, String> map = gson.fromJson(data, Map.class);
            int doc1 = Integer.parseInt(map.get("doc1"));
            int doc2 = Integer.parseInt(map.get("doc2"));

            System.out.println(doc1+","+doc2);
            try {
                doSearch(doc1,doc2,request,response);//获取关键词
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void doSearch(int doc1, int doc2, HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, ParseException, InvalidClassException,IOException
    {
        //路径
        String filePath = this.getServletConfig().getServletContext().getRealPath("/");
        String dir = "";
        String []dirs = filePath.split("\\\\");
        for(int z=0;z<dirs.length-3;z++){
            dir += "/";
            dir += dirs[z];
        }
        dir = dir.substring(1,dir.length());
        dir += "/所有歌词（未删停用词）/";

        double sim = w.getSIM(doc1,doc2);
        Song s1 = new Song(doc1,dir);
        Song s2 = new Song(doc2,dir);

        String song1 = DataFactory.documentToJson(s1);
        String song2 = DataFactory.documentToJson(s2);

        String json = "{\"sim\":"+sim+",\"song1\":"+song1+",\"song2\":"+song2+"}";

        System.out.println("数据："+json);//测试！！！

        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();
        out.write(json);
        out.flush();
        out.close();
    }
}

