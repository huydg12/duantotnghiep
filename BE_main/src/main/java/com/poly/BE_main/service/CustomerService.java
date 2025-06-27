package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.Customer;

public interface CustomerService {
    List<Customer> findAll();
    Customer findById(Long id);
    Customer save(Customer customer);
    void deleteById(Long id);
}
