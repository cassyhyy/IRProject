package cn.edu.seu.ir;

import cn.edu.seu.Global;
import cn.edu.seu.storage.BplusTree;
import cn.edu.seu.word.Stemming;

import java.io.IOException;
import java.util.*;

public class BigramSearch {
    public BplusTree termTree;//存放所有term项的B+树
    public List<Document> document;//文档语言模型
    public Map<BigramModel,Integer> all;//全局语言模型
    public int terms_sum;//所有terms总数量，用于计算词频

    //初始化，包括从数据库中读数据terms并存入到B+树中、将terms依据Bigram建立相应索引
    public BigramSearch(){
        List<Term> tlist = new ArrayList<Term>();

        //从数据库中一次性取出所有数据
        tlist = Global.database.select();
        terms_sum = 0;

        //存入到B+树中
        termTree = new BplusTree(6);
        for(Term t : tlist) {
            termTree.insertOrUpdate(t.term, t);
            terms_sum += t.sum;
        }

        document = new ArrayList<Document>();
        document = Global.database.getDocument();

        //初始化全局语言模型，对应bigram-在所有文档中的词序列频率
        all = new HashMap<BigramModel, Integer>();
        for(int i=0;i<document.size();i++) {
            Map<BigramModel, Integer> bi = document.get(i).sequence;

            Iterator<Map.Entry<BigramModel, Integer>> it=bi.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<BigramModel, Integer> entry = it.next();
                BigramModel b = entry.getKey();
                if (!all.isEmpty() && all.containsKey(b)) {
                    all.replace(b,all.get(b)+entry.getValue());
                } else {
                    all.put(b, entry.getValue());
                }
            }
        }
    }

    public String preHandle(String str){
        str = str.toLowerCase();
        String[] string = str.split(" ");

        Stemming s = new Stemming();
        String result = "";

        for(String t : string){
            t = s.stem(t);//截词算法处理
            result += " "+t;
        }

        result = result.substring(1);//第一个字符是空格，先删去
        return result;
    }

    //得到在全局中的ranking值
    public double getRankingAll(String str){
        str = preHandle(str);
        String[] words = str.split(" ");
        double result = 0.0;
        int find = 0;

        for(int i=0;i<words.length;i++) {
            if (termTree.get(words[i])!=null){
                if(find>0){
                    BigramModel b = new BigramModel(words[i-1],words[i]);
                    Term t1 = (Term)termTree.get(words[i-1]);
                    result *= all.get(b)/t1.sum;
                }else{
                    Term t2 = (Term)termTree.get(words[i]);
                    result = t2.sum/terms_sum;
                    find=1;
                }
            }else{
                return 0.0;
            }
        }
        return result;
    }


    //得到在文档id中的ranking值
    public double getRanking(String str,int id){
        str = preHandle(str);
        String[] words = str.split(" ");
        double result = 0.0;
        int find = 0;

        for(int i=0;i<words.length;i++) {
            if (termTree.get(words[i])==null)
                return 0.0;
            Term temp = (Term)termTree.get(words[i]);
            if(!temp.map.containsKey(id))
                return 0.0;

            if (find > 0) {
                BigramModel b = new BigramModel(words[i - 1], words[i]);

                if(!document.get(id-1).sequence.containsKey(b))
                    return 0.0;

                int f = document.get(id-1).sequence.get(b);//2个词按次序出现的次数
                Term t = (Term)termTree.get(words[i-1]);
                //int num = document.get(id-1).terms.get(words[i-1]);//前面一个词出现的次数
                int num = t.map.get(id);//前面一个词出现的次数
                result *= (double) f / num;
            }else{
                //第一个单词出现的概率
                Term t = (Term)termTree.get(words[i]);
                //int num = document.get(id-1).terms.get(words[i-1]);//前面一个词出现的次数
                result = (double)t.map.get(id)/document.get(id-1).words_num;
                find = 1;
            }
        }
        //System.out.println(id+":"+result);//测试！！！！
        return result;
    }

    //模糊匹配下的ranking计算，包括了对全局、单独文档语言模型的ranking，r为平滑处理参数
    public double getRankingIndistinct(String str,int id){
        double r = 0.85;
        return r*getRanking(str,id)+(1-r)*getRankingAll(str);
    }


    //对用户输入的查询进行搜索（模糊匹配），并随ranking排序
    public List<Song> searchWithLM(String str,String dir) throws IOException {
        Map<Integer,Double> ranking = new TreeMap<Integer, Double>();

        for(int i=1;i<=Global.doc_sum;i++) {
            if (getRankingIndistinct(str, i) > 0.0)
                ranking.put(i, getRankingIndistinct(str, i));
        }
        //将ranking按值降序排序
        List<Map.Entry<Integer,Double>> rList = new ArrayList<Map.Entry<Integer,Double>>(ranking.entrySet());
        Collections.sort(rList, new Comparator<Map.Entry<Integer,Double>>() {
            @Override
            public int compare(Map.Entry<Integer,Double> o1, Map.Entry<Integer,Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });


        List<Song> result = new ArrayList<Song>();
        for(int i=0;i<rList.size();i++){
            //System.out.println(rList.get(i).getKey()+":"+rList.get(i).getValue());
            result.add(new Song(rList.get(i).getKey(),rList.get(i).getValue(),dir));
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        BigramSearch b = new BigramSearch();
        String str = "";

        System.out.println("请输入歌词（结果仅会输出ranking大于0的歌词）：");

        Scanner scan = new Scanner(System.in);
        // 从键盘接收数据

        // 判断是否还有输入
        if (scan.hasNextLine()) {
            str = scan.nextLine();
        }
        scan.close();

        List<Song> song =  b.searchWithLM(str,Global.dir);
        for(Song so : song){
            System.out.println("文档序号："+so.title);
            System.out.println("ranking值："+so.ranking);
        }

        //System.out.println(b.getRanking(str,2));
    }
}
