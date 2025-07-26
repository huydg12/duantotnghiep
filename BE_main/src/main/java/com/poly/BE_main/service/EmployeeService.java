package com.poly.BE_main.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.dto.EmployeeDTO;
import com.poly.BE_main.model.Account;
import com.poly.BE_main.model.Employee;
import com.poly.BE_main.repository.EmployeeRepository;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    private EmployeeDTO toDTO(Employee e) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(e.getId());
        dto.setFullName(e.getFullName());
        dto.setGender(e.getGender());
        dto.setEmail(e.getEmail());
        dto.setNumberPhone(e.getNumberPhone());
        dto.setBirthOfDate(e.getBirthOfDate());
        dto.setIsActive(e.getIsActive());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setCreatedDate(e.getCreatedDate());
        if (e.getAccount() != null) {
            dto.setAccountId(e.getAccount().getId());
            dto.setAccountUsername(e.getAccount().getUsername()); // nếu cần
        }
        System.out.println(dto);
        return dto;
    }

    private Employee toEntity(EmployeeDTO dto) {
        Employee e = new Employee();
        e.setId(dto.getId());
        e.setFullName(dto.getFullName());
        e.setGender(dto.getGender());
        e.setEmail(dto.getEmail());
        e.setNumberPhone(dto.getNumberPhone());
        e.setBirthOfDate(dto.getBirthOfDate());
        e.setIsActive(dto.getIsActive());
        e.setCreatedBy(dto.getCreatedBy());
        e.setCreatedDate(dto.getCreatedDate());
        if (dto.getAccountId() != null) {
            Account acc = new Account();
            acc.setId(dto.getAccountId());
            e.setAccount(acc);
        }
        return e;
    }

    public List<EmployeeDTO> findAll() {
        return employeeRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public EmployeeDTO create(EmployeeDTO dto) {
        Employee e = toEntity(dto);
        employeeRepository.save(e);
        return toDTO(e);
    }

    public void deleteById(Integer id) {
        employeeRepository.deleteById(id);
    }

    public EmployeeDTO update(Integer id, EmployeeDTO dto) {
        return employeeRepository.findById(id).map(e -> {
            e.setFullName(dto.getFullName());
            e.setGender(dto.getGender());
            e.setEmail(dto.getEmail());
            e.setNumberPhone(dto.getNumberPhone());
            e.setBirthOfDate(dto.getBirthOfDate());
            e.setIsActive(dto.getIsActive());
            e.setCreatedBy(dto.getCreatedBy());
            e.setCreatedDate(dto.getCreatedDate());

            if (dto.getAccountId() != null) {
                Account acc = new Account();
                acc.setId(dto.getAccountId());
                e.setAccount(acc);
            }

            return toDTO(employeeRepository.save(e));
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên có id: " + id));
    }
}
