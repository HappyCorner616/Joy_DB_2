
<%@page import="com.mycompany.joy_db_2.model.sql.Column"%>
<%@page import="com.mycompany.joy_db_2.model.sql.Table"%>
<%@page import="com.mycompany.joy_db_2.model.sql.Schema"%>
<%@page import="java.util.List"%>
<%@page import="com.mycompany.joy_db_2.db.Requestor"%>
<h2>TEST</h2>

<%
    Requestor requestor = new Requestor();
    List<Schema> schemas = requestor.getAllSchemas();
%>
<%@include file="../WEB-INF/fragments/leftSidebarStart.jspf" %>
<div class="schemas">
<% for(Schema schema : schemas){ %>
<div class="schema" >
        <div class="lineElements">
            <img class="schemaImg" id="<%="schema_" + schema.getName()%>" src="../images/schemas.png" class="lineElement">
            <p class="lineElement"><%= schema.getName()%></p>
        </div>    
        <div class="tables" id="<%="schema_" + schema.getName() + "_tables"%>">
            <%for(Table table : schema.getTables()) {%> 
                <div class="table" >
                    <div class="lineElements">
                        <img class="tableImg" id="<%="table_" + table.getName()%>" src="../images/db_1.png" class="lineElement">
                        <a href="table.jsp?schemaName=<%=table.getSchemaName()%>&tableName=<%=table.getName()%>"  
                           class="lineElement"><%=table.getName()%>
                        </a>
                    </div>
                    <div class="columns" id="<%="table_" + table.getName() + "_columns"%>">
                        <%for(Column column : table.getColumns()){%>
                        <p class="lineElement"><%=column.toString()%></p><br>                        
                        <%}%>
                    </div>
                </div>
            <%}%>
        </div>
    </div>
<%}%>
</div>
<script>
$(document).ready(function(){
$("ul").hide();
$("p img").click(function(){
$(this).parent().next().slideToggle();
});
}); 
</script>
<%@include file="../WEB-INF/fragments/leftSidebarEnd.jspf" %>


