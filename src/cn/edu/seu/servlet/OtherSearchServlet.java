package cn.edu.seu.servlet;

import cn.edu.seu.ir.SimpleSearch;
import cn.edu.seu.ir.Song;
import cn.edu.seu.ir.SongReader;
import cn.edu.seu.ir.Term;
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
import java.util.*;

public class OtherSearchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SimpleSearch s;

    public void init() throws ServletException {
        System.out.println("��������ʼ������");

        s = new SimpleSearch();
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request,response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("utf-8");

        //��ȡ�ֶ�����
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
            String keyword = map.get("keyword");//��ȡ�ؼ���
            System.out.println(keyword);
            try {
                doSearch(keyword,request,response);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void doSearch(String string,HttpServletRequest request,
                         HttpServletResponse response) throws ServletException, ParseException, InvalidClassException,IOException
    {
        List<Term> result = s.handle(string);

        //��resultת��Ϊjson����
        String json = "[";
        int i =0;

        //·��
        String filePath = this.getServletConfig().getServletContext().getRealPath("/");
        String dir = "";
        String []dirs = filePath.split("\\\\");
        for(int z=0;z<dirs.length-3;z++){
            dir += "/";
            dir += dirs[z];
        }
        dir = dir.substring(1,dir.length());
        dir += "/���и�ʣ�δɾͣ�ôʣ�/";
        //System.out.println(dir);

        //String dir = "E:/��ѧ/��Ϣ����/���/doc";

        for(Term t : result){
            System.out.println(t.term);
            if(i>0){
                json += ",";
            }
            //��ÿ��term������ǰ��term,�����ĵ���������Ƶ�������Լ���Ӧ���ڵ��ĵ�
            json += "{\"word\":"+DataFactory.documentToJson(t);

            List<Song> song = new ArrayList<Song>();
            Iterator<Map.Entry<Integer,Integer>> it = t.map.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry<Integer,Integer> e = it.next();
                int id = e.getKey();
                song.add(new Song(id,dir));
            }
            json += ",\"song\":"+DataFactory.documentToJson(song)+"}";
            i++;
        }

        json += "]";

        System.out.println("���ݣ�"+json);//���ԣ�����

        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();
        out.write(json);
        out.flush();
        out.close();
    }

}
