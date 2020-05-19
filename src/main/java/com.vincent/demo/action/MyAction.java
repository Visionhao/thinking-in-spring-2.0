package com.vincent.demo.action;

import com.vincent.demo.service.IModifyService;
import com.vincent.demo.service.IQueryService;
import com.vincent.spring.framework.annotation.VincentAutowried;
import com.vincent.spring.framework.annotation.VincentController;
import com.vincent.spring.framework.annotation.VincentRequestMapping;
import com.vincent.spring.framework.annotation.VincentRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 公共接口 url
 */
@VincentController
@VincentRequestMapping("/web")
public class MyAction {

    @VincentAutowried
    private IQueryService queryService;

    @VincentAutowried
    private IModifyService modifyService;

    @VincentRequestMapping("/query.json")
    public void query(HttpServletRequest request, HttpServletResponse response,
                      @VincentRequestParam("name") String name){
        String result = queryService.query(name);
        out(response,result);
    }

    @VincentRequestMapping("/add*.json")
    public void add(HttpServletRequest request, HttpServletResponse response,
                    @VincentRequestParam("name") String name,@VincentRequestParam("addr") String addr){
        String result = modifyService.add(name,addr);
        out(response,result);
    }

    @VincentRequestMapping("/remove.json")
    public void remove(HttpServletRequest request, HttpServletResponse response,
                       @VincentRequestParam("id") Integer id){
        String result = modifyService.remove(id);
        out(response,result);
    }

    @VincentRequestMapping("/edit.json")
    public void edit(HttpServletRequest request, HttpServletResponse response,
                     @VincentRequestParam("id") Integer id,@VincentRequestParam("name") String name){
        String result = modifyService.edit(id, name);
        out(response,result);
    }

    private void out(HttpServletResponse response, String result) {
        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
