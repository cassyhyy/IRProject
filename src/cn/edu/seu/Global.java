/**
 * 存放一些全局变量，便于修改
 */
package cn.edu.seu;

import cn.edu.seu.storage.Database;

public class Global {
    public static int doc_sum = 246;
    public static Database database = new Database("com.mysql.jdbc.Driver","jdbc:mysql://localhost:3306/ir-exercise","root","123456");
    public static String dir = "./所有歌词（未删停用词）/"; //存放未删停用词歌曲的路径
}
