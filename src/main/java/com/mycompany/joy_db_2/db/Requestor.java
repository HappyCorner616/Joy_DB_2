
package com.mycompany.joy_db_2.db;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import com.mycompany.joy_db_2.model.sql.Cell;
import com.mycompany.joy_db_2.model.sql.Column;
import com.mycompany.joy_db_2.model.sql.Row;
import com.mycompany.joy_db_2.model.sql.Schema;
import com.mycompany.joy_db_2.model.sql.Table;
import java.sql.Blob;
import java.util.Collections;
import javafx.print.Collation;

public class Requestor {
    
    public List<Schema> getAllSchemas() throws Exception{
        Map<String, Schema> schemas = new TreeMap<>();
        
        Connection conn = getConnection();
        try {
            Statement statement = conn.createStatement();
            ResultSet res = statement.executeQuery(schemaInformationQuery());
            while(res.next()){
                
                String schemaName = res.getString("SCHEM_NAME");             
                Schema schema = schemas.get(schemaName);
                if(schema == null){
                    schema = new Schema(schemaName);
                    schemas.put(schemaName, schema);
                }
                
                String tableName = res.getString("TAB_NAME");
                if(tableName == null) continue;
                Table table = schema.getTable(tableName);
                if(table == null){
                    table = new Table(schema.getName(), tableName);
                    try {
                        schema.addTable(table);
                    } catch (Exception ex) {
                        Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                String columnName = res.getString("COL_NAME");
                if(columnName == null) continue;
                Column column = createColumnFromResultSet(res);
                
                try {
                    table.addColumn(column);
                } catch (Exception ex) {
                    Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception(ex.getMessage());
        }
        
        for(Schema s : schemas.values()){
            for(Table t : s.getTables()){
                Collections.sort(t.getColumns());
            }
        }

        return new ArrayList<>(schemas.values());
    }
    
    public Table getTable(String schemaName, String tableName, boolean filled){
        Table table = new Table(schemaName, tableName);
        
        try(Connection conn = getConnection()){
            PreparedStatement ps = conn.prepareStatement(tableInformationQuery(schemaName, tableName));
            ResultSet res = ps.executeQuery();
            while(res.next()){
                Column column = createColumnFromResultSet(res);
                table.addColumn(column);
            }
            
        } catch (SQLException ex) {        
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(filled){
            fillTable(table);
        }
              
        return table == null ? new Table() : table;
    }
        
    public Row addRow(Row row, String schemaName, String tableName) throws SQLException{
        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        
        StringBuilder colSb = new StringBuilder("INSERT INTO " + schemaName + "." + tableName + " (");
        StringBuilder dataSb = new StringBuilder("(");
        boolean firstVal = true;
        List<Cell> cells = row.getCells();
        for(int i = 0; i < cells.size(); i++){
            Cell cl = cells.get(i);
            Column cn = cl.getColumn();
            
            System.out.println("Column: " + cn.information());
            
            //большие данные пока не используем
            if(cn.isLOB())continue;
            
            if(cn.isInt() && cn.autoIncrement() && cl.intVal() == 0){
                continue;
            }
            
            if(firstVal){
                firstVal = false;               
            }else{
                colSb.append(", ");
                dataSb.append(", ");
            }
            
            colSb.append(cn.getName());
            if(cn.isInt()){
                dataSb.append(cl.intVal());
            }else if(cn.isDecimal()){
                dataSb.append(cl.decVal());
            }else{
                dataSb.append("'" + cl.getVal() + "'");
            }
            
        }
        colSb.append(") VALUES ");
        dataSb.append(")");
        System.out.println("SQL: " + colSb.toString() + dataSb.toString());
        int added = statement.executeUpdate(colSb.toString() + dataSb.toString(), Statement.RETURN_GENERATED_KEYS);
        ResultSet generated = statement.getGeneratedKeys();
        System.out.println("generated: " + generated.toString());
        
        return row;        
    }
    
    public Row updateRow(Row row, String schemaName, String tableName) throws SQLException{
        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        
        StringBuilder colSb = new StringBuilder("UPDATE " + schemaName + "." + tableName + " SET");
        StringBuilder dataSb = new StringBuilder();
        boolean firstVal = true;
        StringBuilder whereSb = new StringBuilder(" WHERE ");
        List<Cell> cells = row.getCells();
        for(int i = 0; i < cells.size(); i++){
            Cell cl = cells.get(i);
            Column cn = cl.getColumn();
            
            //большие данные пока не используем
            if(cn.isLOB())continue;
            
            if(cn.isPK()){
                whereSb.append(cn.getName() + "=");
                if(cn.isInt()){
                    whereSb.append(cl.intVal());
                }else if(cn.isDecimal()){
                    whereSb.append(cl.decVal());
                }else{
                    whereSb.append("'" + cl.getVal() + "'");
                }
                continue;
            }
            
            if(cn.autoIncrement())continue;
            
            if(firstVal){
                firstVal = false;               
            }else{
                dataSb.append(", ");
            }
            
            dataSb.append(" " + cn.getName() + "=");
            if(cn.isInt()){
                dataSb.append(cl.intVal());
            }else if(cn.isDecimal()){
                dataSb.append(cl.decVal());
            }else{
                dataSb.append("'" + cl.getVal() + "'");
            }
  
        }
        System.out.println("SQL: " + colSb.toString() + dataSb.toString() + whereSb.toString());
        int updated = statement.executeUpdate(colSb.toString() + dataSb.toString() + whereSb.toString(), Statement.RETURN_GENERATED_KEYS);
        ResultSet generated = statement.getGeneratedKeys();
        System.out.println("generated: " + generated.toString());
        
        return row;        
    }
    
    public int insertUpdateData(String query) throws SQLException{
        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        int res = statement.executeUpdate(query);
        return res;
    }
    
    private Column createColumnFromResultSet(ResultSet res){             
        try {
            String columnName = res.getString("COL_NAME");
            String type = res.getString("COL_TYPE");
            int numericPrecision = res.getInt("NUM_PRECISION");
            int numericScale = res.getInt("NUM_PRECISION");
            String key = res.getString("COL_KEY");
            int position = res.getInt("COL_POS");
            String extra = res.getString("COL_EXTRA");
            boolean autoincrement = false;
            if(extra != null && extra.equals("auto_increment")){
                autoincrement = true;
            }
            String refSchemaName = res.getString("KEY_SCHEMA");
            String refTableName = res.getString("KEY_TABLE");
            String refColumnName = res.getString("KEY_COLUMN");
            
            return new Column(columnName, type, key, autoincrement, position, numericPrecision, numericScale, refSchemaName, refTableName, refColumnName);
        } catch (SQLException ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);         
        }     
        return new Column();
    }
    
    private void fillTable(Table table){
        table.clearRows();
        Connection conn = getConnection();       
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(tableDataQuery(table.getSchemaName(), table.getName()));
            while(resultSet.next()){
                Row row = filledRow(table.getColumns(), resultSet);
                try {
                    table.addRow(row);
                } catch (Exception ex) {
                    Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Row filledRow(List<Column> columns, ResultSet resultSet){
        Row row = new Row();
        for(Column column : columns){
            try {
                if(column.isLOB()){
                    Blob blob = resultSet.getBlob(column.getName());
                    row.addCell(new Cell(column, blob.length()));
                }else if(column.isInt()){
                    row.addCell(new Cell(column, resultSet.getInt(column.getName())));
                }else if(column.isDecimal()){
                    row.addCell(new Cell(column, resultSet.getDouble(column.getName())));
                }else if(column.isString()){
                    row.addCell(new Cell(column, resultSet.getString(column.getName())));
                }else if(column.isDate()){
                    row.addCell(new Cell(column, resultSet.getString(column.getName())));
                }
            } catch (SQLException ex) {
                    Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        return row;
    }
    
    private String schemaInformationQuery(){
        String condition = "WHERE schms.SCHEMA_NAME NOT IN ('sys', 'mysql', 'information_schema', 'performance_schema')";
        return informationQuery(condition);
    }
    
    private String tableInformationQuery(String schemaName, String tableName){
        String condition = "WHERE schms.SCHEMA_NAME='" + schemaName + "' AND tabs.TABLE_NAME='" + tableName + "'";
        return informationQuery(condition);
    }
    
    private String informationQuery(String condition){
        return " SELECT schms.SCHEMA_NAME AS SCHEM_NAME,"
        + " tabs.TABLE_NAME AS TAB_NAME,"
        + " cols.COLUMN_NAME AS COL_NAME,"
        + " cols.COLUMN_TYPE AS COL_TYPE,"
        + " cols.NUMERIC_PRECISION AS NUM_PRECISION,"
        + " cols.NUMERIC_SCALE AS NUM_SCALE,"        
        + " cols.ORDINAL_POSITION AS COL_POS,"
        + " cols.COLUMN_KEY AS COL_KEY,"
        + " cols.EXTRA AS COL_EXTRA,"
        + " refs.REF_SCHM_NAME AS KEY_SCHEMA,"
        + " refs.REF_TML_NAME AS KEY_TABLE,"
        + " refs.REF_CLMN_NAME AS KEY_COLUMN"
        + " FROM information_schema.SCHEMATA AS schms"
        + " LEFT JOIN information_schema.tables AS tabs "
        + " ON schms.SCHEMA_NAME = tabs.TABLE_SCHEMA"
        + " LEFT JOIN information_schema.columns AS cols"
        + " ON tabs.TABLE_SCHEMA = cols.TABLE_SCHEMA"
        + " AND tabs.TABLE_NAME = cols.TABLE_NAME"
        + " LEFT JOIN (" + " SELECT refs.TABLE_SCHEMA AS SCHM_NAME,"
        + " refs.TABLE_NAME AS TBL_NAME,"
        + " refs.COLUMN_NAME AS CLMN_NAME,"
        + " refs.REFERENCED_TABLE_SCHEMA AS REF_SCHM_NAME,"
        + " refs.REFERENCED_TABLE_NAME AS REF_TML_NAME,"
        + " refs.REFERENCED_COLUMN_NAME AS REF_CLMN_NAME"
        + " FROM information_schema.KEY_COLUMN_USAGE AS refs"
        + " WHERE NOT ISNULL(refs.REFERENCED_TABLE_SCHEMA)"
        + " AND NOT ISNULL(refs.REFERENCED_TABLE_NAME)"
        + " AND NOT ISNULL(refs.REFERENCED_COLUMN_NAME))" + " AS refs"
        + " ON cols.TABLE_SCHEMA = refs.SCHM_NAME"
        + " AND cols.TABLE_NAME = refs.TBL_NAME"
        + " AND cols.COLUMN_NAME = refs.CLMN_NAME"
        + " " + condition + ";";
                
    }
    
    private String tableDataQuery(String schemaName, String tableName){
        return "SELECT * FROM " + schemaName + "." + tableName;
    }
    
    private Connection getConnection(){
       return getMySqlConnection();  
    }
    
    private Connection getMySqlConnection(){
        Connection conn = null;
        
        try {
//            InitialContext ic = new InitialContext();
//            DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/_MySQLSchemas");
//            conn = ds.getConnection();
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            //conn = DriverManager.getConnection("jdbc:mysql://SG-test-117-master.servers.mongodirector.com:3306/test?user=qwertyuser&password=Qwertyuser1!&useSSL=false");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=glassfishsql&password=qwerty&useSSL=false&serverTimezone=UTC");
        } catch (SQLException ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return conn;
    }
    
    
    
}

