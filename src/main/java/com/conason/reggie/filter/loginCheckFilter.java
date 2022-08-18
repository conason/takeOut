package com.conason.reggie.filter;


import com.alibaba.fastjson.JSON;
import com.conason.reggie.common.BaseContext;
import com.conason.reggie.controller.Request;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.naming.Context;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 未登录请求拦截器
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class loginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //请求路径
        String requestURI = request.getRequestURI();

        //放行路径
        String[] urls = new String[]{
                "/backend/**",
                "/front/**",
                "/employee/login",
                "/employee/logout",
                "/user/login",
                "/user/sendMsg"
        };

        //放行白名单路径
        if(check(urls,requestURI)){
            filterChain.doFilter(request,response);
            return;
        }

        //放行员工已登录请求（session捏造一个“employee”也会放行）
        if(request.getSession().getAttribute("employee") != null){

            Long id = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(id);

            filterChain.doFilter(request,response);
            return;
        }

        //放行用户已登录请求
        if(request.getSession().getAttribute("user") != null){

            Long id = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(id);

            filterChain.doFilter(request,response);
            return;
        }

        //JSON输出流响应客户端
        response.getWriter().write(JSON.toJSONString(Request.error("NOTLOGIN")));
        return;

    }

    //匹配知否未放行白名单
    public boolean check(String[] urls, String requestURI){
        for (String url : urls) {
            if(PATH_MATCHER.match(url, requestURI)){
                return true;
            }
        }
        return false;
    }
}
