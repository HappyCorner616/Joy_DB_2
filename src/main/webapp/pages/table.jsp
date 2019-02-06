
<%@page import="com.mycompany.joy_db_2.model.sql.Cell"%>
<%@page import="java.util.List"%>
<%@page import="com.mycompany.joy_db_2.model.sql.Row"%>
<%@page import="com.mycompany.joy_db_2.model.sql.Column"%>
<%@page import="com.mycompany.joy_db_2.db.Requestor"%>
<%@page import="com.mycompany.joy_db_2.model.sql.Table"%>
<%@include file="../WEB-INF/fragments/leftSidebarStart.jspf"%>


<%@include file="../WEB-INF/fragments/leftSidebarEnd.jspf"%>

<%
    String tableName = request.getParameter("tableName");
    String schemaName = request.getParameter("schemaName");
    Table table = new Requestor().getTable(schemaName, tableName, true);
%>

<div class="tableFrame">
    <div class="tableTitles">
        <% for(Column col : table.getColumns()){ %>
        <div class="columnTitle"><%=col.getName()%></div>
        <%}%>
    </div>
    <%List<Row> rows = table.getRows(); 
    for(Row row : rows){%>
        <%List<Cell> cells = row.getCells();
        for(Cell cell : cells){%>
        <div class="rowCell"><%=cell.getPropertyVal()%></div>
        <%}%>
        <br>
    <%}%>
</div>