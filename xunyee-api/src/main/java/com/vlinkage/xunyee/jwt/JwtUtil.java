package com.vlinkage.xunyee.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Slf4j
public class JwtUtil {
    /**
     * 过期时间 7天过期
     * refresh 15天过期
     */
    public static final long ACCESS_EXPIRE_TIME = 1L * 60 * 60 * 24 * 1000;
    public static final long REFRESH_EXPIRE_TIME = 30L * 60 * 60 * 24 * 1000;


//    private static final long ACCESS_EXPIRE_TIME = 1L * 60 * 1000;
//    private static final long REFRESH_EXPIRE_TIME = 3L * 60 * 1000;
    /**
     * token私钥
     */
    private static final String TOKEN_SECRET = "DD5654D654DSD5S1D65S4D65S1D";


    /**
     * 生成 access token
     * @param userId
     * @return
     */
    public static String getAccessToken(String userId) {
        return getToken(userId,ACCESS_EXPIRE_TIME);
    }

    /**
     * 生成 refresh token
     * @param userId
     * @return
     */
    public static String getRefreshToken(String userId) {
       return getToken(userId,REFRESH_EXPIRE_TIME);
    }


    private static String getToken(String userId,long expireTime) {
        try {
            //过期时间
            Date date = new Date(System.currentTimeMillis() + expireTime);
            //私钥及加密算法
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            //设置头部信息
            Map<String, Object> header = new HashMap<>(2);
            header.put("typ", "JWT");
            header.put("alg", "hs256");
            //附带userName userId信息，生成签名
            return JWT.create()
                    .withHeader(header)
                    .withClaim("userId", userId)
                    .withIssuedAt(new Date())
                    .withExpiresAt(date)
                    .sign(algorithm);

        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * 校验token是否正确
     * 是否还有效
     * @param token 密钥
     * @return 是否正确
     */
    public static boolean verify(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(TOKEN_SECRET);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build();
            DecodedJWT JWT = verifier.verify(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }



    /**
     * 验证token是否过期失效
     * true 失效 false 还有效
     * @param token
     * @return
     */
    public static boolean isTokenExpired(String token) {
        try {
            DecodedJWT decode = JWT.decode(token);
            boolean isExp=decode.getExpiresAt().before(new Date());
            return isExp;
        }catch (Exception e){
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 获取token中的信息无需secret解密也能获取
     *
     * @param token 密钥
     * @return token中包含的用户ID
     */
    public static String getUserId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("userId").asString();
        } catch (JWTDecodeException ex) {
            return null;
        }
    }



    public static void main(String[] args) {
        Date date = new Date(System.currentTimeMillis() + ACCESS_EXPIRE_TIME);
        SimpleDateFormat sd=new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

        System.out.println(ACCESS_EXPIRE_TIME);
        System.out.println(sd.format(date));

        System.out.println(getAccessToken("20"));
        System.out.println(getRefreshToken("20"));
    }
}