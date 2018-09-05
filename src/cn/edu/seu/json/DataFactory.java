package cn.edu.seu.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * 使用Gson进行Json和String之间的转换
 */
public class DataFactory {
    public static Object getInstanceByJson(Class<?> clazz, String json)
    {
        Object obj = null;
        //Gson，用于处理Java和Json对象间的转换
        Gson gson = new Gson();
        obj = gson.fromJson(json, clazz); //将String（Json)对象转换成clazz的类对象
        return obj;
    }

    public static <T> List<T> jsonToList(String json, Class<T[]> clazz)
    {
        Gson gson = new Gson();
        T[] array = gson.fromJson(json, clazz);
        return Arrays.asList(array); //asList：将一个数组转变为List类型
    }


    public static <T> ArrayList<T> jsonToArrayList(String json, Class<T> clazz)
    {
        //TypeToken是为了解决在Json和Java类型转换过程中的泛型数组序列化和反序列化问题
    	/*
    	 * 例子
    	 * TestGeneric<String> t = new TestGeneric<String>();
  		 * t.setValue("Alo");
  		 * Type type = new TypeToken<TestGeneric<String>>(){}.getType();
  		 * String gStr = GsonUtils.gson.toJson(t,type);
  		 * System.out.println(gStr);
  		 * TestGeneric t1 = GsonUtils.gson.fromJson(gStr, type);
  		 * System.out.println(t1.getValue());
    	 */
    	Type type = new TypeToken<ArrayList<JsonObject>>(){}.getType();
        ArrayList<JsonObject> jsonObjects = new Gson().fromJson(json, type);

        ArrayList<T> arrayList = new ArrayList<T>();
        for (JsonObject jsonObject : jsonObjects)
        {
            arrayList.add(new Gson().fromJson(jsonObject, clazz));
        }
        return arrayList;
    }
    
    //文书类转换为Json对象
    public static String documentToJson(Object document) {
        Gson gson = new Gson();
        String json = gson.toJson(document);
        return json;
    }
}
