package ecx.akudelic.library.model;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class BookCatalog {
    @JacksonXmlProperty(localName = "book")
    private List<Book> booksList;

    public List<Book> getAllBooks() {
        return booksList;
    }

    public void setAllBooks(List<Book> booksList) {
        this.booksList = booksList;
    }
}
