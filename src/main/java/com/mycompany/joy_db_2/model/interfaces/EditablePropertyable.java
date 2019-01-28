package com.mycompany.joy_db_2.model.interfaces;

public interface EditablePropertyable extends Propertyable {
    int typeForEditField();
    void setVal(Object val);
}
