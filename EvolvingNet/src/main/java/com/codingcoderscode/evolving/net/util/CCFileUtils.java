package com.codingcoderscode.evolving.net.util;

import android.text.TextUtils;

import java.io.File;

/**
 * Created by ghc on 2017/10/31.
 */

public class CCFileUtils {

    /**
     * 删除文件
     *
     * @param filePath
     */
    public static void deleteFile(String filePath) {
        try {
            if (!TextUtils.isEmpty(filePath)){
                deleteFile(new File(filePath));
            }
        } catch (Exception e) {

        }
    }

    /**
     * 删除文件
     *
     * @param file
     */
    public static void deleteFile(File file) {
        try {
            if (file != null && file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

}
