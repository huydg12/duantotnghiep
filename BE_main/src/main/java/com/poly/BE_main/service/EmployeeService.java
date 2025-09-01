package com.poly.BE_main.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.poly.BE_main.dto.EmployeeDTO;
import com.poly.BE_main.dto.InformationEmployeeDTO;
import com.poly.BE_main.model.Account;
import com.poly.BE_main.model.Employee;
import com.poly.BE_main.repository.EmployeeRepository;
import jakarta.transaction.Transactional;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    private EmployeeDTO toDTO(Employee e) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setId(e.getId());
        dto.setFullName(e.getFullName());
        dto.setGender(e.isGender());
        dto.setEmail(e.getEmail());
        dto.setNumberPhone(e.getNumberPhone());
        dto.setBirthOfDate(e.getBirthOfDate());
        dto.setActive(e.isActive());
        dto.setCreatedBy(e.getCreatedBy());
        dto.setCreatedDate(e.getCreatedDate());
        if (e.getAccount() != null) {
            dto.setAccountId(e.getAccount().getId());
            dto.setAccountUsername(e.getAccount().getUsername());
        }
        System.out.println(dto);
        return dto;
    }

    private Employee toEntity(EmployeeDTO dto) {
        Employee e = new Employee();
        e.setId(dto.getId());
        e.setFullName(dto.getFullName());
        e.setGender(dto.isGender());
        e.setEmail(dto.getEmail());
        e.setNumberPhone(dto.getNumberPhone());
        e.setBirthOfDate(dto.getBirthOfDate());
        e.setActive(dto.isActive());
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

    public Optional<EmployeeDTO> findById(Integer id) {
        return employeeRepository.findById(id).map(this::toDTO);
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
            e.setGender(dto.isGender());
            e.setEmail(dto.getEmail());
            e.setNumberPhone(dto.getNumberPhone());
            e.setBirthOfDate(dto.getBirthOfDate());
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

    public EmployeeDTO updateStatus(Integer id, EmployeeDTO dto) {
        return employeeRepository.findById(id).map(e -> {
            if (e.isActive() == true) {
                e.setActive(false);
            } else {
                e.setActive(true);
            }

            return toDTO(employeeRepository.save(e));
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên có id: " + id));
    }

    public InformationEmployeeDTO findInformationEmployeeByEmployeeId(Integer employeeId) {
        Employee e = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with id=" + employeeId));

        return new InformationEmployeeDTO(
                e.getFullName(),
                e.isGender(), // nếu entity là Boolean -> Boolean.TRUE.equals(e.getGender())
                e.getEmail(),
                e.getNumberPhone(),
                e.getBirthOfDate() // nếu entity là LocalDate, trả thẳng; nếu java.sql.Date thì .toLocalDate()
        );
    }

    // Cập nhật theo EMPLOYEE ID
    @Transactional
    public InformationEmployeeDTO updateInformationEmployeeByEmployeeId(Integer employeeId,
            InformationEmployeeDTO dto) {
        Employee e = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Employee not found with id=" + employeeId));

        e.setFullName(dto.getFullName());
        e.setGender(dto.isGender());
        e.setEmail(dto.getEmail());
        e.setNumberPhone(dto.getNumberPhone());
        e.setBirthOfDate(dto.getBirthOfDate());

        employeeRepository.save(e);

        return new InformationEmployeeDTO(
                e.getFullName(),
                e.isGender(),
                e.getEmail(),
                e.getNumberPhone(),
                e.getBirthOfDate());
    }
}
