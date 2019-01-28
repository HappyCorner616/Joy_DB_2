
package com.mycompany.joy_db_2.model;

import com.mycompany.joy_db_2.model.sql.Schema;

import java.util.ArrayList;
import java.util.List;


public class Schemas {
    private List<Schema> schemas;

    public Schemas() {
        schemas = new ArrayList<>();
    }

    public Schemas(List<Schema> schemas) {
        this.schemas = schemas;
    }
    
    public List<Schema> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<Schema> schemas) {
        this.schemas = schemas;
    }

    @Override
    public String toString() {
        return "Schemas{" + "schemas=" + schemas + '}';
    }
   
}
