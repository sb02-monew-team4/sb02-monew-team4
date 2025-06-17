package com.team4.monew.mapper;

import com.team4.monew.dto.user.UserDto;
import com.team4.monew.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
  UserDto toDto(User user);
}
