package de.rieckpil.courses.book.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.rieckpil.courses.book.management.Book;
import de.rieckpil.courses.book.management.BookRepository;
import de.rieckpil.courses.book.management.User;
import de.rieckpil.courses.book.management.UserService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @Mock
  private ReviewVerifier mockedReviewVerifier;

  @Mock
  private UserService userService;

  @Mock
  private BookRepository bookRepository;

  @Mock
  private ReviewRepository reviewRepository;

  @InjectMocks
  private ReviewService cut;

  private static final String EMAIL = "duke@spring.io";
  private static final String USERNAME = "duke";
  private static final String ISBN = "42";

  private ObjectMapper objectMapper;

  @BeforeEach
  void beforeEach() {
    this.objectMapper = new ObjectMapper();
  }

  @Test
  void shouldNotBeNull() {
    assertNotNull(reviewRepository);
    assertNotNull(mockedReviewVerifier);
    assertNotNull(userService);
    assertNotNull(bookRepository);
    assertNotNull(cut);
  }

  @Test
  @DisplayName("Write english sentence")
  void shouldThrowExceptionWhenReviewedBookIsNotExisting() {
    when(bookRepository.findByIsbn(ISBN)).thenReturn(null);

    assertThrows(IllegalArgumentException.class,
      () -> cut.createBookReview(ISBN, null, USERNAME, EMAIL));
  }

  @Test
  void shouldRejectReviewWhenReviewQualityIsBad() {
    // arrange - given
    BookReviewRequest bookReviewRequest =
      new BookReviewRequest("Title", "BADCONTENT!", 1);
    when(bookRepository.findByIsbn(ISBN)).thenReturn(new Book());
    when(mockedReviewVerifier.doesMeetQualityStandards(
      bookReviewRequest.getReviewContent())).thenReturn(false);

    // act - when
    assertThrows(BadReviewQualityException.class,
      () -> cut.createBookReview(ISBN, bookReviewRequest, USERNAME, EMAIL));

    // assert - then
    verify(reviewRepository, times(0)).save(ArgumentMatchers.any(Review.class));
  }

  @Test
  void shouldStoreReviewWhenReviewQualityIsGoodAndBookIsPresent() {

    BookReviewRequest bookReviewRequest =
      new BookReviewRequest("Title", "GOOD CONTENT!", 1);

    when(bookRepository.findByIsbn(ISBN)).thenReturn(new Book());
    when(mockedReviewVerifier.doesMeetQualityStandards(
      bookReviewRequest.getReviewContent())).thenReturn(true);
    when(userService.getOrCreateUser(USERNAME, EMAIL)).thenReturn(new User());
    when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
      Review reviewToSave = invocation.getArgument(0);
      reviewToSave.setId(42L);
      return reviewToSave;
    });

    Long result = cut.createBookReview(ISBN, bookReviewRequest, USERNAME, EMAIL);

    Long expected = 42L;
    assertEquals(expected, result);
  }

  @Test
  void shouldGetAllReviewsByRating() {
    List<Review> reviewList = mockReviews();
    when(reviewRepository.findTop5ByOrderByRatingDescCreatedAtDesc()).thenReturn(reviewList);
    ArrayNode allReviews = cut.getAllReviews(reviewList.size(), "rating");
    assertNotNull(allReviews);
    assertEquals(2, allReviews.size());
  }

  @Test
  void shouldGetAllReviewsByCreatedAt() {
    List<Review> reviewList = mockReviews();
    when(reviewRepository.findAllByOrderByCreatedAtDesc(any(PageRequest.class))).thenReturn(
      reviewList);
    ArrayNode allReviews = cut.getAllReviews(reviewList.size(), "created");
    assertNotNull(allReviews);
    assertEquals(2, allReviews.size());
  }

  @Test
  void shouldDeleteAReview() {
    doNothing().when(reviewRepository).deleteByIdAndBookIsbn(anyLong(), anyString());
    cut.deleteReview("1", 1L);
    verify(reviewRepository, times(1)).deleteByIdAndBookIsbn(anyLong(), anyString());
  }

  @Test
  void shouldGetAReviewById() {
    List<Review> reviewList = mockReviews();
    when(reviewRepository.findByIdAndBookIsbn(anyLong(), anyString())).thenReturn(
      Optional.of(reviewList.get(0)));
    ObjectNode review = cut.getReviewById("1", 1L);
    assertNotNull(review);
    assertEquals("Content 0", review.get("reviewContent").asText());
  }

  @Test
  void shouldGetReviewStatistics() {
    ReviewStatistic reviewStatistic = getReviewStatistic();
    List<ReviewStatistic> reviewStatisticList = new ArrayList<>();
    reviewStatisticList.add(reviewStatistic);
    when(reviewRepository.getReviewStatistics()).thenReturn(reviewStatisticList);
    ArrayNode reviewStatistics = cut.getReviewStatistics();
    assertEquals(1, reviewStatistics.size());
    assertEquals(reviewStatistic.getId(), reviewStatistics.get(0).get("bookId").asLong());
  }

  @NotNull
  private ReviewStatistic getReviewStatistic() {
    return new ReviewStatistic() {
      @Override
      public Long getId() {
        return 1L;
      }

      @Override
      public Long getRatings() {
        return 4L;
      }

      @Override
      public String getIsbn() {
        return UUID.randomUUID().toString();
      }

      @Override
      public BigDecimal getAvg() {
        return new BigDecimal("5.0");
      }
    };
  }

  private List<Review> mockReviews() {
    List<Review> reviewList = new ArrayList<>();
    for (int i = 0; i < 2; i++) {
      Review review = new Review();
      review.setId((long) i);
      review.setContent("Content " + i);
      review.setRating(i);
      review.setTitle("Title " + i);
      review.setBook(new Book());
      review.setUser(new User());
      review.setCreatedAt(LocalDateTime.now());
      reviewList.add(review);
    }
    return reviewList;
  }
}
