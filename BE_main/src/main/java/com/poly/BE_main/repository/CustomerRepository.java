package com.poly.BE_main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.poly.BE_main.model.Customer;
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

}