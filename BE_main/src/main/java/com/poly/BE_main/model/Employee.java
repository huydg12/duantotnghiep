package com.poly.BE_main.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table (name = "EMPLOYEE", schema = "dbo")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer Id;

    @Column(name = "ACCOUNT_ID")
    private Integer AccountId;

    @Column(name = "FULL_NAME", length = 255)
    private String FullName;

    @Column(name = "GENDER", length = 45)
    private String Gender;

    @Column(name = "EMAIL", length = 255)
    private String Email;

    @Column(name = "NUMBER_PHONE", length = 45)
    private String NumberPhone;

    @Column(name = "BIRTH_OF_DATE")
    private LocalDate BirthOfDate;

    @Column(name = "IS_ACTIVE")
    private Boolean IsActive;

    @Column(name = "CREATED_BY")
    private String CreatedBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime CreatedDate;

    public Employee(){
    }

    public Employee(Integer id, Integer accountId, String fullName, String gender, String email,
                    String numberPhone, LocalDate birthOfDate, Boolean isActive,
                    String createdBy, LocalDateTime createdDate) {
        Id = id;
        AccountId = accountId;
        FullName = fullName;
        Gender = gender;
        Email = email;
        NumberPhone = numberPhone;
        BirthOfDate = birthOfDate;
        IsActive = isActive;
        CreatedBy = createdBy;
        CreatedDate = createdDate;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public Integer getAccountId() {
        return AccountId;
    }

    public void setAccountId(Integer accountId) {
        AccountId = accountId;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getGender() {
        return Gender;
    }

    public void setGender(String gender) {
        Gender = gender;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getNumberPhone() {
        return NumberPhone;
    }

    public void setNumberPhone(String numberPhone) {
        NumberPhone = numberPhone;
    }

    public LocalDate getBirthOfDate() {
        return BirthOfDate;
    }

    public void setBirthOfDate(LocalDate birthOfDate) {
        BirthOfDate = birthOfDate;
    }

    public Boolean getIsActive() {
        return IsActive;
    }

    public void setIsActive(Boolean isActive) {
        IsActive = isActive;
    }

    public String getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(String createdBy) {
        CreatedBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        CreatedDate = createdDate;
    }

}