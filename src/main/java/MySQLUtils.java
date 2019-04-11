
import java.io.*;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class MySQLUtils {

    public HashMap<String, String> queryColumn(String HostName, String DBname, String UserName, String Password, String TableName, int VersionDesc) {


        String[] authconfig = new String[5];
        if (VersionDesc > 0) {
            //新驱动包全名
            authconfig[0] = "com.mysql.cj.jdbc.Driver";
        } else {
            authconfig[0] = "com.mysql.jdbc.Driver";
        }
        //地址
        authconfig[1] = "jdbc:mysql://" + HostName + ":3306/" + DBname + "?&useSSL=false";
        //用户名
        authconfig[2] = UserName;
        //密码
        authconfig[3] = Password;

        Connection connection = null;

        HashMap<String, String> colmap = new HashMap<String, String>();

        try {
            //加载mysql的驱动类
            Class.forName(authconfig[0]);
            //获取数据库连接
            connection = DriverManager.getConnection(authconfig[1], authconfig[2], authconfig[3]);
            //mysql查询语句
            String sql = "SELECT * FROM " + TableName + ";";
            PreparedStatement prst = connection.prepareStatement(sql);
            //获取列总数
            System.out.println("Sum List : " + prst.getMetaData().getColumnCount());

            int sumcol = prst.getMetaData().getColumnCount();

            for (int i = 0; i < sumcol; i++) {
                colmap.put(prst.getMetaData().getColumnName(i + 1), prst.getMetaData().getColumnTypeName(i + 1));
            }
        } catch (Exception e ) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return colmap;
    }

    public static void main(String[] args) {
//        数据库地址
        String HostName = "192.168.1.10";
//        数据库
        String DBName = "flume";
//        用户名
        String UserNmae = "root";
//        密码
        String Password = "123456";
//        表名
        String TableNmae = "logs";
//        版本信息 如果是新版本的就填写大于1的数 反之小于零的数
        int VersionDesc = 9;

        GenerateBean bean = new GenerateBean();

        bean.GenerateBean(new MySQLUtils().queryColumn(HostName, DBName, UserNmae, Password, TableNmae, VersionDesc));

    }

}

//写出文件
class GenerateBean {

    BufferedWriter bWriter = null;

    public final String keyword = "private";

    {
        try {
            bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("./" + System.currentTimeMillis() + ".java"))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //输出Bean
    public void GenerateBean(HashMap<String, String> mysql) {
        // 遍历MySQL字段信息
        for (Map.Entry<String, String> map : mysql.entrySet()) {
            try {
                //字段名字
                String deffields = map.getKey();

                //字段类型
                String deftypes = map.getValue();

                //字段驼峰处理
                String[] temp = deffields.split("_");

                int tempindex = temp.length;

                String problemfield = "";

                if (tempindex == 2) {
                    temp[0] = temp[0].substring(0, 1).toUpperCase() + temp[0].substring(1).toLowerCase();
                    temp[1] = temp[1].substring(0, 1).toUpperCase() + temp[1].substring(1).toLowerCase();
                    problemfield = temp[0] + temp[1];
                } else {
                    problemfield = deffields.substring(0, 1).toUpperCase() + deffields.substring(1).toLowerCase();
                }
                if (tempindex == 3) {
                    temp[0] = temp[0].substring(0, 1).toUpperCase() + temp[0].substring(1).toLowerCase();
                    temp[1] = temp[1].substring(0, 1).toUpperCase() + temp[1].substring(1).toLowerCase();
                    temp[2] = temp[2].substring(0, 1).toUpperCase() + temp[2].substring(1).toLowerCase();
                    problemfield = temp[0] + temp[1] + temp[2];
                }

                problemfield += ";\n";
                System.out.println(deftypes);
                //判断列类型
                if (deftypes.contains("DOUBLE") || deftypes.contains("FLOAT") || deftypes.contains("DECIMAL") || deftypes.contains("REAL") || deftypes.contains("NUMERIC")) {
                    bWriter.append(keyword + " double " + problemfield);
                } else if (deftypes.contains("BIG")) {
                    bWriter.append(keyword + " long " + problemfield);
                } else if (deftypes.contains("DATE") || deftypes.contains("CHAR") || deftypes.contains("TEXT") || deftypes.contains("TIME")) {
                    bWriter.append(keyword + " String " + problemfield);
                } else if (deftypes.contains("INT")) {
                    bWriter.append(keyword + " int " + problemfield);
                }
                bWriter.flush();
            } catch (IOException e) {
                try {
                    bWriter.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }

}

