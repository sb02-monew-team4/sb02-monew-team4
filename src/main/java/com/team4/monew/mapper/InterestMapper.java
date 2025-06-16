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

  @Mapping(target = "name", source = "name")
  @Mapping(target = "subscribedByMe", expression = "java(isSubscribed(interest, requesterId))")
  @Mapping(target = "keywords", expression = "java(interestKeywordsToStrings(interest.getKeywords()))")
  InterestDto toDto(Interest interest, @Context UUID requesterId);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "subscriberCount", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  @Mapping(target = "subscriptions", ignore = true)
  @Mapping(target = "article", ignore = true)
  @Mapping(target = "keywords", ignore = true)
  Interest toEntity(InterestRegisterRequest request);

  @AfterMapping
  default void connectKeywordsToInterest(
      @MappingTarget Interest interest,
      InterestRegisterRequest request
  ) {
    if (request.keywords() != null && !request.keywords().isEmpty()) {
      for (String keyword : request.keywords()) {
        interest.addKeyword(new InterestKeyword(interest, keyword));
      }
    }
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
