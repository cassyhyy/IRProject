package cn.edu.seu.storage;


public interface B {
	public Object get(Comparable key);   //��ѯ  
    public void remove(Comparable key);    //�Ƴ�  
    public void insertOrUpdate(Comparable key, Object obj); //������߸��£�����Ѿ����ڣ��͸��£��������  
}

