package com.poly.BE_main.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
@Entity
@Table(name = "CUSTOMER", schema = "dbo")
public class Customer {

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

    @Column(name = "STATUS")
    private Integer Status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime CreatedDate;

    public Customer(){
    }

    public Customer(Integer id, Integer accountId, String fullName, String gender,
                String email, String numberPhone, LocalDate birthOfDate,
                Integer status, LocalDateTime createdDate) {
        ID = id;
        AccountId = accountId;
        FullName = fullName;
        Gender = gender;
        Email = email;
        NumberPhone = numberPhone;
        BirthOfDate = birthOfDate;
        Status = status;
        CreatedDate = createdDate;
    }

        public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNumberPhone() {
        return numberPhone;
    }

    public void setNumberPhone(String numberPhone) {
        this.numberPhone = numberPhone;
    }

    public LocalDate getBirthOfDate() {
        return birthOfDate;
    }

    public void setBirthOfDate(LocalDate birthOfDate) {
        this.birthOfDate = birthOfDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
