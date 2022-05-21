package com.it.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.it.reggie.common.BaseContext;
import com.it.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否登录的过滤器
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j   //日志
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    //过滤的方法
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1、获取本次请求的URI
        String requestURI = request.getRequestURI();

        log.info("拦截到请求：{}", requestURI);

        // 把不需要拦截的路径放入
        // 比如登录页面（不让人登录怎么用？）的请求与退出（退出易漏）
        // 还有静态资源（因为重点的是数据，所以页面设计被看到也无所谓）
        String[] urls = new String[] {
                "/employee/login",
                "/employee/logout",
                "/backend/**",           //注意哦，/backend/index.html是匹配不上的，所以这里需要使用路径匹配器
                "/front/**",
                "/common/**",            //图片（文件）上传
                "/user/login",           //移动端登录
                "/user/sendMsg"          //移动端发送短信
        };

        //2、判断本次请求是否需要处理
        boolean check = check(urls, requestURI);

        //3、如果不需要处理，则直接放行
        if (check) {
            log.info("本次请求{}不需要处理", requestURI);
            filterChain.doFilter(request,response);    //放行
            return;   //方法结束，不需拦截
        }

        //4.1、判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("employee") != null) {
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("employee"));

            //获取当前线程ID到ThreadLocal封装工具类
            Long empId = (Long) request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);

            filterChain.doFilter(request, response);
            return;
        }

        //4.2、（对应移动端）（上面的代码复制下来把employee的全改成user即可）判断登录状态，如果已登录，则直接放行
        if (request.getSession().getAttribute("user") != null) {
            log.info("用户已登录，用户id为：{}", request.getSession().getAttribute("user"));

            //获取当前线程ID到ThreadLocal封装工具类
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);

            filterChain.doFilter(request, response);
            return;
        }

        log.info("用户未登录");
        //5、如果未登录则返回未登录结果，通过输出流方式向客户端页面响应数据（注意拦截器返回的是void，所以不能像controller那里这里返回R.error）
        //R转成JSON写回去
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));   //msg参数为NOTLOGIN 对应前端的js.request.js中的拦截器数据
        return;


        // {}表示占位符，↓相当于"拦截到请求：" + request.getRequestURI();
        //log.info("拦截到请求：{}",request.getRequestURI());   //写之前测试拦截器是否生效用的
    }

    /**
     * （因为/backend/**处理不了/backend/index.html这样的路径，所以需要路径匹配）
     * 路径匹配，检查本次请求是否需要放行
     * @param urls
     * @param requestURI
     * @return
     */
    public boolean check(String[] urls,String requestURI) {
        for (String url : urls) {
            boolean match = PATH_MATCHER.match(url, requestURI);  //匹配
            if (match) {
                return true;
            }
        }
        return false;
    }
}
