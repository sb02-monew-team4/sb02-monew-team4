package com.team4.monew.mapper;

import com.team4.monew.dto.interest.InterestDto;
import com.team4.monew.dto.interest.InterestRegisterRequest;
import com.team4.monew.entity.Interest;
import com.team4.monew.entity.InterestKeyword;
import java.util.List;
import org.mapstruct.*;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface InterestMapper {

  @Mapping(target = "subscribedByMe", expression = "java(isSubscribed(interest, requesterId))")
  @Mapping(target = "keywords", expression = "java(interestKeywordsToStrings(interest.getKeywords()))")
  InterestDto toDto(Interest interest, @Context UUID requesterId);

  @Mapping(target = "keywords", expression = "java(stringsToInterestKeywords(request.keywords()))")
  Interest toEntity(InterestRegisterRequest request);

  default List<InterestKeyword> stringsToInterestKeywords(List<String> keywordStrings) {
    if (keywordStrings == null) return null;
    return keywordStrings.stream()
        .map(k -> new InterestKeyword(null, null, k))
        .toList();
  }

  default List<String> interestKeywordsToStrings(List<InterestKeyword> keywordEntities) {
    if (keywordEntities == null) return null;
    return keywordEntities.stream()
        .map(InterestKeyword::getKeyword)
        .toList();
  }

  default boolean isSubscribed(Interest interest, UUID requesterId) {
    if (requesterId == null) return false;
    return interest.getSubscriptions().stream()
        .anyMatch(sub -> sub.getUser().getId().equals(requesterId));
  }
}
