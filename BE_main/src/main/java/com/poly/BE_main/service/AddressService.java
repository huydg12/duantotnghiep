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

    public List<Address> findAll() {
        return addressRepository.findAll();
    }

    public List<Address> findByCustomerId(Integer customerId) {
        return addressRepository.findByCustomerId(customerId);
    }

    public Address create(Address a) {
        addressRepository.save(a);
        return a;
    }

    public void deleteById(Integer id) {
        addressRepository.deleteById(id);
    }

    public Address update(Address a) {
        return addressRepository.save(a);
    }

    public Address update(int id, Address aUpdate) {
        return addressRepository.findById(id).map(a -> {
            a.setFullAddress(aUpdate.getFullAddress());
            a.setNumberPhone(aUpdate.getNumberPhone());
            a.setFullName(aUpdate.getFullName());
            a.setCustomerId(aUpdate.getCustomerId());
            a.setDefault(aUpdate.isDefault());
            a.setDistrictName(aUpdate.getDistrictName());
            a.setDetailAddress(aUpdate.getDetailAddress());
            a.setWardName(aUpdate.getWardName());
            a.setCityName(aUpdate.getCityName());
            return addressRepository.save(a);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ: " + id));
    }

    public void setDefaultAddress(Integer addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

        Integer customerId = address.getCustomerId();

        // Bỏ default của tất cả địa chỉ cũ
        List<Address> allAddresses = addressRepository.findByCustomerId(customerId);
        for (Address a : allAddresses) {
            if (Boolean.TRUE.equals(a.isDefault())) {
                a.setDefault(false);
            }
        }

        // Gán địa chỉ mới là mặc định
        address.setDefault(true);
        allAddresses.add(address); // Đảm bảo address mới được cập nhật trong danh sách

        addressRepository.saveAll(allAddresses);
    }
}
