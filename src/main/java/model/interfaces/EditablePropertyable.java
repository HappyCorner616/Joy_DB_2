package model.interfaces;

public interface EditablePropertyable extends Propertyable {
    int typeForEditField();
    void setVal(Object val);
}
