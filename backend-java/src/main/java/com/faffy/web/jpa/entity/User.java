package com.faffy.web.jpa.entity;

import com.faffy.web.dto.*;
import com.faffy.web.exception.IllegalInputException;
import com.faffy.web.jpa.type.Gender;
import com.faffy.web.jpa.type.LoginType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static javax.persistence.FetchType.LAZY;

/*
작성자 : 이준성
기능 : #S07P12A802-147-[BE] JPA Entity 구현
설명 : user table JPA entity
*/

@Entity
@Getter
@ToString
public class User extends BaseEntity implements UserDetails {
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String nickname;
    @Column
//    @Column(nullable = false)
    private String password;

    private LocalDate birthday;
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
//    @Column(nullable = false)
    private Gender gender;

    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    // 한 줄 자기소개
    @Column(length = 200)
    private String introduce;
    /**
     * 자기소개 문구
     */
    @Lob
    @Column(length = 2000)
    private String info;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "file_no")
    private UploadFile profileImage;

    @Enumerated(EnumType.STRING)
    @NonNull
    private LoginType loginType;

    private String instaLink;
    private String facebookLink;
    private String youtubeLink;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY, mappedBy = "user")
    @JsonIgnore
    List<Board> writeBoards = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY, mappedBy = "userCategoryMapper.user")
    @JsonIgnore
    List<UserCategory> categories = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY, mappedBy = "followMapper.followUser")
    @JsonIgnore
    List<FollowMapping> followMappings = new ArrayList<>();
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = LAZY, mappedBy = "followMapper.followedUser")
    @JsonIgnore
    List<FollowMapping> followedMappings = new ArrayList<>();

    public User() {
    }

    @Builder
    public User(int no, String email, LoginType loginType, String name, String nickname, String password, LocalDate birthday, String introduce, Gender gender, String info, List<String> roles
            , String instaLink, String facebookLink, String youtubeLink, UploadFile profileImage) {
        this.no = no;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.birthday = birthday;
        this.gender = gender;
        this.info = info;
        this.roles = roles;
        this.loginType = loginType;
        this.introduce = introduce;
        this.instaLink = instaLink;
        this.facebookLink = facebookLink;
        this.youtubeLink = youtubeLink;
        this.profileImage = profileImage;
    }

    /**
     * 이름, 닉네임, 패스워드, 자기소개, 생년월일 수정
     *
     * @param userDto
     * @return 수정된 User
     * @throws IllegalInputException null 혹은 빈 값이면 안되는 입력이 들어올 경우
     */
    public void updateUser(UserUpdateDto userDto) throws IllegalInputException {
        this.nickname = userDto.getNickname();
        this.name = userDto.getName();
        this.password = userDto.getPassword();
        if(this.info != null) this.info = userDto.getInfo();
        if(this.gender != null) this.gender = Gender.values()[Integer.parseInt(userDto.getGender())];
        if(!userDto.getBirthday().equals("null")) this.birthday = LocalDate.parse(userDto.getBirthday());
        if(this.introduce != null) this.introduce = userDto.getIntroduce();
        if(this.instaLink != null) this.instaLink = userDto.getInstaLink();
        if(this.facebookLink != null) this.facebookLink = userDto.getFacebookLink();
        if(this.youtubeLink != null) this.youtubeLink = userDto.getYoutubeLink();
    }

    /**
     * 프로필 사진 수정
     *
     * @param img
     */
    public void updateProfileImage(UploadFile img) {
        this.profileImage = img;
    }

    @JsonIgnore
    public UserPublicDto toPublicDto() {
        UserPublicDto userPublicDto = UserPublicDto.builder()
                .no(this.no)
                .email(this.email)
                .nickname(this.nickname)
                .name(this.name)
                .gender(this.gender)
                .birthday(this.birthday)
                .info(this.info)
                .roles(this.roles)
                .introduce(this.introduce)
                .facebookLink(this.facebookLink)
                .instaLink(this.instaLink)
                .youtubeLink(this.youtubeLink)
                .categories(this.categories)
                .loginType(this.loginType)
                .build();
        return userPublicDto;
    }

    public UserGetDetailDto toDetailDto() {
        UserGetDetailDto userDetailDto = UserGetDetailDto.builder()
                .no(this.no)
                .email(this.email)
                .nickname(this.nickname)
                .name(this.name)
                .gender(this.gender)
                .birthday(this.birthday)
                .info(this.info)
                .roles(this.roles)
                .categories(this.categories)
                .followerCount(this.followedMappings.size())
                .followingCount(this.followMappings.size())
                .introduce(this.introduce)
                .facebookLink(this.facebookLink)
                .instaLink(this.instaLink)
                .youtubeLink(this.youtubeLink)
                .build();
        return userDetailDto;
    }

    public UserGetSimpleDto toSimpleDto() {
        UserGetSimpleDto simpleDto = UserGetSimpleDto.builder()
                .no(this.no)
                .nickname(this.nickname)
                .build();
        return simpleDto;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
}
