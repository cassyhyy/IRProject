/**
 * 二元语言模型
 */
package cn.edu.seu.ir;

import java.util.Objects;

public class BigramModel {
    public String t1;
    public String t2;

    public BigramModel(String t1, String t2){
        this.t1 = t1;
        this.t2 = t2;
    }

    //重写equals函数
    @Override
    public boolean equals(Object obj) {
        if(obj==null){
            return false;
        }
        if(this==obj){
            return true;
        }

        if(obj instanceof BigramModel){
            BigramModel b=(BigramModel) obj;
            if(this.t1.equals(b.t1) && this.t2.equals(b.t2))
                return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(t1,t2);
    }
}
