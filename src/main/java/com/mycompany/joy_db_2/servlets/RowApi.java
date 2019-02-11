
package com.mycompany.joy_db_2.servlets;

import com.google.gson.Gson;
import com.mycompany.joy_db_2.model.ErrorResponse;
import com.mycompany.joy_db_2.db.Requestor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.mycompany.joy_db_2.model.MessageResponse;
import com.mycompany.joy_db_2.model.sql.Row;


public class RowApi extends HttpServlet {

    private Gson gson = new Gson();
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet RowApi</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet RowApi at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("Row POST");
        
        String schemaName = request.getHeader("schemaName");
        String tableName = request.getHeader("tableName");
        
        String requestBody = getRequestBody(request);
        Row row = gson.fromJson(requestBody, Row.class);
        
        boolean isSuccessful = true;
        String responseBody;
        Requestor requestor = new Requestor();
        
        if(schemaName == null || tableName == null || row == null){
            isSuccessful = false;
            response.setStatus(400);
            responseBody = gson.toJson(new ErrorResponse("wrong params"));
        }else{
            try {
                Row addedRow = requestor.addRow(row, schemaName, tableName);
                responseBody = gson.toJson(addedRow);
            } catch (SQLException e) {
                Logger.getLogger(RowApi.class.getName()).log(Level.SEVERE, null, e);
                response.setStatus(400);
                responseBody = gson.toJson(new ErrorResponse(e.getMessage()));
            }
        }
        
        try(PrintWriter out = response.getWriter()){
            out.println(requestBody);
        }
        
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Row PUT");
    }
    
    

    @Override
    public String getServletInfo() {
        return "Short description";
    }
    
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
