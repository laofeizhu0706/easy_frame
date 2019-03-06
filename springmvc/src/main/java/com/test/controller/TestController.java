package com.test.controller;

import com.test.pojo.req.Person;
import com.test.springmvc.annotation.Controller;
import com.test.springmvc.annotation.RequestMapping;
import com.test.springmvc.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 老肥猪
 * @since 2019/2/28
 * msg:
 */
@Controller
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/hello")
    public String hello(HttpServletRequest request, HttpServletResponse response){
        try {
            response.getWriter().write("test hello");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    @RequestMapping("/hello2")
    public String hello2(HttpServletResponse response, @RequestParam String name,@RequestParam String age){
        try {
            response.getWriter().write("hello "+name+",your age is "+age);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
