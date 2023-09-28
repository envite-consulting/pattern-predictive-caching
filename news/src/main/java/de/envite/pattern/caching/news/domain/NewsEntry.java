package de.envite.pattern.caching.news.domain;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "news")
public class NewsEntry implements Serializable {
    @Id
    private int id;
    private String link;
    private String headline;
    private String category;
    private String shortDescription;
    private String authors;
    private LocalDate date;

    public NewsEntry() {
    }

    public NewsEntry(int id, String link, String headline, String category, String shortDescription, String authors, LocalDate date) {
        this.id = id;
        this.link = link;
        this.headline = headline;
        this.category = category;
        this.shortDescription = shortDescription;
        this.authors = authors;
        this.date = date;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getId() {
        return id;
    }

    @Column(name = "link")
    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Column(name = "headline")
    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    @Column(name = "category")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Column(name = "short_description")
    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    @Column(name = "authors")
    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    @Column(name = "date")
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
