package com.mycompany.joy_db_2.model.sql;

public class Cell implements Comparable<Cell>{

    private Column column;
    private int iVal;
    private double dVal;
    private String sVal;
    private long lVal;

    public Cell(Column column, Object val) {
        this.column = column;
        setVal(val);
    }

    public void setVal(Object val) {
        if(getColumn().isInt()){
            if(val instanceof String){
                iVal = Integer.parseInt((String)val);
            }else if(val instanceof Double){
                iVal = ((Double)val).intValue();
            }else if(val instanceof Integer){
                iVal = (Integer)val;
            }
        }else if(getColumn().isDecimal()){
            if(val instanceof String){
                dVal = Double.parseDouble((String)val);
            }else if(val instanceof Double){
                dVal = (Double)val;
            }else if(val instanceof Integer){
                dVal = (Integer)val;
            }
        }else if(column.isLOB()){
            if(val instanceof String){
                lVal = Long.parseLong((String)val);
            }else if(val instanceof Double){
                lVal = ((Double)val).longValue();
            }else if(val instanceof Long){
                lVal = (Long)val;
            }
        }else{
            this.sVal = String.valueOf(val);
        }
    }

    public Column getColumn() {
        return column;
    }

    public Object getVal() {
        if(column.isInt()){
            return iVal;
        }else if(column.isDecimal()){
            return dVal;
        }else if(column.isLOB()){
            return lVal;
        }else{
            return sVal;
        }
    }

    public int intVal(){
        return iVal;
    }

    public double decVal(){
        return dVal;
    }

    public Cell copy(){
        return new Cell(column.copy(), getVal());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        if(this == obj) return true;
        if(obj instanceof Cell){
            Cell tmp = (Cell)obj;
            return this.column.equals(tmp.column);
        }
        return false;
    }

    @Override
    public String toString() {
        return column.getName() + "-" + getVal(); 
    }
    
    @Override
    public int compareTo(Cell c) {
        return this.column.compareTo(c.column);
    }

}
