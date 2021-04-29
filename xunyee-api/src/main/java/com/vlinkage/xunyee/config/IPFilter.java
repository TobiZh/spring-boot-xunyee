package com.vlinkage.xunyee.config;

import com.vlinkage.xunyee.config.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Order(0)
@Component
@WebFilter(filterName = "IPFilter", urlPatterns = "/*")
public class IPFilter implements Filter {

    //单位时间内最大访问数
    private static final Integer MAX_COUNT = 3;
    //单位时间
    private static final Integer UNIT_TIME = 1 * 1000;
    //限制时长
    private static final Long REJECT_TIME = 1 * 60 * 1000L;

    @Autowired
    private RedisUtil redisUtil;
    private ApplicationContext context;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String ip = request.getRemoteAddr();
        //过滤黑名单
        if (redisUtil.hasKey("filter:ip:black:" + ip)) {
            log.error("ip访问过于频繁，已被限制=>" + ip + " 倒计时" + redisUtil.getExpire("filter:ip:black:" + ip));
            return;
        }
        //判断ip是否首次访问
        if (redisUtil.hExists("filter:ip:normal", "count:" + ip)) {
            //判断最大访问次数
            Integer maxCount = Integer.valueOf(redisUtil.hGet("filter:ip:normal", "count:" + ip).toString());
            log.info("ip：" + ip + " 访问" + maxCount + "次");
            if (maxCount > MAX_COUNT) {
                Long maxTime = Long.valueOf(redisUtil.hGet("filter:ip:normal", "time:" + ip).toString());
                if (System.currentTimeMillis() - maxTime < UNIT_TIME) {
                    log.error("ip访问过于频繁，已被限制=>" + ip + " 倒计时" + REJECT_TIME);
                    redisUtil.setEx("filter:ip:black:" + ip, "1", REJECT_TIME, TimeUnit.MILLISECONDS);
                    String str[] = {"count:" + ip, "time:" + ip};
                    redisUtil.hDelete("filter:ip:normal", str);
                    return;
                }
                initVisitsIP(ip);
            }
        } else {
            initVisitsIP(ip);
        }
        chain.doFilter(request, response);
        redisUtil.hincr("filter:ip:normal", "count:" + ip, 1);
    }

    /**
     * 初始化访问ip
     *
     * @param ip
     */
    private void initVisitsIP(String ip) {
        redisUtil.hPut("filter:ip:normal", "count:" + ip, "0");
        redisUtil.hPut("filter:ip:normal", "time:" + ip, String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void destroy() {

    }
}
