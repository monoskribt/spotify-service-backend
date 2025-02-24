package com.spotifyapi.controller;

import com.spotifyapi.dto.UserInfoDTO;
import com.spotifyapi.enums.SubscribeStatus;
import com.spotifyapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    public UserInfoDTO getInfoAboutUser() {
        return new UserInfoDTO(
                userService.getCurrentUsername(),
                userService.getSubscribeStatusUsers());
    }


    @PutMapping("/subscribe")
    public ResponseEntity<String> manageSubscribeStatus(@RequestParam SubscribeStatus subscribeStatus) {
        userService.manageSubscribeStatusOfUser(subscribeStatus);
        return ResponseEntity.ok("Subscribe status updated successfully");
    }
}
