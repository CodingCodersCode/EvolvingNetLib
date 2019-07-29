package com.codingcoderscode.lib.net.util;

import android.text.TextUtils;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;

import okhttp3.MediaType;

/**
 * Created by CodingCodersCode on 2017/10/31.
 */

public class CCFileUtils {

    /**
     * 删除文件
     *
     * @param filePath
     */
    public static void deleteFile(String filePath) {
        try {
            if (!TextUtils.isEmpty(filePath)) {
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

    /**
     * 获取File上传的mimeType
     *
     * @param filePath
     * @return
     */
    public static String getMimeType(String filePath) {
        String mimeType = "";
        MediaType mediaType;
        try {
            if (TextUtils.isEmpty(filePath)) {
                mimeType = "multipart/form-data;";
            } else {
                FileNameMap fileNameMap = URLConnection.getFileNameMap();
                //url = url.replace("#", "");   //解决文件名中含有#号异常的问题
                String contentType = fileNameMap.getContentTypeFor(filePath.replace("#", ""));
                if (contentType == null) {
                    mediaType = MediaType.parse("application/octet-stream");
                } else {
                    mediaType = MediaType.parse(contentType);
                }

                if (mediaType != null) {
                    mimeType = mediaType.toString();
                } else {
                    mimeType = "multipart/form-data;";
                }
            }
        } catch (Exception e) {

        }
        return mimeType;
    }
}
