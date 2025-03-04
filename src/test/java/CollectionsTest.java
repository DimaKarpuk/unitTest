import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class CollectionsTest {
    @Test
    public void collectionsTest(){
        List<String> books = new ArrayList<>(List.of("Book1, Author1, 1999, 4.2", "Book2, Author2, 2001, 4.6",
               "Book3, Author1, 2010, 4.8", "Book4, Author3, 2005, 3.9"));

        List<Author> authors = books.stream().map(x->{
            String[] arr = x.split(", ");
            return new Author(arr[0], arr[1], Integer.parseInt(arr[2]),
                    Double.parseDouble(arr[3]));
        }).toList();

        authors.stream().filter(x -> x.getRating() > 4.5).map(Author::getName).forEach(System.out::println);
    }
}