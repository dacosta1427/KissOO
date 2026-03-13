package org.garret.perst.impl;

import org.garret.perst.*;

class FieldValue implements Comparable<FieldValue> { 
    Comparable value;
    Object     obj;
    
    public int compareTo(FieldValue f) { 
        return value.compareTo(f.value);
    }
    
    FieldValue(Object obj, Object value) { 
        this.obj = obj;
        this.value = (Comparable)value;
    }
}