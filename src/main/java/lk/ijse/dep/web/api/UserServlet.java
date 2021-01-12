package lk.ijse.dep.web.api; /**
 * @author : Damika Anupama Nanayakkara <damikaanupama@gmail.com>
 * @since : 11/01/2021
 **/

import lk.ijse.dep.web.dto.UserDTO;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lk.ijse.dep.web.util.AppUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.dbcp2.BasicDataSource;

import javax.crypto.SecretKey;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "UserServlet", urlPatterns = {"/api/v1/users/*", "/api/v1/auth/"})//url pattern comes from authentication
public class UserServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        Jsonb jsonb = JsonbBuilder.create();
        UserDTO userDTO = jsonb.fromJson(request.getReader(), UserDTO.class);//get the received json object as an UserDTO
        try(Connection connection = cp.getConnection()) {
            PreparedStatement pstm = connection.prepareStatement("SELECT * FROM user WHERE username = ?");
            if (userDTO.getUserName()!=null) {
                pstm.setObject(1,request.getAttribute("user"));
            }
            ResultSet rst = pstm.executeQuery();
            List<UserDTO> user = new ArrayList<>();
            while (rst.next()){
                username
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
        try {
            UserDTO userDTO = jsonb.fromJson(request.getReader(), UserDTO.class);//get the received json object as an UserDTO
            //validation part
            if (userDTO.getUserName() == null || userDTO.getPassword() == null || userDTO.getUserName().trim().isEmpty() || userDTO.getPassword().trim().isEmpty()){
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
            try (Connection connection = cp.getConnection()) {
                //comes request to make JWS
                if (request.getServletPath().equals("/api/v1/auth")){
                    //gives credentials to jwt to authenticate
                    PreparedStatement pstm = connection.
                            prepareStatement("SELECT * FROM `user` WHERE username=?");
                    pstm.setObject(1, userDTO.getUserName());
                    ResultSet rst = pstm.executeQuery();
                    if (rst.next()){//gets node by node
                        //digest it's password in to sha256 value through sha algorithm
                        String sha256Hex = DigestUtils.sha256Hex(userDTO.getPassword());
                        //after digest a value we cannot get value from sha value(unidirectional function)
                        if (sha256Hex.equals(rst.getString("password"))){
                            //so we verify with object's digested password with real password(saved as a sha256 value)
                            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(AppUtil.getAppSecretKey()));//get Secret key
//  Creating a JWS
                            String jws = Jwts.builder()// (1)Use the Jwts.builder() method to create a JwtBuilder instance.
                                    .setIssuer("ijse")//(Standard Claim)    (2) Call JwtBuilder methods to add header parameters and claims as desired.
                                    .setExpiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * 24)))//(Standard Claim)
                                    .setIssuedAt(new java.util.Date())//(Standard Claim)
                                    .claim("name", userDTO.getUserName())
                                    .signWith(key)// (3)Specify the SecretKey or asymmetric PrivateKey you want to use to sign the JWT.
                                    .compact();// (4)Finally, call the compact() method to compact and sign, producing the final jws.

//
                            response.setContentType("text/plain");
                            response.getWriter().println(jws);

                        }else{
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                        }
                    }else{
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    }
                }else {
                    PreparedStatement pstm = connection.prepareStatement("SELECT * FROM `user` WHERE username=?");
                    pstm.setObject(1, userDTO.getUserName());
                    //check whether this user has been saved before
                    if (pstm.executeQuery().next()) {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("User already exists");
                        return;
                    }
                    pstm = connection.prepareStatement("INSERT INTO `user` VALUES (?,?)");
                    pstm.setObject(1, userDTO.getUserName());
                    String sha256Hex = DigestUtils.sha256Hex(userDTO.getPassword());
                    pstm.setObject(2, sha256Hex);
                    if (pstm.executeUpdate() > 0) {
                        response.setStatus(HttpServletResponse.SC_CREATED);
                    } else {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    }
                }
            } catch (SQLException throwables) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                throwables.printStackTrace();
            }

        }catch (JsonbException exp){
            exp.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
