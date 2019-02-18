
package com.mycompany.joy_db_2.servlets;

import com.mycompany.joy_db_2.db.Requestor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ResponseStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class SqlRequestApi extends HttpServlet {


    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet SqlRequestApi</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet SqlRequestApi at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        System.out.println("SQL_REQUEST_GET");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        System.out.println("SQL_REQUEST_POST");

        String requestBody = getRequestBody(request);
        boolean successful = true;
        String responseBody = "";
        Requestor requestor = new Requestor();
        
        try {
            int res = requestor.insertUpdateData(requestBody);
            responseBody = "" + res + " rows were updatre";
        } catch (SQLException e) {
            Logger.getLogger(SqlRequestApi.class.getName()).log(Level.SEVERE, null, e);
            successful = false;
            responseBody = e.getMessage();
            response.setStatus(402);
        }
        
        
        
        try(PrintWriter out = response.getWriter()){
            out.println(responseBody);
        }
        
        
        
    }


    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    
    public String getRequestBody(HttpServletRequest request) throws IOException{
        InputStreamReader isr = new InputStreamReader(request.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        StringBuilder sb = new StringBuilder();
        String line = br.readLine();
        while(line != null){
            sb.append(line);
            line = br.readLine();
        }
        isr.close();
        br.close();        
        
        System.out.println("request = " + sb.toString());      
        return sb.toString();
    }
    
}
