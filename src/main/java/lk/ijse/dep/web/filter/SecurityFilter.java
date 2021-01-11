package lk.ijse.dep.web.filter; /**
 * @author : Damika Anupama Nanayakkara <damikaanupama@gmail.com>
 * @since : 11/01/2021
 **/

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lk.ijse.dep.web.util.AppUtil;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter(filterName = "SecurityFilter" ,servletNames = {"TodoItemServlet","UserServlet"})//we can save servlets as an array which want this securityfilter in servletNames
public class SecurityFilter extends HttpFilter {//we should extend security filter to HttpFilter explicitly when we choose  filter
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (req.getServletPath().equals("/api/v1/auth") && req.getMethod().equals("POST")) {
            chain.doFilter(req, res);//let to go next filter
        } else {
            String authorization = req.getHeader("Authorization");//gets the authorization part from the header
            if (authorization == null || !authorization.startsWith("Bearer")) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                //jwt token //
                //we put jwt token in security filter for we can send re
                String token = authorization.replace("Bearer ", "");
                Jws<Claims> jws;
                try {
                    SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(AppUtil.getAppSecretKey()));
                    jws = Jwts.parserBuilder()
                            .setSigningKey(key)
                            .build()
                            .parseClaimsJws(token);
                    req.setAttribute("user", jws.getBody().get("name"));
                    chain.doFilter(req, res);
                } catch (JwtException ex) {
                    ex.printStackTrace();
                    res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                }
            }
        }
    }
}
