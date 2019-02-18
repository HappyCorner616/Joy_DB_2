package com.mycompany.joy_db_2.model.sql;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Row{

    private List<Cell> cells;

    public Row() {
        cells = new ArrayList<>();
    }

    public Row(List<Cell> cells) {
        this.cells = cells;
    }

    public void addCell(Cell cell){
        int index = cells.indexOf(cell);
        if(index >= 0){
            cells.set(index, cell);
        }else{
            cells.add(cell);
        }
    }

    public void setCellVal(String columnName, Object val){
        for(Cell c : cells){
            if(c.getColumn().getName().equalsIgnoreCase(columnName)){
                c.setVal(val);
                break;
            }
        }
    }
    
    public Object getVal(String columnName){
        for(Cell c : cells){
            if(c.getColumn().getName().equals(columnName)){
                return c.getVal();
            }
        }
        return null;
    }

    public Set<Column> columns(){
        Set<Column> columns = new HashSet<>();
        for(Cell c : cells){
            columns.add(c.getColumn());
        }
        return columns;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(Cell c : cells){
            sb.append("[" + c.toString() + "]");
        }
        return sb.toString();
    }

    public List<Cell> getCells(){
        List<Cell> list = new ArrayList<>(cells);
        return list;
    }

    public String getName() {
       if(cells.size() == 0){
           return "()";
       }
       for(Cell c : cells){
           if(c.getColumn().getName().equalsIgnoreCase("id")){
               return "(id) " + c.getVal();
           }
       }
       return "(" + cells.get(0).getColumn().getName() + ") " + cells.get(0).getVal();
    }

}