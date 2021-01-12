package lk.ijse.dep.web.api; /**
 * @author : Damika Anupama Nanayakkara <damikaanupama@gmail.com>
 * @since : 11/01/2021
 **/

import lk.ijse.dep.web.dto.TodoItemDTO;
import lk.ijse.dep.web.util.Priority;
import lk.ijse.dep.web.util.Status;
import org.apache.commons.dbcp2.BasicDataSource;

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

@WebServlet(name = "TodoItemServlet", value = "/api/v1/items/*")
public class TodoItemServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");
        Jsonb jsonb = JsonbBuilder.create();
        if (request.getPathInfo() == null || request.getPathInfo().equals("/")) {
            try (Connection connection = cp.getConnection()) {
//                request.setAttribute("user", "admin");
                PreparedStatement pstm = connection.
                        prepareStatement("SELECT * FROM todo_item WHERE username = ?");
                pstm.setObject(1, request.getAttribute("user"));
                ResultSet rst = pstm.executeQuery();
                List<TodoItemDTO> items = new ArrayList<>();
                while (rst.next()) {
                    items.add(new TodoItemDTO(rst.getInt("id"),
                            rst.getString("text"),
                            Priority.valueOf(rst.getString("priority")),
                            Status.valueOf(rst.getString("status")),
                            rst.getString("username")));
                }
                response.setContentType("application/json");
                response.getWriter().println(jsonb.toJson(items));
            } catch (SQLException | JsonbException throwables) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                throwables.printStackTrace();
            }
        } else {
            try (Connection connection = cp.getConnection()) {
                int id = Integer.parseInt(request.getPathInfo().replace("/", ""));
//                request.setAttribute("user","admin");
                PreparedStatement pstm = connection.
                        prepareStatement("SELECT * FROM todo_item WHERE id=? AND username=?");
                pstm.setObject(1, id);
                pstm.setObject(2, request.getAttribute("user"));
                ResultSet rst = pstm.executeQuery();
                if (!rst.next()) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                } else {
                    response.setContentType("application/json");
                    TodoItemDTO item = new TodoItemDTO(rst.getInt("id"),
                            rst.getString("text"),
                            Priority.valueOf(rst.getString("priority")),
                            Status.valueOf(rst.getString("status")),
                            rst.getString("username"));
                    response.getWriter().println(jsonb.toJson(item));
                }
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            } catch (SQLException throwables) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                throwables.printStackTrace();
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Jsonb jsonb = JsonbBuilder.create();
//        System.out.println("/////////////////////////////////////////");
        try {
            TodoItemDTO item = jsonb.fromJson(request.getReader(), TodoItemDTO.class);//get the received json object as a TodoItemDTO
            //validation part
            if (item.getId() != null || item.getText() == null || item.getUsername() == null ||
                    item.getText().trim().isEmpty() || item.getUsername().trim().isEmpty()) {
                //item id shouldn't filled from json object it auto increments at DB
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);//404
                return;
            }
            BasicDataSource cp = (BasicDataSource) getServletContext().getAttribute("cp");//cp connection object from connection pool
            try (Connection connection = cp.getConnection()) {
                PreparedStatement pstm = connection.prepareStatement("SELECT * FROM `user` WHERE username = ?");
                pstm.setObject(1, item.getUsername());
                //check whether the user and todolist's user is same
                if (!pstm.executeQuery().next()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("text/plain");
                    response.getWriter().println("Invalid user");
                    return;
                }
                pstm = connection.prepareStatement("INSERT INTO todo_item (`text`, `priority`, `status`, `username`) VALUES (?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
                pstm.setObject(1, item.getText());
                pstm.setObject(2, item.getPriority().toString());
                pstm.setObject(3, item.getStatus().toString());
                pstm.setObject(4, item.getUsername());
                if (pstm.executeUpdate() > 0) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    ResultSet generatedKeys = pstm.getGeneratedKeys();
                    generatedKeys.next();//increment to next value in id
                    int generatedId = generatedKeys.getInt(1);
                    item.setId(generatedId);
                    response.setContentType("application/json");
                    response.getWriter().println(jsonb.toJson(item));
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }
            }
        } catch (JsonbException | SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
}
