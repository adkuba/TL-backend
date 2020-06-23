package com.tl.backend.mappers;

import com.tl.backend.models.ERole;
import com.tl.backend.models.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserRolesMapper {

    @Autowired
    public UserRolesMapper(){}

    public List<String> mapToString(Set<Role> roles){
        if (roles != null){
            return roles.stream().map(Role::getName).map(ERole::toString).collect(Collectors.toList());
        }
        return null;
    }
}
