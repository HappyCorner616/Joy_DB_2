
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

public class Requestor {
    
    private static final String HEROKU_URI = "postgres://mhymvbvcmdathp:a4f43202bd4b441a0da9a4418c2aaae9517bc3476e4de22e7b14500382dd1777@ec2-54-243-223-245.compute-1.amazonaws.com:5432/dbiifilj2htvrs";
    private static final String HEROKU_USER = "mhymvbvcmdathp";
    private static final String HEROKU_PASSWORD = "a4f43202bd4b441a0da9a4418c2aaae9517bc3476e4de22e7b14500382dd1777";
    private static final int HEROKU_MODE = 1;
    private static final int LOCAL_MODE = 2;
    
    //private static final int CURRRENT_MODE = HEROKU_MODE;
    private static final int CURRRENT_MODE = LOCAL_MODE;
    
    public List<Schema> getAllSchemas() throws Exception{
        Map<String, Schema> schemas = new TreeMap<>();
        
        Connection conn = getConnection();
        try {
            Statement statement = conn.createStatement();
            ResultSet res = statement.executeQuery(getSchemaQuery());
            while(res.next()){
                
                String schemaName = res.getString("SCHEM_NAME");             
                Schema schema = schemas.get(schemaName);
                if(schema == null){
                    schema = new Schema(schemaName);
                    schemas.put(schemaName, schema);
                }
                
                String tableName = res.getString("TAB_NAME");
                Table table = schema.getTable(tableName);
                if(table == null){
                    table = new Table(schema.getName(), tableName);
                    try {
                        schema.addTable(table);
                    } catch (Exception ex) {
                        Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                
                Column column = getColumnFromResultSet(res);
                
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

        return new ArrayList<>(schemas.values());
    }
    
    public Table getTable(String schemaName, String tableName, boolean filled){
        Table table = new Table(schemaName, tableName);
        
        try(Connection conn = getConnection()){
            String query = getTableQuery(schemaName) + " AND tabs.TABLE_NAME = '" + tableName + "'";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet res = ps.executeQuery();
            while(res.next()){
                Column column = getColumnFromResultSet(res);
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
    
    private Column getColumnFromResultSet(ResultSet res){             
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
            return new Column(columnName, type, key, autoincrement, position, numericPrecision, numericScale);
        } catch (SQLException ex) {
            Logger.getLogger(Requestor.class.getName()).log(Level.SEVERE, null, ex);         
        }     
        return new Column();
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
    
    public Row addRow(Row row, String schemaName, String tableName) throws SQLException{
        Connection conn = getConnection();
        Statement statement = conn.createStatement();
        
        StringBuilder colSb = new StringBuilder("INSERT INTO " + schemaName + "." + tableName + " (");
        StringBuilder dataSb = new StringBuilder("(");
        List<Cell> cells = row.getCells();
        for(int i = 0; i < cells.size(); i++){
            Cell cl = cells.get(i);
            Column cn = cl.getColumn();
            
            if(cn.isInt() && cn.autoIncrement() && cl.intVal() > 0){
                continue;
            }
            
            colSb.append(cn.getName());
            if(cn.isInt()){
                dataSb.append(cl.intVal());
            }else if(cn.isDecimal()){
                dataSb.append(cl.decVal());
            }else{
                dataSb.append("'" + cl.getVal() + "'");
            }
  
            if(i < cells.size() - 1){
                colSb.append(", ");
                dataSb.append(", ");
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
    
    private String getSchemaQuery(){
        return "SELECT tabs.TABLE_SCHEMA AS SCHEM_NAME,"
        + " tabs.TABLE_NAME AS TAB_NAME,"
        + " cols.COLUMN_NAME AS COL_NAME,"
        + " cols.COLUMN_TYPE AS COL_TYPE,"
        + " cols.NUMERIC_PRECISION AS NUM_PRECISION,"
        + " cols.NUMERIC_SCALE AS NUM_SCALE,"        
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
        + " cols.COLUMN_TYPE AS COL_TYPE,"
        + " cols.NUMERIC_PRECISION AS NUM_PRECISION,"
        + " cols.NUMERIC_SCALE AS NUM_SCALE,"
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
       if(CURRRENT_MODE == LOCAL_MODE){
           return getMySqlConnection();
       }else{
           return getPostgreSqlConnection();
       }      
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
    
    private Connection getPostgreSqlConnection(){
        Connection conn = null;
        
        try {
//            InitialContext ic = new InitialContext();
//            DataSource ds = (DataSource) ic.lookup("java:comp/env/jdbc/_MySQLSchemas");
//            conn = ds.getConnection();
            Class.forName("org.postgresql.Driver").newInstance();
            conn = DriverManager.getConnection("postgres://mhymvbvcmdathp:a4f43202bd4b441a0da9a4418c2aaae9517bc3476e4de22e7b14500382dd1777@ec2-54-243-223-245.compute-1.amazonaws.com:5432/dbiifilj2htvrs");
            //conn = DriverManager.getConnection("jdbc.mysql://localhost:3306/?udeSSL=false&serverTmeZome=Europe/Moscow", "glassfishsql", "qwerty");
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

