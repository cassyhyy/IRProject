package cn.edu.seu.servlet;

import cn.edu.seu.ir.*;
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

public class LMSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private BigramSearch s;

    public void init() throws ServletException {
        System.out.println("服务器初始化。。");
        s = new BigramSearch();
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request,response);
    }


    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");

        //获取字段内容
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
            String keyword = map.get("keyword");
            System.out.println(keyword);
            try {
                doSearch(keyword,request,response);//获取关键词
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void doSearch(String string,HttpServletRequest request,
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

        List<Song> result = s.searchWithLM(string,dir);


        String json = DataFactory.documentToJson(result);
        System.out.println("数据："+json);//测试！！！

        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();
        out.write(json);
        out.flush();
        out.close();
    }
}
