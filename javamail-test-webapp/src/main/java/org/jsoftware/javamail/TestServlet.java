package org.jsoftware.javamail;


import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class TestServlet extends HttpServlet {

    /** On tomcat @Resource loads global mailSession but should load those defined in context.xml */
	private transient Session mailSession;


    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse response) throws ServletException, IOException {
        httpServletRequest.getRequestDispatcher("/index.jsp").forward(httpServletRequest, response);
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse response) throws ServletException, IOException {
        MimeMessage message = new MimeMessage(mailSession);
        String to = httpServletRequest.getParameter("to");
        if (to == null || to.length() == 0) {
            throw new ServletException("No \"to\" parameter!");
        }
        try {
            Date now = new Date();
            String from = mailSession.getProperty("mail.from");
            if (from == null || from.length() == 0) {
                from = "test@test.com";
            }
            message.setFrom(new InternetAddress(from));
            InternetAddress[] address = { new InternetAddress(to) };
            message.setRecipients(Message.RecipientType.TO, address);
            message.setSubject("JavaMail Test - " + now);
            message.setSentDate(now);
            message.setText("This is test message sent by org.jsoftware.javamail:javamail-test-webapp.");
            mailSession.getTransport().sendMessage(message, address);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ServletException("Error sending mail!", ex);
        }
        doGet(httpServletRequest, response);
    }


    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
            mailSession = (Session) envCtx.lookup("mail/Session");
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }
}
