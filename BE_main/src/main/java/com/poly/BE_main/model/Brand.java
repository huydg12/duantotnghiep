package com.poly.BE_main.model;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "BRAND")
public class Brand {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name="ID")
private Integer Id;

@Column(name="NAME")
private Integer Name;

@Column(name="DESCRIPTION")
private String Description;

public Brand() {
}

public Brand(Integer id, Integer name, String description) {
    Id = id;
    Name = name;
    Description = description;
}

public Integer getId() {
    return Id;
}

public void setId(Integer id) {
    Id = id;
}

public Integer getName() {
    return Name;
}

public void setName(Integer name) {
    Name = name;
}

public String getDescription() {
    return Description;
}

public void setDescription(String description) {
    Description = description;
}

}