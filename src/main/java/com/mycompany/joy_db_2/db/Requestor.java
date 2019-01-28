
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
import com.mycompany.joy_db_2.model.sql.enums.ColumnKeys;
import com.mycompany.joy_db_2.model.sql.enums.SqlDataTypes;

public class Requestor {
    
    public List<Schema> getAllSchemas(){
        Map<String, Schema> schemas = new TreeMap<>();
        
        Connection conn = getConnection();
        try {
            Statement statement = conn.createStatement();
            ResultSet res = statement.executeQuery(getSchemaQuery());
            while(res.next()){
                String schemaName = res.getString("SCHEM_NAME");
                String tableName = res.getString("TAB_NAME");
                String columnName = res.getString("COL_NAME");
                
                Schema schema = schemas.get(schemaName);
                if(schema == null){
                    schema = new Schema(schemaName);
                    schemas.put(schemaName, schema);
                }
                
                Table table = schema.getTable(tableName);
                if(table == null){
                    table = new Table(schema.getName(), tableName);
                    try {
                        schema.addTable(table);
                    } catch (Exception ex) {
                        Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
               
                SqlDataTypes type = Column.mapType(res.getString("COL_TYPE"));
                ColumnKeys key = Column.mapKey(res.getString("COL_KEY"));
                int position = res.getInt("COL_POS");
                String extra = res.getString("COL_EXTRA");
                boolean autoincrement = false;
                if(extra != null && extra.equals("auto_increment")){
                    autoincrement = true;
                }
                
                Column column = new Column(columnName, type, key, autoincrement, position);
                
                try {
                    table.addColumn(column);
                } catch (Exception ex) {
                    Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        } catch (SQLException ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new ArrayList<>(schemas.values());
    }
    
    public Table getTable(String schemaName, String tableName, boolean filled){
        Table table = new Table(schemaName, tableName);
        
        try(Connection conn = getConnection()){
            String query = getTableQuery(schemaName) + " AND tabs.TABLE_NAME = '" + tableName + "'";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet res = ps.executeQuery();
            while(res.next()){
                String columnName = res.getString("COL_NAME");
                SqlDataTypes type = Column.mapType(res.getString("COL_TYPE"));
                ColumnKeys key = Column.mapKey(res.getString("COL_KEY"));
                int position = res.getInt("COL_POS");
                String extra = res.getString("COL_EXTRA");
                boolean autoincrement = false;
                if(extra != null && extra.equals("auto_increment")){
                    autoincrement = true;
                }
                Column column = new Column(columnName, type, key, autoincrement, position);
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
    
    public void fillTable(Table table){
        table.clearRows();
        Connection conn = getConnection();       
        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(getRowsQuery(table.getSchemaName(), table.getName()));
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
                switch(column.getType()){
                    case VARCHAR:            
                        row.addCell(new Cell(column, resultSet.getString(column.getName())));
                        break;
                    case SHORTINT:
                    case INT:
                    case BIGINT:
                        row.addCell(new Cell(column, resultSet.getInt(column.getName())));
                        break;
                    case DATE:
                        row.addCell(new Cell(column, resultSet.getString(column.getName())));
                        break;
                    case BLOB:
                        row.addCell(new Cell(column, resultSet.getBlob(column.getName())));
                        break;
                }
            } catch (SQLException ex) {
                    Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
            }            
        }
        return row;
    }
    
    public boolean addRow(Row row, String schemaName, String tableName) throws SQLException{
        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        
        StringBuilder sb = new StringBuilder("INSERT INTO " + schemaName + "." + tableName + " (");
        StringBuilder dataSb = new StringBuilder("(");
        List<Cell> cells = row.getCells();
        for(int i = 0; i < cells.size(); i++){
            Cell c = cells.get(i);
            if(c.getColumn().getName().equalsIgnoreCase("id")) continue;
            
            sb.append(c.getProperty());
            if(SqlDataTypes.isNumberType(c.getColumn().getType())){
                dataSb.append(c.getPropertyVal());
            }else{
                dataSb.append("'" + c.getPropertyVal() + "'");
            }
  
            if(i < cells.size() - 1){
                sb.append(", ");
                dataSb.append(", ");
            }
        }
        sb.append(") VALUES ");
        dataSb.append(")");
        System.out.println("SQL: " + sb.toString() + dataSb.toString());
        int added = statement.executeUpdate(sb.toString() + dataSb.toString());
        
        return added > 0;        
    }
    
    private String getSchemaQuery(){
        return "SELECT tabs.TABLE_SCHEMA AS SCHEM_NAME,"
        + " tabs.TABLE_NAME AS TAB_NAME,"
        + " cols.COLUMN_NAME AS COL_NAME,"
        + " cols.DATA_TYPE AS COL_TYPE,"
        + " cols.ORDINAL_POSITION AS COL_POS,"
        + " cols.COLUMN_KEY AS COL_KEY,"
        + " cols.EXTRA AS COL_EXTRA"
        + " FROM information_schema.tables AS tabs"
        + " LEFT JOIN information_schema.columns AS cols"
        + " ON tabs.TABLE_SCHEMA = cols.TABLE_SCHEMA"
        + " AND tabs.TABLE_NAME = cols.TABLE_NAME"
        + " WHERE tabs.TABLE_SCHEMA NOT IN ('sys', 'mysql', 'information_schema', 'performance_schema')";
    }
    
    private String getTableQuery(String schemaName){
        return "SELECT tabs.TABLE_NAME AS TAB_NAME,"
        + " cols.COLUMN_NAME AS COL_NAME,"
        + " cols.DATA_TYPE AS COL_TYPE,"
        + " cols.ORDINAL_POSITION AS COL_POS,"
        + " cols.COLUMN_KEY AS COL_KEY,"
        + " cols.EXTRA AS COL_EXTRA"
        + " FROM information_schema.tables AS tabs"
        + " LEFT JOIN information_schema.columns AS cols"
        + " ON tabs.TABLE_SCHEMA = cols.TABLE_SCHEMA"
        + " AND tabs.TABLE_NAME = cols.TABLE_NAME"
        + " WHERE tabs.TABLE_SCHEMA = '" + schemaName + "'";
    }
    
    private String getRowsQuery(String schemaName, String tableName){
        return "SELECT * FROM " + schemaName + "." + tableName;
    }
    
    private Connection getConnection(){
        Connection conn = null;
        
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/_MySQLSchemas");
            conn = ds.getConnection();
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //conn = DriverManager.getConnection("jdbc:mysql://SG-test-117-master.servers.mongodirector.com:3306/test?user=qwertyuser&password=Qwertyuser1!&useSSL=false");
        } catch (SQLException ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return conn;
    }
    
}

