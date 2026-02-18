package org.garret.perst.continuous;

import org.garret.perst.*;

class Employee extends CVersion
{ 
    @FullTextSearchable
    @Indexable(unique=true)
    private String name;

    private int age;

    @Indexable
    private CVersionHistory<Company> company;

    public String getName() { 
        return name;
    }

    public int getAge() { 
        return age;
    }

    public Company getCompany() { 
        return company.getCurrent();
    }

    public void setCompany(Company company) { 
        this.company = company.getVersionHistory();
    }

    public Employee(String name, int age, Company company) { 
        this.name = name;
        this.age = age;
        setCompany(company);
    }

    Employee() {}

    public String toString() { 
        return "name='" + getName() + "', age=" + getAge() + ", company=" + getCompany().getName();
    }
}
