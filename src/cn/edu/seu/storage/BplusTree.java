package cn.edu.seu.storage;

import java.util.HashMap;
import java.util.Map;

import cn.edu.seu.ir.Term;

public class BplusTree implements B {  
    
    /** 根节点 */  
    protected Node root;  
      
    /** 阶数，M值 */  
    protected int order;  
      
    /** 叶子节点的链表头*/  
    protected Node head;  
      
    public Node getHead() {  
        return head;  
    }  
  
    public void setHead(Node head) {  
        this.head = head;  
    }  
  
    public Node getRoot() {  
        return root;  
    }  
  
    public void setRoot(Node root) {  
        this.root = root;  
    }  
  
    public int getOrder() {  
        return order;  
    }  
  
    public void setOrder(int order) {  
        this.order = order;  
    }  
  
    @Override  
    public Object get(Comparable key) {  
        return root.get(key);  
    }  
  
    @Override  
    public void remove(Comparable key) {  
        root.remove(key, this);  
  
    }  
  
    @Override  
    public void insertOrUpdate(Comparable key, Object obj) {  
        root.insertOrUpdate(key, obj, this);  
  
    }  
      
    public BplusTree(int order){  
        if (order < 3) {  
            System.out.print("order must be greater than 2");  
            System.exit(0);  
        }  
        this.order = order;  
        root = new Node(true, true);  
        head = root;  
    }  
      
    //测试  
    public static void main(String[] args) {  
        BplusTree tree = new BplusTree(6);  
        Map m = new HashMap<Integer,Integer>();
        m.put(1, 1);
        m.put(2, 1);
        m.put(3, 1);
        Term t = new Term("abc",3,3,m);
        
        tree.insertOrUpdate("abc",t);
    }  
  
}  
