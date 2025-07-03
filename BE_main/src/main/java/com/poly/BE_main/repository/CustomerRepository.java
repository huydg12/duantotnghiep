package com.poly.BE_main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.BE_main.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

}