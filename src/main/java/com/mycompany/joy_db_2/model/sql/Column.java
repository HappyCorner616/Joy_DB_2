
package com.mycompany.joy_db_2.model.sql;

import com.mycompany.joy_db_2.model.interfaces.Nameable;
import com.mycompany.joy_db_2.model.interfaces.Propertyable;

import java.util.Comparator;


public class Column implements Comparable<Column>{

    public static final int INT = 0;
    public static final int DECIMAL = 1;
    public static final int STRING = 2;
    public static final int LOB = 3;
    public static final int DATE = 4;
    
    private String name;
    private int position;
    private int mainType;
    private String type;
    private boolean unsigned;
    private int precision;
    private int scale;
    private String key;
    private boolean autoIncrement; 
    
    public Column(){
        name = "_error_";
        type = "_error_";
        position = 0;
        mainType = INT;
        precision = 0;
        scale = 0;
        unsigned = false;
        autoIncrement = false;
    }
    
    public Column(String name, String type, String key, boolean autoIncrement, int position, int numericPrecision, int numericScale) {
        this.name = name;
        this.type = type.split(" ")[0];
        this.key = key;
        this.autoIncrement = autoIncrement;
        this.position = position;
        unsigned = type.contains("unsigned");
        if(type.contains("blob") || type.contains("longtext")){
            mainType = LOB;
        }else if(type.contains("bit") || type.contains("int")){
            mainType = INT;
            precision = numericPrecision;
        }else if(type.contains("decimal") || type.contains("float") || type.contains("double")){
            mainType = DECIMAL;
            precision = numericPrecision;
            scale = numericScale;
        }else if(type.contains("char") || type.contains("text") || type.contains("binary")){
            mainType = STRING;
        }else if(type.contains("date") || type.contains("time") || type.contains("year")){
            mainType = DATE;
        }      
    }

    public int getMainType() {
        return mainType;
    }

    public String getName() {
        return name;
    }
    
    public boolean isInt(){
        return mainType == INT;
    }
    
    public boolean isDecimal(){
        return mainType == DECIMAL;
    }
    
    public boolean isString(){
        return mainType == STRING;
    }
    
    public boolean isLOB(){
        return mainType == LOB;
    }
    
    public boolean isDate(){
        return mainType == DATE;
    }
    
    public boolean autoIncrement(){
        return autoIncrement;
    }
    
    public String information(){
        return type
                + " " + (unsigned ? "unsigned" : "")
                + " " + key;
    }
    
    public boolean isPK(){
        return key.equals("PRI");
    }

    public boolean unsigned(){
        return unsigned;
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
