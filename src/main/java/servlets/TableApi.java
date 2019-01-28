
package servlets;

import com.google.gson.Gson;
import db.Requestor;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.sql.Table;


public class TableApi extends HttpServlet {

    private Gson gson = new Gson();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet TableApi</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet TableApi at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("request: " + request.toString());
        
        response.setContentType("application/json;charset=utf-8");
        String schemaName = request.getHeader("schemaName");
        String tableName = request.getHeader("tableName");
        boolean filled = Boolean.parseBoolean(request.getHeader("filled"));
        
        System.out.println("schemaName: " + schemaName + "; tableName: " + tableName + "; filled: " + filled);
        
        if(schemaName != null && tableName != null){
            Requestor requestor = new Requestor();
            Table table = requestor.getTable(schemaName, tableName, filled);
            String responseBody = gson.toJson(table);
            
            System.out.println("responseBody: " + responseBody);
            
            try(PrintWriter out = response.getWriter()){
                out.println(responseBody);
            }
            
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);        
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
