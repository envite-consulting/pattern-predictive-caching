package de.envite.pattern.caching.news.domain;

import io.micrometer.core.annotation.Timed;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface NewsRepository extends JpaRepository<NewsEntry, Long> {

    @Timed
    List<NewsEntry> findByCategoryInAndDateGreaterThanEqualAndDateLessThanEqual(Set<String> categories, LocalDate start, LocalDate end);

    @Timed
    List<NewsEntry> findByDateLessThanEqual(LocalDate end, Pageable top);
}
