package lk.ijse.dep.web.util;

import java.io.IOException;
import java.util.Properties;

/**
 * @author : Damika Anupama Nanayakkara <damikaanupama@gmail.com>
 * @since : 11/01/2021
 **/
public class AppUtil {
    public static String getAppSecretKey() throws IOException {
        Properties prop = new Properties();
        prop.load(AppUtil.class.getResourceAsStream("/application.properties"));
        return prop.getProperty("app.key");
    }
}









/*Here we put secret key access through this method
we have put out secret key in properties
* */
