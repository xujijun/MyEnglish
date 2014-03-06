/**
 * 
 */
package com.xjj.myEnglish.util;

import java.io.InputStream;

import org.apache.http.util.EncodingUtils;

import android.content.Context;
import android.widget.Toast;

/**
 * @author XJJ
 * Provide some utilities
 */
public class Util {
	
	/**
	 * Read txt file in the raw folder
	 * @return the content in the txt file
	 */
	public static String readRawTxtFile(Context context, int fileId){
		String content = ""; 
        try { 
            InputStream in = context.getResources().openRawResource(fileId); 
            //获取文件的字节数 
            int length = in.available(); 
            //创建byte数组 
            byte[] buffer = new byte[length]; 
            //将文件中的数据读到byte数组中 
            in.read(buffer); 
            content = EncodingUtils.getString(buffer, "UTF-8"); 
        } catch (Exception e) { 
        	content = "读取raw文件错误！";
            Toast.makeText(context, content, Toast.LENGTH_LONG).show();
            //e.printStackTrace(); 
        } 
                
        return content;
		
	}

}
