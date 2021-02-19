package ecx.akudelic.library.model;

import java.util.Date;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Book{
    @JacksonXmlProperty(localName = "id", isAttribute = true)
    private String id;

    @JacksonXmlProperty(localName = "author")
    private String author;

    @JacksonXmlProperty(localName = "title")
    private String title;

    @JacksonXmlProperty(localName = "genre")
    private String genre;

    @JacksonXmlProperty(localName = "price")
    private Float price;

    @JacksonXmlProperty(localName = "publish_date")
    private String publishDate;

    @JacksonXmlProperty(localName = "description")
    private String description;

    public void setId(String id) {
        this.id = id;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setPrice(Float price) {
        this.price = price;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }
    
    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getGenre() {
        return genre;
    }

    public Float getPrice() {
        return price;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public String getDescription() {
        return description;
    }
}