package com.ojash.workoutrec.service.impl;
import com.ojash.workoutrec.entity.User;
import com.ojash.workoutrec.dto.UserDto;
import com.ojash.workoutrec.repository.UserRepo;
import com.ojash.workoutrec.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private UserRepo userRepo;
    private PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void saveUser(UserDto userDto) {
        User u = new User();
        u.setUsername(userDto.getUsername());
        u.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userRepo.save(u);
    }
    @Override
    public User getUserByUsername(String name) {
        return userRepo.findByUsername(name);
    }
    private UserDto mapToUserDto(User u) {
        UserDto dto = new UserDto();
        dto.setUsername(u.getUsername());
        dto.setId(u.getId());
        return dto;
    }
    public List<UserDto> getUsers() {
        List<User> users = userRepo.findAll();
        return users.stream()
                .map((user) -> mapToUserDto(user))
                .collect(Collectors.toList());
    }
}
