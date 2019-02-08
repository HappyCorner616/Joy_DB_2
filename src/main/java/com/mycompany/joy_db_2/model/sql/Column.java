
package com.mycompany.joy_db_2.model.sql;

import com.mycompany.joy_db_2.model.sql.enums.ColumnKeys;
import com.mycompany.joy_db_2.model.sql.enums.SqlDataTypes;
import com.mycompany.joy_db_2.model.interfaces.Nameable;
import com.mycompany.joy_db_2.model.interfaces.Propertyable;

import java.util.Comparator;


public class Column implements Comparable<Column>{

    private String name;
    private SqlDataTypes mainType;
    private String type;
    private boolean unsigned;
    private ColumnKeys key;
    private boolean autoIncrement;
    private int position;
    
    public Column(){
        name = "_";
        mainType = SqlDataTypes.STRING;
        type = "varchar(30)";
    }
    
    public Column(String name){
        this.name = name;
        mainType = SqlDataTypes.STRING;
        type = "varchar(30)";
    }

    public Column(String name, SqlDataTypes mainType, String type, boolean unsigned, ColumnKeys key, boolean autoIncrement, int position) {
        this.name = name;
        this.mainType = mainType;
        this.type = type.split(" ")[0];
        this.unsigned = unsigned;
        this.key = key;
        this.autoIncrement = autoIncrement;
        this.position = position;
    }
    
    public Column(String name, String type, ColumnKeys key, boolean autoIncrement, int position) {
        this.name = name;
        this.type = type.split(" ")[0];
        this.key = key;
        this.autoIncrement = autoIncrement;
        this.position = position;
        if(isLOB()){
            mainType = SqlDataTypes.LOB;
        }else if(isNumeric()){
            mainType = SqlDataTypes.NUMERIC;
        }else if(isString()){
            mainType = SqlDataTypes.STRING;
        }else if(isDate()){
            mainType = SqlDataTypes.DATE;
        }
        unsigned = type.contains("unsigned");
    }

    public boolean isNumeric(){
        if(type.contains("bit") || type.contains("int")
        || type.contains("decimal") || type.contains("float")
        || type.contains("double")){
            return true;
        }else{
            return false;
        }
    }
    
    public boolean isString(){
        if(type.contains("char") || type.contains("text") || type.contains("binary")){
            return true;
        }else{
            return false;
        }
    }
    
    public boolean isDate(){
        if(type.contains("date") || type.contains("time") || type.contains("year")){
            return true;
        }else{
            return false;
        }
    }
    
    public boolean isLOB(){
        if(type.contains("blob") || type.contains("longtext")){
            return true;
        }else{
            return false;
        }
    }

    public String getName() {
        return name;
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
        
    public static ColumnKeys mapKey(String key){
        switch(key){
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
