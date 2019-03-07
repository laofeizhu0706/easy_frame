package com.test.mytomcat;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author 老肥猪
 * @since 2019/3/6
 */
@Getter
@Setter
public class Request {
    private String method;
    private String url;

    public Request(InputStream inputStream) {
        byte[] bytes = new byte[1024];
        int length=0;
        String httpRequestString = null;
        try {
            if ((length=inputStream.read(bytes))>0) {
                httpRequestString=new String(bytes,0,length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
//            try {
//                if(inputStream!=null) {
//                    inputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }
        if(httpRequestString==null) {
            return;
        }
        //        POST /test/hello HTTP/1.1
        String http = httpRequestString.split("\n")[0];
        this.method = http.split("\\s")[0];
        this.url = http.split("\\s")[1];
        System.out.println("\n"+httpRequestString);
    }
}
