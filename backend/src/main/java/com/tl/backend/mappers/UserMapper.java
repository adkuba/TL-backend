package com.tl.backend.mappers;

import com.tl.backend.models.User;
import com.tl.backend.response.UserResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", uses = UserRolesMapper.class, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @BeanMapping(resultType = UserResponse.class)
    UserResponse userResponse(User user);

    @IterableMapping(elementTargetType = UserResponse.class)
    List<UserResponse> usersResponse(List<User> users);
}
