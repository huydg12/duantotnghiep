package com.poly.BE_main.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        dto.setGender(customer.isGender());
        dto.setEmail(customer.getEmail());
        dto.setNumberPhone(customer.getNumberPhone());
        dto.setBirthOfDate(customer.getBirthOfDate());
        dto.setActive(customer.isActive());
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
        customer.setGender(dto.isGender());
        customer.setEmail(dto.getEmail());
        customer.setNumberPhone(dto.getNumberPhone());
        customer.setBirthOfDate(dto.getBirthOfDate());
        customer.setActive(dto.isActive());
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
            c.setGender(dto.isGender());
            c.setEmail(dto.getEmail());
            c.setNumberPhone(dto.getNumberPhone());
            c.setBirthOfDate(dto.getBirthOfDate());
            c.setActive(dto.isActive());
            c.setCreatedDate(dto.getCreatedDate());
            return toDTO(customerRepository.save(c));
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + id));
    }

    public CustomerDTO updateStatus(Integer id, CustomerDTO dto) {
        return customerRepository.findById(id).map(c -> {
            if (c.isActive() == true) {
                c.setActive(false);
            }else{
                c.setActive(true);
            }
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

        LocalDate birthOfDate = null;
        if (data[4] != null) {
            birthOfDate = ((java.sql.Date) data[4]).toLocalDate();
        }
        return new InformationCustomerDTO(
                (String) data[0], // fullName
                (boolean) data[1], // gender
                (String) data[2], // email
                (String) data[3], // numberPhone
                birthOfDate);
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

    // Update thông tin theo customerId
    @Transactional
    public void updateCustomerInfo(Integer id, String fullName, boolean gender, String email, String numberPhone,
            java.time.LocalDate birthDate) {
        java.sql.Date sqlDate = null;
        if (birthDate != null) {
            sqlDate = java.sql.Date.valueOf(birthDate); // convert LocalDate → java.sql.Date
        }

        customerRepository.updateInformationCustomerByCustomerId(
                id,
                fullName,
                gender,
                email,
                numberPhone,
                sqlDate);
    }

}