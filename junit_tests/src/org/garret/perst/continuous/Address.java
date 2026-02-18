package org.garret.perst.continuous;

import org.garret.perst.IValue;

class Address implements IValue { 
    @FullTextSearchable
    private String country;

    @FullTextSearchable
    private String city;

    @FullTextSearchable
    private String street;

    public String getCountry() { 
        return country;
    }

    public String getCity() { 
        return city;
    }

    public String getStreet() { 
        return street;
    }

    public String toString() { 
        return "country=" + country + ", city=" + city + ", street=" + street;
    }


    public Address(String country, String city,  String street) { 
        this.country = country;
        this.city = city;
        this.street = street;
    }
    
    private Address() {}
}
