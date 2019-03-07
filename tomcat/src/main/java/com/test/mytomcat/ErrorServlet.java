package com.test.mytomcat;

/**
 * @author 老肥猪
 * @since 2019/3/6
 */
public class ErrorServlet extends Servlet {


    @Override
    public void doGet(Request request, Response response) {
        this.doPost(request,response);
    }

    @Override
    public void doPost(Request request, Response response) {
        response.write("<center><h1>404</h1></center>");
    }
}
