package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Role;
import com.poly.BE_main.repository.RoleRepository;

@Service
public class RoleService {
    @Autowired
    RoleRepository roleRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Role create(Role r) {
        return roleRepository.save(r);
    }

    public void delete(Integer id) {
        roleRepository.deleteById(id);
    }

    public Role update(int id, Role rUpdate) {
        return roleRepository.findById(id).map(r -> {
            r.setRoleName(rUpdate.getRoleName());
            r.setDescription(rUpdate.getDescription());
            return roleRepository.save(r);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy Role có id: " + id));
    }
}
