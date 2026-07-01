package com.poly.BE_main.repository;

import com.poly.BE_main.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Integer> {
    List<Address> findByCustomerId(Integer customerId);
}