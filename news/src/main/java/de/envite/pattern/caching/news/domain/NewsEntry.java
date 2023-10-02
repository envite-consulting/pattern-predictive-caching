package de.envite.pattern.caching.news.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.NaturalId;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "news")
public class NewsEntry implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @NaturalId(mutable = true)
    @Column(name = "link", nullable = false, unique = true)
    private String link;
    @Column(name = "headline")
    private String headline;
    @Column(name = "category")
    private String category;
    @Column(name = "short_description")
    private String shortDescription;
    @Column(name = "authors")
    private String authors;
    @Column(name = "date")
    private LocalDate date;

    public NewsEntry() {
    }

    public NewsEntry(final Integer id,
                     final String link,
                     final String headline, final String category, final String shortDescription, final String authors, final LocalDate date) {
        this.id = id;
        this.link = requireNonNull(link);
        this.headline = headline;
        this.category = category;
        this.shortDescription = shortDescription;
        this.authors = authors;
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof NewsEntry newsEntry)) return false;
        return Objects.equals(link, newsEntry.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }
}
