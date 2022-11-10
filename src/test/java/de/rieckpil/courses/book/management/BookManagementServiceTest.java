package de.rieckpil.courses.book.management;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BookManagementServiceTest {

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private BookManagementService bookManagementService;

  @Test
  void shouldGetAllBooks() {
    when(bookRepository.findAll()).thenReturn(new ArrayList<>());

    List<Book> allBooks = bookManagementService.getAllBooks();

    verify(bookRepository, times(1)).findAll();
  }
}
