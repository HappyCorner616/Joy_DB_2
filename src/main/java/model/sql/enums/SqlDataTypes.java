
package model.sql.enums;


public enum SqlDataTypes {
    SHORTINT {public String toString(){return "shortint";}},
    INT {public String toString(){return "int";}},
    BIGINT {public String toString(){return "bigint";}},
    VARCHAR {public String toString(){return "varchar";}},
    DATE {public String toString(){return "date";}},
    BLOB {public String toString(){return "blob";}};
    

    public static boolean isNumberType(SqlDataTypes type){
        switch(type){
            case SHORTINT:
            case INT:
            case BIGINT:
                return true;
            default:
                return false;            
        }
    }
    
}
