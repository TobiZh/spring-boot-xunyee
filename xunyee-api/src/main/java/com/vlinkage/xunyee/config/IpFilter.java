package com.vlinkage.xunyee.config;
 
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义过滤器，用来判断IP访问次数是否超限
 * 如果前台用户访问网站的频率过快（达到超过50次/秒），则判定该ip恶意刷新操作，
 * 限制该IP的访问，1小时后自己解除限制
 * 
 * @author wukaikai
 *
 */

@Slf4j
@WebFilter(urlPatterns="/*")
public class IpFilter implements Filter{

    /**
     * 默认限制时间（单位：ms）3600000,3600(s),
     */
    private static final long LIMITED_TIME_MILLIS = 10 * 1000;

    /**
     * 用户连续访问最高阀值，超过该值则认定为恶意操作的IP，进行限制
     */
    private static final int LIMIT_NUMBER = 5;

    /**
     * 用户访问最小安全时间，在该时间内如果访问次数大于阀值，则记录为恶意IP，否则视为正常访问
     */
    private static final int MIN_SAFE_TIME = 5000;

    private FilterConfig config;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;    //设置属性filterConfig
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        ServletContext context = config.getServletContext();
        // 获取限制IP存储器：存储被限制的IP信息
        //Map<String, Long> limitedIpMap = (Map<String, Long>) context.getAttribute("limitedIpMap");
        ConcurrentHashMap<String ,Long> limitedIpMap = (ConcurrentHashMap<String, Long>)  context.getAttribute("limitedIpMap");
        // 过滤受限的IP
        filterLimitedIpMap(limitedIpMap);
        // 获取用户IP
        String ip = IPUtil.getRemoteIpAddr(request);
        System.err.println("ip:"+ip);
        // 判断是否是被限制的IP，如果是则跳到异常页面
        if (isLimitedIP(limitedIpMap, ip)) {
            long limitedTime = limitedIpMap.get(ip) - System.currentTimeMillis();
            // 剩余限制时间(用为从毫秒到秒转化的一定会存在些许误差，但基本可以忽略不计)
            request.setAttribute("remainingTime", ((limitedTime / 1000) + (limitedTime % 1000 > 0 ? 1 : 0)));
            System.err.println("ip访问过于频繁："+ip);
            throw new RuntimeException("ip访问过于频繁");
        }
        // 获取IP存储器
        ConcurrentHashMap<String, Long[]> ipMap = (ConcurrentHashMap<String, Long[]>) context.getAttribute("ipMap");
        // 判断存储器中是否存在当前IP，如果没有则为初次访问，初始化该ip
        // 如果存在当前ip，则验证当前ip的访问次数
        // 如果大于限制阀值，判断达到阀值的时间，如果不大于[用户访问最小安全时间]则视为恶意访问，跳转到异常页面
        if (ipMap.containsKey(ip)) {
            Long[] ipInfo = ipMap.get(ip);
            ipInfo[0] = ipInfo[0] + 1;
            log.debug("当前第[" + (ipInfo[0]) + "]次访问");
            if (ipInfo[0] > LIMIT_NUMBER) {
                Long ipAccessTime = ipInfo[1];
                Long currentTimeMillis = System.currentTimeMillis();

                log.debug("ip访问过于频繁：currentTimeMillis: "+currentTimeMillis+"  - ipAccessTime:"+ipAccessTime+" : " + (currentTimeMillis - ipAccessTime) + "<="+ MIN_SAFE_TIME);

                if (currentTimeMillis - ipAccessTime <= MIN_SAFE_TIME) {
                    limitedIpMap.put(ip, currentTimeMillis + LIMITED_TIME_MILLIS);
                    request.setAttribute("remainingTime", LIMITED_TIME_MILLIS);

                    log.debug("ip访问过于频繁：LIMITED_TIME_MILLIS:"+LIMITED_TIME_MILLIS);

                    log.debug("ip访问过于频繁："+ip);
                    throw new RuntimeException("ip访问过于频繁");
                } else {
                    initIpVisitsNumber(ipMap, ip);
                }
            }
        } else {
            initIpVisitsNumber(ipMap, ip);
            System.out.println("您首次访问该网站");
        }
        context.setAttribute("ipMap", ipMap);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub
    }

    /**
     * @Description 过滤受限的IP，剔除已经到期的限制IP
     * @param limitedIpMap
     */
    private void filterLimitedIpMap(ConcurrentHashMap<String, Long> limitedIpMap) {
        if (limitedIpMap == null) {
            return;
        }
        Set<String> keys = limitedIpMap.keySet();
        Iterator<String> keyIt = keys.iterator();
        long currentTimeMillis = System.currentTimeMillis();
        while (keyIt.hasNext()) {
            long expireTimeMillis = limitedIpMap.get(keyIt.next());
            log.debug("expireTimeMillis <= currentTimeMillis:"+ expireTimeMillis+" <="+ currentTimeMillis);
            if (expireTimeMillis <= currentTimeMillis) {
                keyIt.remove();
            }
        }
    }

    /**
     * @Description 是否是被限制的IP
     * @param limitedIpMap
     * @param ip
     * @return true : 被限制 | false : 正常
     */
    private boolean isLimitedIP(ConcurrentHashMap<String, Long> limitedIpMap, String ip) {
        if (limitedIpMap == null || ip == null) {
            // 没有被限制
            return false;
        }
        Set<String> keys = limitedIpMap.keySet();
        Iterator<String> keyIt = keys.iterator();
        while (keyIt.hasNext()) {
            String key = keyIt.next();
            if (key.equals(ip)) {
                // 被限制的IP
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化用户访问次数和访问时间
     *
     * @param ipMap
     * @param ip
     */
    private void initIpVisitsNumber(ConcurrentHashMap<String, Long[]> ipMap, String ip) {
        Long[] ipInfo = new Long[2];
        ipInfo[0] = 0L;// 访问次数
        ipInfo[1] = System.currentTimeMillis();// 初次访问时间
        ipMap.put(ip, ipInfo);
    }


}