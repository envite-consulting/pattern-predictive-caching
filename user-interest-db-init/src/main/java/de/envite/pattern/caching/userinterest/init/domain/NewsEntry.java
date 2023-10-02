package de.envite.pattern.caching.userinterest.init.domain;

import com.opencsv.bean.CsvBindByName;

import java.io.Serializable;
import java.util.Objects;

public class NewsEntry implements Serializable {

    @CsvBindByName(column = "link") String link;
    @CsvBindByName(column = "headline") String headline;
    @CsvBindByName(column = "category") String category;
    @CsvBindByName(column = "short_description") String shortDescription;
    @CsvBindByName(column = "authors") String authors;
    @CsvBindByName(column = "date") String date;

    public NewsEntry() {}

    public NewsEntry(final String link, final String headline, final String category, final String shortDescription, final String authors, final String date) {
        this.link = link;
        this.headline = headline;
        this.category = category;
        this.shortDescription = shortDescription;
        this.authors = authors;
        this.date = date;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NewsEntry newsEntry)) return false;
        return Objects.equals(link, newsEntry.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(link);
    }
}
