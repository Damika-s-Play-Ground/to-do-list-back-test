package lk.ijse.dep.web.listner; /**
 * @author : Damika Anupama Nanayakkara <damikaanupama@gmail.com>
 * @since : 11/01/2021
 **/

import org.apache.commons.dbcp2.BasicDataSource;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

@WebListener
public class ContextListener implements ServletContextListener, HttpSessionListener, HttpSessionAttributeListener {

    public ContextListener() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("Initializing Connection pool..");
        Properties properties = new Properties();//connect with properties in resource bundle
        try {
            properties.load(this.getClass().getResourceAsStream("/application.properties"));
            BasicDataSource bds = new BasicDataSource();
            bds.setUsername(properties.getProperty("mysql.username"));
            bds.setPassword(properties.getProperty("mysql.password"));
            bds.setUrl(properties.getProperty("mysql.url"));
            bds.setDriverClassName(properties.getProperty("mysql.driver_class"));
            bds.setConnectionProperties("SSL=falsex");
            bds.setInitialSize(5);//total connections in c.pool
            bds.setMaxTotal(5);//Max connections for db(solution for data traffic)
            ServletContext stx = sce.getServletContext();//get the servlet context
            stx.setAttribute("cp",bds);//input data to s.context as key value pairs
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        BasicDataSource bds = (BasicDataSource) sce.getServletContext().getAttribute("cp");
        try {
            bds.close();
            System.out.println("Closing connection pool");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
