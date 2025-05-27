package com.ojash.workoutrec.repository;
import com.ojash.workoutrec.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long>{
    User findByUsername(String username);
}
