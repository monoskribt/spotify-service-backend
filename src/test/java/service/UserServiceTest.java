package service;

import com.spotifyapi.enums.SubscribeStatus;
import com.spotifyapi.model.User;
import com.spotifyapi.repository.UserRepository;
import com.spotifyapi.service.TokenService;
import com.spotifyapi.service.impl.UserServiceImpl;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user_1;
    private User user_2;
    private User user_3;

    @SneakyThrows
    @BeforeEach
    void init() {
        user_1 = new User();
        user_1.setId("1");
        user_1.setUsername("user-1");
        user_1.setEmail("testEmailUser-1@gmail.com");
        user_1.setSubscribeStatus(SubscribeStatus.SUBSCRIBE);

        user_2 = new User();
        user_2.setId("2");
        user_2.setUsername("user-2");
        user_2.setEmail("testEmailUser-2@gmail.com");
        user_2.setAccessToken("test-token-user-2");
        user_2.setSubscribeStatus(SubscribeStatus.SUBSCRIBE);

        user_3 = new User();
        user_3.setId("3");
        user_3.setUsername("user-3");
        user_3.setEmail("testEmailUser-3@gmail.com");
        user_3.setSubscribeStatus(SubscribeStatus.UNSUBSCRIBE);
    }

    @Test
    void getAllUsersWithSubscribeStatusAndReturnsSubscribedUsersTest() {
        when(userRepository.findAll()).thenReturn(List.of(user_1, user_2, user_3));

        Set<User> subscribedUsers = userService.getAllUsersWithSubscribeStatus();

        assertEquals(2, subscribedUsers.size());
        assertTrue(subscribedUsers.contains(user_1));
        assertFalse(subscribedUsers.contains(user_3));
    }

    @Test
    void getAccessTokenFromDBTest() {
        when(tokenService.isValidAccessToken(user_2)).thenReturn(true);

        String token = userService.getAccessTokenFromDB(user_2);

        assertEquals("test-token-user-2", token);
        verify(tokenService, never()).getNewAccessToken(user_2);
        verify(userRepository, never()).save(any());
    }
}
