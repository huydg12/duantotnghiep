package com.poly.BE_main.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ROLE")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int Id;
    @Column(name = "ROLE_NAME")
    private String RoleName;
    @Column(name = "DESCRIPTION")
    private String Description;
    public Role() {
    }
    public Role(int id, String roleName, String description) {
        Id = id;
        RoleName = roleName;
        Description = description;
    }
    public int getId() {
        return Id;
    }
    public void setId(int id) {
        Id = id;
    }
    public String getRoleName() {
        return RoleName;
    }
    public void setRoleName(String roleName) {
        RoleName = roleName;
    }
    public String getDescription() {
        return Description;
    }
    public void setDescription(String description) {
        Description = description;
    }

    
}
