package com.it.reggie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j   //日志log简化需要加的配置
@SpringBootApplication   //spring的启动类需要加的注释
@ServletComponentScan    //加了才会去访问过滤器（Web的过滤器），才能扫描到@WebFilter
@EnableTransactionManagement   //要让事务控制生效（ DishServiceImpl的@Transactional标签生效）
@EnableCaching  //开启Spring Cache注解方式的缓存功能
public class ReggieApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReggieApplication.class);
        log.info("项目启动成功");
    }
}
