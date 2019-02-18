package com.mycompany.joy_db_2.model.sql;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Schema {
    private String name;
    Map<String, Table> tables;
    
    public Schema(String name){
        this.name = name;
        tables = new TreeMap<>();
    }

    public String getName(){
        return name;
    }
    
    public void addTable(Table table) throws Exception{
        if(tables.containsKey(table.getName())){
            throw new Exception("Table already exist!");
        }
        tables.put(table.getName(), table);
    }
    
    public Table getTable(String tableName){
        return tables.get(tableName);
    }
    
    public List<Table> getTables(){
        return new ArrayList<>(tables.values());
    }

    @Override
    public String toString() {
        return "Schema{" +
                "name='" + name + '\'' +
                ", tables=" + getTables() +
                '}';
    }
}
