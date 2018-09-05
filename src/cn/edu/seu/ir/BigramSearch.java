package cn.edu.seu.ir;

import cn.edu.seu.Global;
import cn.edu.seu.storage.BplusTree;
import cn.edu.seu.word.Stemming;

import java.io.IOException;
import java.util.*;

public class BigramSearch {
    public BplusTree termTree;//�������term���B+��
    public List<Document> document;//�ĵ�����ģ��
    public Map<BigramModel,Integer> all;//ȫ������ģ��
    public int terms_sum;//����terms�����������ڼ����Ƶ

    //��ʼ�������������ݿ��ж�����terms�����뵽B+���С���terms����Bigram������Ӧ����
    public BigramSearch(){
        List<Term> tlist = new ArrayList<Term>();

        //�����ݿ���һ����ȡ����������
        tlist = Global.database.select();
        terms_sum = 0;

        //���뵽B+����
        termTree = new BplusTree(6);
        for(Term t : tlist) {
            termTree.insertOrUpdate(t.term, t);
            terms_sum += t.sum;
        }

        document = new ArrayList<Document>();
        document = Global.database.getDocument();

        //��ʼ��ȫ������ģ�ͣ���Ӧbigram-�������ĵ��еĴ�����Ƶ��
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
            t = s.stem(t);//�ش��㷨����
            result += " "+t;
        }

        result = result.substring(1);//��һ���ַ��ǿո���ɾȥ
        return result;
    }

    //�õ���ȫ���е�rankingֵ
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


    //�õ����ĵ�id�е�rankingֵ
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

                int f = document.get(id-1).sequence.get(b);//2���ʰ�������ֵĴ���
                Term t = (Term)termTree.get(words[i-1]);
                //int num = document.get(id-1).terms.get(words[i-1]);//ǰ��һ���ʳ��ֵĴ���
                int num = t.map.get(id);//ǰ��һ���ʳ��ֵĴ���
                result *= (double) f / num;
            }else{
                //��һ�����ʳ��ֵĸ���
                Term t = (Term)termTree.get(words[i]);
                //int num = document.get(id-1).terms.get(words[i-1]);//ǰ��һ���ʳ��ֵĴ���
                result = (double)t.map.get(id)/document.get(id-1).words_num;
                find = 1;
            }
        }
        //System.out.println(id+":"+result);//���ԣ�������
        return result;
    }

    //ģ��ƥ���µ�ranking���㣬�����˶�ȫ�֡������ĵ�����ģ�͵�ranking��rΪƽ���������
    public double getRankingIndistinct(String str,int id){
        double r = 0.85;
        return r*getRanking(str,id)+(1-r)*getRankingAll(str);
    }


    //���û�����Ĳ�ѯ����������ģ��ƥ�䣩������ranking����
    public List<Song> searchWithLM(String str,String dir) throws IOException {
        Map<Integer,Double> ranking = new TreeMap<Integer, Double>();

        for(int i=1;i<=Global.doc_sum;i++) {
            if (getRankingIndistinct(str, i) > 0.0)
                ranking.put(i, getRankingIndistinct(str, i));
        }
        //��ranking��ֵ��������
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

        System.out.println("�������ʣ�����������ranking����0�ĸ�ʣ���");

        Scanner scan = new Scanner(System.in);
        // �Ӽ��̽�������

        // �ж��Ƿ�������
        if (scan.hasNextLine()) {
            str = scan.nextLine();
        }
        scan.close();

        List<Song> song =  b.searchWithLM(str,Global.dir);
        for(Song so : song){
            System.out.println("�ĵ���ţ�"+so.title);
            System.out.println("rankingֵ��"+so.ranking);
        }

        //System.out.println(b.getRanking(str,2));
    }
}
