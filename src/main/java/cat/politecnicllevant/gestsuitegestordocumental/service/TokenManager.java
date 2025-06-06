package cat.politecnicllevant.gestsuitegestordocumental.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;

@Service
public class TokenManager {


    @Value("${jwt.secret}")
    private String jwtSecret;

    public Claims getClaims(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        String token = auth.replace("Bearer ", "");
        return getClaims(token);
    }

    private Claims getClaims(String token) {
        Claims claims = null;
        try {
            Key key = Keys.hmacShaKeyFor(this.jwtSecret.getBytes());

            claims = Jwts.parser() // <== already returns JwtParserBuilder in 0.12.x
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return claims;
    }

}