package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Address;
import com.poly.BE_main.repository.AddressRepository;

@Service
public class AddressService {
    @Autowired
    AddressRepository addressRepository;

    public List<Address> findAll(){
        return addressRepository.findAll();
    }

    public Address create(Address a){
        addressRepository.save(a);
        return a;
    }
    
    public void deleteById(Integer id){
        addressRepository.deleteById(id);
    }

    public Address update(Address a){
        return addressRepository.save(a);
    }

    public Address update(int id, Address aUpdate){
        return addressRepository.findById(id).map(a ->{
            a.setFullAddress(aUpdate.getFullName());
            a.setAddress(aUpdate.getAddress());
            a.setCreatedBy(aUpdate.getCreatedBy());
            a.setCreatedDate(aUpdate.getCreatedDate());
            a.setModifiedBy(aUpdate.getModifiedBy());
            a.setModifiedDate(aUpdate.getModifiedDate());
            a.setNumberPhone(aUpdate.getNumberPhone());
            a.setFullName(aUpdate.getFullName());
            a.setCustomerId(aUpdate.getCustomerId());
            a.setDefault(aUpdate.isDefault());
            a.setAddressType(aUpdate.getAddressType());
            a.setStreetName(aUpdate.getStreetName());
            a.setWardName(aUpdate.getWardName());
            a.setCityName(aUpdate.getCityName());
            return addressRepository.save(a);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ: " + id));
    }
}
