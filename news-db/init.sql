CREATE DATABASE IF NOT EXISTS newsdb;

USE newsdb;

CREATE TABLE IF NOT EXISTS news (
    id MEDIUMINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    link VARCHAR(1024),
    headline VARCHAR(1024),
    category VARCHAR(255),
    short_description VARCHAR(2048),
    authors VARCHAR(255),
    date DATE
);

LOAD DATA INFILE '/etc/News_Category_Dataset_v3.csv' INTO TABLE news
    FIELDS TERMINATED BY ','
    ENCLOSED BY '"'
    LINES TERMINATED BY '\n'
    IGNORE 1 LINES
    (link, headline, category, short_description, authors, @var1)
    SET
        id = NULL,
        date = DATE_FORMAT(STR_TO_DATE(@var1, '%Y-%m-%d'), '%y-%m-%d');