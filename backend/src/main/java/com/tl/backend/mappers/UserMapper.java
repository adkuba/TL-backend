package com.tl.backend.mappers;

import com.tl.backend.models.User;
import com.tl.backend.response.UserResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @BeanMapping(resultType = UserResponse.class)
    UserResponse userResponse(User user);
}
