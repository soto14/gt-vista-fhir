package ca.uhn.example.listener;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by es130 on 8/31/2016.
 */
public class SpringApplicationContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ApplicationContext ac = new ClassPathXmlApplicationContext("web.xml");
        servletContextEvent.getServletContext().setAttribute("applicationContext", ac);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
