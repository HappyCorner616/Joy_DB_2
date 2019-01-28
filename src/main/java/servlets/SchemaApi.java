/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import com.google.gson.Gson;
import db.Requestor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.sql.Schema;
import model.Schemas;


public class SchemaApi extends HttpServlet {

    private Gson gson = new Gson();
    
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet SchemaApi</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet SchemaApi at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        System.out.println("request: " + request.toString());
        
        response.setContentType("application/json;charset=utf-8");
        Requestor requestor = new Requestor();
        List<Schema> list = requestor.getAllSchemas();
        
        //System.out.println("list: " + list);
        
        Schemas schemas = new Schemas(list);
        String responseBody = gson.toJson(schemas);
        
        //System.out.println("responseBody: " + responseBody);
        
        try(PrintWriter out = response.getWriter()){
            out.println(responseBody);
        }
        
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }


    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
