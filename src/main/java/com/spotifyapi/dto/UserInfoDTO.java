package com.spotifyapi.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class UserInfoDTO {

    private String nickname;
    private String userId;
    private String status;

    public UserInfoDTO(String nickname, String status) {
        this.nickname = nickname;
        this.status = status;
    }
}
