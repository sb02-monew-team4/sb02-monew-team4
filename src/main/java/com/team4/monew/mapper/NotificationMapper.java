package com.team4.monew.mapper;

import com.team4.monew.dto.notifications.NotificationDto;
import com.team4.monew.entity.Notification;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
  @Mapping(source = "user.id", target = "userId")
  NotificationDto toDto(Notification notification);

  List<NotificationDto> toDtoList(List<Notification> notifications);
}
