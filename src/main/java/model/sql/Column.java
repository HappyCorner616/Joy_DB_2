
package model.sql;

import model.sql.enums.ColumnKeys;
import model.sql.enums.SqlDataTypes;
import model.interfaces.Nameable;
import model.interfaces.Propertyable;

import java.util.Comparator;


public class Column implements Nameable, Propertyable, Comparable<Column>{

    private String name;
    private SqlDataTypes type;
    private ColumnKeys key;
    private boolean autoIncrement;
    private int position;
    
    public Column(){
        name = "_";
        type = SqlDataTypes.VARCHAR;
    }
    
    public Column(String name){
        this.name = name;
        type = SqlDataTypes.VARCHAR;
    }
    
    public Column(String name, int position){
        this.name = name;
        this.position = position;
        type = SqlDataTypes.VARCHAR;
    }
    
    public Column(String name, SqlDataTypes type){
        this.name = name;
        this.type = type;
    }

    public Column(String name, SqlDataTypes type, ColumnKeys key, boolean autoIncrement, int position) {
        this.name = name;
        this.type = type;
        this.key = key;
        this.autoIncrement = autoIncrement;
        this.position = position;
    }
    
    @Override
    public String getName(){
        return name;
    }

    public SqlDataTypes getType() {
        return type;
    }

    public ColumnKeys getKey() {
        return key;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }
    
    public void setType(SqlDataTypes type){
        this.type = type;
    }

    @Override
    public String toString() {
        return "(" + type + ") " + name;
    }

    @Override
    public boolean equals(Object obj) {
       if(this == obj){
           return true;
       }
       if(obj == null){
           return false;
       }
       if(obj instanceof Column){
           Column other = (Column) obj;
           if(this.name.equalsIgnoreCase(other.name)){
               return true;
           }
       }
       return false;
    }
    
    public static SqlDataTypes mapType(String typeName){
        switch(typeName){
            case "shortint":
                return SqlDataTypes.SHORTINT;
            case "int":
                return SqlDataTypes.INT;
            case "biggint":
                return SqlDataTypes.BIGINT;
            case "varchar":
                return SqlDataTypes.VARCHAR;
            case "date":
                return SqlDataTypes.DATE;
            case "longblob":
                return SqlDataTypes.BLOB;
            default:
                return SqlDataTypes.VARCHAR;
        }
    }
    
    public static ColumnKeys mapKey(String key){
        switch(key){
            case "":
                return ColumnKeys.NONE;
            case "PRI":
                return ColumnKeys.PRI;
            case "UNI":
                return ColumnKeys.UNI;
            case "MUL":
                return ColumnKeys.MUL;
            default:
                return ColumnKeys.NONE;
        }
    }

    @Override
    public String getProperty() {
        return type.toString();
    }

    @Override
    public Object getPropertyVal() {
        return name;
    }

    @Override
    public int compareTo(Column o) {
        return this.position - o.position;
    }

    public static class PositionComparator implements Comparator<Column>{

        @Override
        public int compare(Column o1, Column o2) {
            return o1.position - o2.position;
        }
    }

}
