package de.rieckpil.courses.book.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService cut;

  @Test
  void shouldIncludeCurrentDateTimeWhenCreatingNewUser() {

    when(userRepository.findByNameAndEmail("duke", "duke@spring.io")).thenReturn(null);
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
      User user = invocation.getArgument(0);
      user.setId(1L);
      return user;
    });

    LocalDateTime defaultLocalDateTime = LocalDateTime.of(2020, 1, 1, 12, 0);

    System.out.println(LocalDateTime.now());

    try (MockedStatic<LocalDateTime> mockedLocalDateTime = Mockito.mockStatic(LocalDateTime.class)) {
      mockedLocalDateTime.when(LocalDateTime::now).thenReturn(defaultLocalDateTime);

      System.out.println(LocalDateTime.now());

      User result = cut.getOrCreateUser("duke", "duke@spring.io");

      System.out.println(LocalDateTime.now());
      System.out.println(LocalDateTime.now());

      assertEquals(defaultLocalDateTime, result.getCreatedAt());
    }

    System.out.println(LocalDateTime.now());
  }

  @Test
  void shouldFindUser() {

    User myUser = new User();
    myUser.setEmail("deyvidm18@gmail.com");
    myUser.setId(1L);
    myUser.setName("Deyvid");

    when(userRepository.findByNameAndEmail("Deyvid", "deyvidm18@gmail.com")).thenReturn(myUser);
    User result = cut.getOrCreateUser("Deyvid", "deyvidm18@gmail.com");
    assertEquals(myUser.getEmail(), result.getEmail());
    assertEquals(myUser.getName(), result.getName());
    assertEquals(myUser.getId(), result.getId());
  }
}
