package com.test.mytomcat;

/**
 * @author 老肥猪
 * @since 2019/3/6
 */
public abstract class Servlet {
    public abstract void doGet(Request request,Response response);
    public abstract void doPost(Request request,Response response);

    /**
     * 请求分发
     * @param request
     * @param response
     */
    public void service(Request request,Response response) {
        System.out.println(request);
        if("GET".equals(request.getMethod())) {
            this.doGet(request,response);
        } else {
            this.doPost(request,response);
        }
    }
}
