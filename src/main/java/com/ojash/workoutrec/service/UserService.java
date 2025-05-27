package com.ojash.workoutrec.service;
import com.ojash.workoutrec.entity.User;
import com.ojash.workoutrec.dto.UserDto;
import java.util.List;
public interface UserService {
    void saveUser(UserDto userDto);

    User getUserByUsername(String name);

    List<UserDto> getUsers();
}
