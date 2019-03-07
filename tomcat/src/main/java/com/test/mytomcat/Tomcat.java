package com.test.mytomcat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 老肥猪
 * @since 2019/3/6
 */
public class Tomcat {

    private Integer port=8080;

    public Tomcat(Integer port) {
        this.port = port;
    }

    public Tomcat() {
    }

    private static Map<String,String> urlMap=new HashMap<>();
    private void initUrlMap() {
        List<ServletMapping> mappingList = ServletMappingConfig.getMappingList();
        urlMap=mappingList.stream().collect(Collectors.toMap(k->k.getUrl(),v->v.getClazz()));
    }

    public void start() {
        System.out.println("tomcat启动了");
        initUrlMap();
        ServerSocket serverSocket=null;
        try {
            serverSocket =new ServerSocket(port);
            for (;;) {
                Socket socket = serverSocket.accept();
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();
                Request request = new Request(inputStream);
                Response response = new Response(outputStream);
                distribute(request,response);
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(serverSocket != null) {
                    System.out.println("tomcat关闭了");
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 分发请求
     * @param request
     * @param response
     */
    private void distribute(Request request, Response response) {
        String url = request.getUrl();
        String clazz = urlMap.get(url);
        Servlet servlet = null;
        if(clazz==null || "".equals(clazz)) {
            servlet=new ErrorServlet();
        } else {
            try {
                Class<Servlet> aClass = (Class<Servlet>)Class.forName(clazz);
                servlet =  aClass.newInstance();
//                System.out.println(aClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        servlet.service(request,response);
    }


}
