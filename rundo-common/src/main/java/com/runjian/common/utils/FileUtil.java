package com.runjian.common.utils;

import java.io.*;

public class FileUtil {
    /**
     * 方法功能：将字节数组写入到新建文件中。
     * @param String fname
     * @param byte[] msg
     * @return boolean
     * */
    public static boolean save2File(String fname, byte[] msg){
        OutputStream fos = null;
        try{
            File file = new File(fname);
            File parent = file.getParentFile();
            boolean bool;
            if ((!parent.exists()) &&
                    (!parent.mkdirs())) {
                return false;
            }
            fos = new FileOutputStream(file);
            fos.write(msg);
            fos.flush();
            return true;
        }catch (FileNotFoundException e){
            return false;
        }catch (IOException e){
            File parent;
            return false;
        }
        finally{
            if (fos != null) {
                try{
                    fos.close();
                }catch (IOException e) {}
            }
        }
    }

//    public static void main(String[] args)  {
//        String msgStr = "我是java爱好者，我来自java265.com";
//        String filename = "D:\\test\\test.txt";//注意修改为自己的文件名
//
//    }

}
