package cn.edu.seu.storage;

import java.io.IOException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.edu.seu.Global;
import cn.edu.seu.ir.*;
import com.mysql.jdbc.PreparedStatement;


public class Database {
	private Connection con;
	private String driver;
	private String url;
	private String user;
	private String password;
	
	public Database(String d, String url, String u, String p){
		con = null;
		driver = d;
		this.url = url;
		user = u;
		password = p;
		try {
            // 加载驱动
            Class.forName(driver);
            // 设置配置数据
            // 1.url(数据看服务器的ip地址 数据库服务端口号 数据库实例)
            // 2.user
            // 3.password
            con = DriverManager.getConnection(this.url, user, password);
            // 开始连接数据库
            System.out.println("数据库连接成功..");
        } catch (ClassNotFoundException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
	}
	

	//向数据库插入terms
	public void insert(List<Term> t, List<Document> d){
		//遍历查询结果集
		try {
			if(!con.isClosed())
				System.out.println("连接数据库成功！");

			//要执行的SQL语句
			/*PreparedStatement terms = (PreparedStatement) con.prepareStatement("INSERT INTO terms(id,term,doc_num,frequency_sum)"+"values(?,?,?,?)");
			PreparedStatement indexes = (PreparedStatement) con.prepareStatement("INSERT INTO indexes(id,term_id,doc_id,frequency)"+"values(?,?,?,?)");
            PreparedStatement idf = (PreparedStatement) con.prepareStatement("INSERT INTO idf(id,term,idf)"+"values(?,?,?)");
            PreparedStatement weight = (PreparedStatement) con.prepareStatement("INSERT INTO weight(id,term_id,doc_id,w,w_after)"+"values(?,?,?,?,?)");*/
			PreparedStatement bigram = (PreparedStatement) con.prepareStatement("INSERT INTO bigram(id,doc_id,t1,t2,frequency)"+"values(?,?,?,?,?)");


			//id可能需要修改！插入的时候从MAX(id)开始插入
			int j=1,k=1,l=1;
			/*for(int i=0;i<t.size();i++)
			{
				/*String term = t.get(i).term;
				int docs = t.get(i).docs;
				int sum = t.get(i).sum;

				//用preparedStatement预处理来执行sql语句
				terms.setInt(1, i+1);
				terms.setString(2, term);
				terms.setInt(3, docs);
				terms.setInt(4, sum);
				terms.addBatch();

				//idf
                idf.setInt(1,i+1);
                idf.setString(2,term);
                idf.setDouble(3,t.get(i).idf);
                idf.addBatch();

				//文档-词频map
				Iterator<Map.Entry<Integer,Integer>> it=t.get(i).map.entrySet().iterator();  
				while(it.hasNext())
				{
					Entry<Integer,Integer> e = it.next();
					//System.out.print(e.getKey()+"  "+e.getValue()+";");
					indexes.setInt(1, j);
					indexes.setInt(2, i+1);
					indexes.setInt(3, e.getKey());
					indexes.setInt(4, e.getValue());
					indexes.addBatch();
					j++;
				}

				//文档-权值map
                Iterator<Map.Entry<Integer,Double>> it2=t.get(i).weight.entrySet().iterator();
                while(it2.hasNext())
                {
                    Entry<Integer,Double> e = it2.next();
                    //System.out.print(e.getKey()+"  "+e.getValue()+";");
                    weight.setInt(1, k);
                    weight.setInt(2, i+1);
                    weight.setInt(3, e.getKey());
                    weight.setDouble(4, e.getValue());
                    weight.setDouble(5,t.get(i).weight2.get(e.getKey()));
                    weight.addBatch();
                    k++;
                }
			}*/

			for(int x=0;x<d.size();x++){
				//文档-bigram信息
				for (BigramModel bi : d.get(x).sequence.keySet()) {
					bigram.setInt(1, l);
					bigram.setInt(2, x+1);
					bigram.setString(3, bi.t1);
					bigram.setString(4, bi.t2);
					bigram.setInt(5, d.get(x).sequence.get(bi));
					bigram.addBatch();
					l++;
				}
			}

			/*terms.executeBatch();
			terms.close();
			indexes.executeBatch();
			indexes.close();
            idf.executeBatch();
            idf.close();
            weight.executeBatch();
			weight.close();*/
			bigram.executeBatch();
			bigram.close();

			//断开连接
			//con.close();
		} catch(SQLException e) {
			//数据库连接失败异常处理
			e.printStackTrace();  
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			System.out.println("数据库数据成功插入！！");
		}
	}
	
	
	//从数据库中读取
	public List<Term> select(){
		List<Term> t = new ArrayList<Term>();
		//遍历查询结果集
		try {
			if(!con.isClosed())
				System.out.println("连接数据库成功！");

			//要执行的SQL语句
			PreparedStatement sql = (PreparedStatement) con.prepareStatement("SELECT t.term,t.doc_num,t.frequency_sum,i.doc_id,i.frequency FROM terms t,indexes i WHERE t.id=i.term_id ORDER BY t.term,doc_id;");

	        ResultSet r = sql.executeQuery();
	        // 遍历结果
	        Term temp = null;
	        Map<Integer,Integer> map = new TreeMap<Integer,Integer>();
	        
	        //第一行结果
	        r.next();
	        String term = r.getString(1);
	        int docs = r.getInt(2);
	        int sum = r.getInt(3);
	        int doc_id = r.getInt(4);
	        int frequency = r.getInt(5);
	        map.put(doc_id, frequency);
	        
	        while(r.next()){
	        	if(term.equals(r.getString(1))){
	        		//是同一个term
	        		doc_id = r.getInt(4);
	        		frequency = r.getInt(5);
	        		map.put(doc_id, frequency);
	        	}else{
	        		//不是同一个term，先将上一个term存到对象中并add到list里
	        		temp = new Term(term,docs,sum,map);
	        		t.add(temp);
	       
	        		//清空map
	        		map = new TreeMap<Integer,Integer>();
	        		//更新相关数据
	        		term = r.getString(1);
	        		docs = r.getInt(2);
	    	        sum = r.getInt(3);
	    	        doc_id = r.getInt(4);
	    	        frequency = r.getInt(5);
	    	        map.put(doc_id, frequency);
	        	}
	        }
	        //测试
	        /*Iterator<Map.Entry<Integer,Integer>> it = map.entrySet().iterator();
			while(it.hasNext()){
				Entry<Integer,Integer> e = it.next();
				System.out.println(term+":"+e.getKey()+","+e.getValue());
			}*/
			//con.close();
		} catch(SQLException e) {
			//数据库连接失败异常处理
			e.printStackTrace();  
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			System.out.println("数据库数据查询成功！！");
		}

		return t;
	}

	public List<Document> getDocument(){
		Map<Integer,Integer> sum = getWords();
		List<Document> result = new ArrayList<Document>();

		//遍历查询结果集
		try {
			if(!con.isClosed())
				System.out.println("连接数据库成功！");

			//要执行的SQL语句
			PreparedStatement sql = (PreparedStatement) con.prepareStatement("SELECT id,doc_id,t1,t2,frequency FROM bigram;");

			ResultSet r = sql.executeQuery();
			int doc_id = 1;
			Map<BigramModel,Integer> m = new HashMap<BigramModel,Integer>();
			// 遍历结果!!!!!!!!!!!!!!需修改 doc_id<=Global.doc_sum &&
			while(r.next()) {
				if(r.getInt(2) == doc_id){

					String t1 = r.getString(3);
					String t2 = r.getString(4);
					int frequency = r.getInt(5);

					BigramModel b = new BigramModel(t1,t2);
					m.put(b,frequency);
				}else{
					/*List<Term> terms = getTerms(doc_id);
					Map<String,Integer> t = new TreeMap<String, Integer>();
					for(int i=0;i<terms.size();i++){
						t.put(terms.get(i).term,terms.get(i).map.get(doc_id));
					}*/
					result.add(new Document(doc_id, sum.get(doc_id), m));

					//清除m
					m = new HashMap<BigramModel,Integer>();
					m.clear();

					//将当前的信息保存
					doc_id = r.getInt(2);
					String t1 = r.getString(3);
					String t2 = r.getString(4);
					int frequency = r.getInt(5);

					BigramModel b = new BigramModel(t1,t2);
					m.put(b,frequency);
				}
			}
			result.add(new Document(doc_id, sum.get(doc_id), m));//最后一个
		} catch(SQLException e) {
			//数据库连接失败异常处理
			e.printStackTrace();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			System.out.println("数据库数据查询成功！！");
		}

		return result;
	}

	//从数据库中查询每篇文章所有单词出现总和
	public Map<Integer,Integer> getWords(){
		Map<Integer,Integer> m = new TreeMap<Integer, Integer>();
		//遍历查询结果集
		try {
			if(!con.isClosed())
				System.out.println("连接数据库成功！");

			//要执行的SQL语句
			PreparedStatement sql = (PreparedStatement) con.prepareStatement("SELECT doc_id,SUM(frequency) FROM indexes GROUP BY doc_id;");

			ResultSet r = sql.executeQuery();
			// 遍历结果
			while(r.next()) {
				int doc_id = r.getInt(1);
				int words = r.getInt(2);

				m.put(doc_id, words);
			}
		} catch(SQLException e) {
			//数据库连接失败异常处理
			e.printStackTrace();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}finally{
			System.out.println("数据库数据查询成功！！");
		}

		return m;
	}

	//从数据库中查询对应文章中出现的所有term
	public List<Term> getTerms(int doc_id) {
		List<String> terms = new ArrayList<String>();
		List<Term> result = new ArrayList<Term>();
		//遍历查询结果集
		try {
			if (!con.isClosed())
				System.out.println("连接数据库成功！");

			//要执行的SQL语句
			PreparedStatement sql = (PreparedStatement) con.prepareStatement("SELECT term FROM terms t,indexes i WHERE i.doc_id=? AND t.id=i.term_id");
            sql.setInt(1, doc_id);

            ResultSet r = sql.executeQuery();
			// 遍历结果
			while (r.next()) {
				String term = r.getString(1);
				terms.add(term);
			}
			Collections.sort(terms);//term按升序排序

			//通过B+树找到Term，返回结果
			SimpleSearch s = new SimpleSearch();
			for (String t : terms) {
				result.add((Term) s.termTree.get(t));
			}

		} catch (SQLException e) {
			//数据库连接失败异常处理
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			System.out.println("数据库数据查询成功！！");
		}

		return result;
	}
	
	public static void main(String[] args){
		SongReader r = new SongReader();
		try {
			r.saveInMap();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		r.getIndex();

		Global.database.insert(r.index,r.document);
	}
	
}

