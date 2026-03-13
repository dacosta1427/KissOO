package org.garret.perst.impl;

class MultiFieldValue implements Comparable<MultiFieldValue> { 
    Comparable[] values;
    Object       obj;
    
    public int compareTo(MultiFieldValue f) { 
        for (int i = 0; i < values.length; i++) {
            int diff = values[i].compareTo(f.values[i]);
            if (diff != 0) { 
                return diff;
            }
        }
        return 0;
    }
    
    MultiFieldValue(Object obj, Comparable[] values) { 
        this.obj = obj;
        this.values = values;
    }
}