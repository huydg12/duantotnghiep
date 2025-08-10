package com.poly.BE_main.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.dto.CustomerDTO;
import com.poly.BE_main.dto.CustomerInfoDTO;

import com.poly.BE_main.dto.InformationCustomerDTO;
import com.poly.BE_main.model.Account;
import com.poly.BE_main.model.Customer;
import com.poly.BE_main.repository.CustomerRepository;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    private CustomerDTO toDTO(Customer customer) {
        if (customer == null)
            return null;
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setFullName(customer.getFullName());
        dto.setGender(customer.getGender());
        dto.setEmail(customer.getEmail());
        dto.setNumberPhone(customer.getNumberPhone());
        dto.setBirthOfDate(customer.getBirthOfDate());
        dto.setStatus(customer.getStatus());
        dto.setCreatedDate(customer.getCreatedDate());

        if (customer.getAccount() != null) {
            dto.setAccountId(customer.getAccount().getId());
        }

        return dto;
    }

    private Customer toEntity(CustomerDTO dto) {
        if (dto == null)
            return null;
        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setFullName(dto.getFullName());
        customer.setGender(dto.getGender());
        customer.setEmail(dto.getEmail());
        customer.setNumberPhone(dto.getNumberPhone());
        customer.setBirthOfDate(dto.getBirthOfDate());
        customer.setStatus(dto.getStatus());
        customer.setCreatedDate(dto.getCreatedDate());

        // Nếu cần gán account theo accountId thì xử lý ở đây
        if (dto.getAccountId() != null) {
            Account account = new Account();
            account.setId(dto.getAccountId());
            customer.setAccount(account);
        }

        return customer;
    }

    // Lấy tất cả khách hàng (dạng DTO)
    public List<CustomerDTO> findAll() {
        return customerRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // Tìm khách hàng theo ID
    public CustomerDTO findById(Integer id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + id));
        return toDTO(customer);
    }

    // Tạo mới khách hàng
    public CustomerDTO create(CustomerDTO dto) {
        Customer customer = toEntity(dto);
        customer.setCreatedDate(LocalDateTime.now());
        Customer saved = customerRepository.save(customer);
        return toDTO(saved);
    }

    // Cập nhật khách hàng
    public CustomerDTO update(Integer id, CustomerDTO dto) {
        return customerRepository.findById(id).map(c -> {
            c.setFullName(dto.getFullName());
            c.setGender(dto.getGender());
            c.setEmail(dto.getEmail());
            c.setNumberPhone(dto.getNumberPhone());
            c.setBirthOfDate(dto.getBirthOfDate());
            c.setStatus(dto.getStatus());
            c.setCreatedDate(dto.getCreatedDate());
            return toDTO(customerRepository.save(c));
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + id));
    }

    // Xoá khách hàng
    public void deleteById(Integer id) {
        customerRepository.deleteById(id);
    }

    // Truy vấn thông tin theo customerId
    public InformationCustomerDTO findInformationCustomerByCustomerId(Integer customerId) {
        Object[] data = (Object[]) customerRepository.findInformationCustomerByCustomerId(customerId);
        return new InformationCustomerDTO(
                (String) data[0], // fullName
                (String) data[1], // email
                (String) data[2], // numberPhone
                new java.util.Date(((java.util.Date) data[3]).getTime()) // createdDate
        );
    }

    // Truy vấn theo accountId
    public List<CustomerInfoDTO> findByAccountId(Integer accountId) {
        List<Object[]> results = customerRepository.findCustomerByCustomerId(accountId);
        return results.stream().map(obj -> {
            String fullName = (String) obj[0];
            String phoneNumber = (String) obj[1];
            return new CustomerInfoDTO(fullName, phoneNumber);
        }).collect(Collectors.toList());
    }

}