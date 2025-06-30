package com.poly.BE_main.model;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "COLOR")
public class Color {
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
@Column(name="ID")
private Integer Id;
@Column(name = "NAME")
private String Name;
@Column(name = "DESCRIPTION")
private String Description;
@Column(name = "CODE")
private String Code;
public Color() {
}
public Color(Integer id, String name, String description, String code) {
    Id = id;
    Name = name;
    Description = description;
    Code = code;
}
public Integer getId() {
    return Id;
}
public void setId(Integer id) {
    Id = id;
}
public String getName() {
    return Name;
}
public void setName(String name) {
    Name = name;
}
public String getDescription() {
    return Description;
}
public void setDescription(String description) {
    Description = description;
}
public String getCode() {
    return Code;
}
public void setCode(String code) {
    Code = code;
}

}