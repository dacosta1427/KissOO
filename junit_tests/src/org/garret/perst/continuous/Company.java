package org.garret.perst.continuous;

import org.garret.perst.*;

class Company extends CVersion
{
    @FullTextSearchable
    @Indexable(unique=true)
    private String name;

    @FullTextSearchable
    private Address location;    

    public String getName() { 
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getLocation() { 
        return location;
    }

    public void setLocation(Address location) {
        this.location = location;
    }

    public IterableIterator<Employee> getEmployees() { 
        return CDatabase.instance.find(Employee.class, "company", new Key(this));
    }

    public String toString() { 
        return "name='" + name + "', " + location;
    }

    public Company(String name, Address location) { 
        this.name = name;                      
        this.location = location;
    }

    Company() {}
}
