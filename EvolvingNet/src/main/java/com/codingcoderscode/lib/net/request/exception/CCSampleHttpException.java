package com.codingcoderscode.lib.net.request.exception;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Date：2018/5/7 16:53
 * <p>
 * author: CodingCodersCode
 * <p>
 * 简单的HttpException
 */
public class CCSampleHttpException extends HttpException {
    private String errorBody;

    public CCSampleHttpException(Response<?> response) {
        super(response);
        getErrorBodyString(null);
    }

    public CCSampleHttpException(Response<?> response, ResponseBody errorBody) {
        super(response);
        getErrorBodyString(errorBody);
    }


    private void getErrorBodyString(ResponseBody errorBody) {
        try {
            if (errorBody != null) {
                this.errorBody = errorBody.string();
            }
        } catch (Exception e) {
            errorBody = null;
        }
    }

    public String getErrorBody() {
        return errorBody;
    }

    public void setErrorBody(String errorBody) {
        this.errorBody = errorBody;
    }
}
