package com.poly.BE_main.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Customer;
import com.poly.BE_main.repository.CustomerRepository;

@Service
public class CustomerService {
    @Autowired
    CustomerRepository customerRepository;

    public List<Customer> findAll(){
        return customerRepository.findAll();
    }

    public Customer create(Customer c){
        customerRepository.save(c);
        return c;
    }

    public void deleteById(Integer id){
        customerRepository.deleteById(id);
    }

    public Customer update(Customer c){
        return customerRepository.save(c);
    }

    public Customer update(int id, Customer cUpdate){
        return customerRepository.findById(id).map(c ->{
            c.setAccountId(cUpdate.getAccountId());
            c.setFullName(cUpdate.getFullName());
            c.setGender(cUpdate.getGender());
            c.setEmail(cUpdate.getEmail());
            c.setNumberPhone(cUpdate.getNumberPhone());
            c.setBirthOfDate(cUpdate.getBirthOfDate());
            c.setStatus(cUpdate.getStatus());
            c.setCreatedDate(cUpdate.getCreatedDate());
            return customerRepository.save(c);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng có id: " + id));
    }


}
