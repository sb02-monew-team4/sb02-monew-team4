package com.team4.monew.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, unique = true, length = 254)
  private String email;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(nullable = false, length = 100)
  private String password;

  @Column(nullable = false)
  private boolean isDeleted = false;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "user_interests",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "interest_id")
  )
  private List<Interest> interests = new ArrayList<>();

  public static User create(String email, String nickname, String password, boolean isDeleted) {
    return User.builder()
        .email(email)
        .nickname(nickname)
        .password(password)
        .isDeleted(isDeleted)
        .build();
  }

  public void update(String nickname) {
    if (nickname != null && !nickname.equals(this.nickname)) {
      this.nickname = nickname;
    }
  }


}
