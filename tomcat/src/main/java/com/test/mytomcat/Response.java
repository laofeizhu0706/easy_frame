package com.test.mytomcat;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 老肥猪
 * @since 2019/3/6
 */
@Getter
@Setter
public class Response {

    private OutputStream outputStream;

    public Response(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(String content) {
        StringBuffer response=new StringBuffer();
//        HTTP/1.1 200 OK
//        Content-Type: text/html
        response.append("HTTP/1.1 200 OK\n")
                .append("Content-Type: text/html\n")
                .append("\r\n")
                .append("<html><body>")
                .append(content)
                .append("</body></html>");
        try {
            outputStream.write(response.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(outputStream!=null) {
                    outputStream.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
