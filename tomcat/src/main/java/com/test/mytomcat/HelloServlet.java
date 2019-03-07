package com.test.mytomcat;

/**
 * @author 老肥猪
 * @since 2019/3/6
 */
public class HelloServlet extends Servlet {
    @Override
    public void doGet(Request request, Response response) {
        response.write("<h1>Hello world! get</h1>");
    }

    @Override
    public void doPost(Request request, Response response) {
        response.write("<h1>Hello world! post</h1>");
    }
}
