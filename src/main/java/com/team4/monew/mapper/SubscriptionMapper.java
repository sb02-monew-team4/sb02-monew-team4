package com.team4.monew.mapper;

import com.team4.monew.dto.interest.SubscriptionDto;
import com.team4.monew.entity.InterestKeyword;
import com.team4.monew.entity.Subscription;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

  @Mapping(source = "interest.id", target = "interestId")
  @Mapping(source = "interest.name", target = "interestName")
  @Mapping(source = "interest.keywords", target = "interestKeywords")
  @Mapping(source = "interest.subscriberCount", target = "interestSubscriberCount")
  SubscriptionDto toDto(Subscription subscription);

  // MapStruct가 List<InterestKeyword> → List<String> 변환할 때 이 메서드 사용
  default List<String> mapKeywords(List<InterestKeyword> keywords) {
    if (keywords == null) {
      return Collections.emptyList();
    }
    return keywords.stream()
        .map(InterestKeyword::getKeyword)
        .collect(Collectors.toList());
  }
}

