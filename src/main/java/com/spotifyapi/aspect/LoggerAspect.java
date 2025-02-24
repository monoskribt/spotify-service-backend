package com.spotifyapi.aspect;

import com.spotifyapi.dto.UserInfoDTO;
import com.spotifyapi.model.Logger;
import com.spotifyapi.repository.LoggerRepository;
import com.spotifyapi.service.UserService;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@AllArgsConstructor
public class LoggerAspect {
    private final LoggerRepository loggerRepository;
    private final UserService userService;


    @AfterReturning(pointcut = "execution(* com.spotifyapi.controller.SpotifyController.*(..))")
    public void afterReturning(JoinPoint joinPoint) {
        UserInfoDTO userInfoDTO = new UserInfoDTO();

        String methodName = joinPoint.getSignature().getName();

        Logger logger = new Logger();
        logger.setUsername(userInfoDTO.getNickname());
        logger.setUserId(userInfoDTO.getUserId());
        logger.setMethodName(methodName);
        logger.setDateTime(LocalDateTime.now());
        logger.setMessage("Successfully");
        logger.setStatus("SUCCESS");

        loggerRepository.save(logger);
    }

    @AfterThrowing(pointcut = "execution(* com.spotifyapi.controller.SpotifyController.*(..))", throwing = "exception")
    public void afterThrowing(JoinPoint joinPoint, Throwable exception) {
        String methodName = joinPoint.getSignature().getName();
        String exceptionMessage = exception.toString();

        Logger logger = new Logger();
        logger.setUsername(userService.getCurrentUsername());
        logger.setUserId(userService.getCurrentId());
        logger.setMethodName(methodName);
        logger.setDateTime(LocalDateTime.now());
        logger.setMessage("Failed. Exception: " + exceptionMessage);
        logger.setStatus("FAILED");

        loggerRepository.save(logger);
    }
}
