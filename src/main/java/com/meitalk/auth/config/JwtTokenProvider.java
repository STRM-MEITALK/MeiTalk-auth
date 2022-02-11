package com.meitalk.auth.config;

import com.meitalk.auth.exception.*;
import com.meitalk.auth.service.CustomUserDetailService;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret.key}")
    private String secretKey;


    //    access token 30s
//    private final long tokenValidTime = 30 * 1000L;
    //    access token 30m
    public final long tokenValidTime = 30 * 60 * 1000L;
    //    access token 24t
//    public final long tokenValidTime = 24 * 60 * 60 * 1000L;
    //    refresh token 1m
//    public final long refreshTokenValidTime = 60 * 1000L;
    //    refresh token 6t
//    public final long refreshTokenValidTime = 6 * 60 * 60 * 1000L;
    //    refresh token 48t
    public final long refreshTokenValidTime = 2 * 24 * 60 * 60 * 1000L;

    private final CustomUserDetailService userDetailsService;

    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    // JWT access token create
    public String createToken(String userId, String userEmail, String userName, String roles) {
        Claims claims = Jwts.claims().setSubject(userEmail);
        claims.put("userId", userId);
        claims.put("name", userName);
        claims.put("roles", roles);
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidTime)) // set Expire Time
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // JWT refresh token create
    public String createRefreshToken(String userId, String userEmail, String userName, String roles) {
        Claims claims = Jwts.claims().setSubject(userEmail);
        claims.put("userId", userId);
        Date now = new Date();
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenValidTime)) // set Expire Time
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // get info to JWT token
    public Authentication getAuthentication(String token) throws ExpireTokenException, UnSupportedTokenException, WrongTokenException, WrongTypeTokenException, UnknownErrorException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPk(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // get email to JWT token
    public String getUserPk(String token) throws ExpireTokenException, WrongTypeTokenException, UnSupportedTokenException, WrongTokenException, UnknownErrorException {
        String userPk = null;
        try {
            userPk = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody().getSubject();
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage());
            throw new ExpireTokenException("expired token");
        } catch (SecurityException | MalformedJwtException e) {
            log.error(e.getMessage());
            throw new WrongTypeTokenException("wrong type token");
        } catch (UnsupportedJwtException e) {
            log.error(e.getMessage());
            throw new UnSupportedTokenException("unsupported token");
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            throw new WrongTokenException("wrong token");
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new UnknownErrorException("unknown error");
        }
        return userPk;
    }

    // get token for request header. "X-AUTH-TOKEN" : "TOKEN'
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    // token valid + denied check
    public boolean validateToken(String jwtToken, HttpServletRequest request) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(jwtToken);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage());
            request.setAttribute("exception", ExceptionCode.EXPIRED_TOKEN.getCode());
        } catch (SecurityException | MalformedJwtException e) {
            log.error(e.getMessage());
            request.setAttribute("exception", ExceptionCode.WRONG_TYPE_TOKEN.getCode());
        } catch (UnsupportedJwtException e) {
            log.error(e.getMessage());
            request.setAttribute("exception", ExceptionCode.UNSUPPORTED_TOKEN.getCode());
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            request.setAttribute("exception", ExceptionCode.WRONG_TOKEN.getCode());
        } catch (Exception e) {
            log.error(e.getMessage());
            request.setAttribute("exception", ExceptionCode.UNKNOWN_ERROR.getCode());
        }
        return false;
    }

    // Bearer
    public String tokenBearer(String token) {
        return token.split("Bearer ")[1];
    }

}
