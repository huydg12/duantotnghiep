package com.poly.BE_main.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.poly.BE_main.model.Account;


public interface AccountRepository extends JpaRepository<Account, Integer> {
    Optional<Account> findByUsername(String username);
    Optional<Account> findById(int id);
    
    @Modifying
    @Transactional  // phải có nếu dùng @Modifying
    @Query(value = """
        UPDATE ACCOUNT
        SET PASSWORD = :password
        WHERE ID = :id
    """, nativeQuery = true)
    int updatePasswordById(
        @Param("id") Integer id,
        @Param("password") String password
    );
    
}
