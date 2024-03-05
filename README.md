Użyte programy:
- IntelliJ IDEA
- XAMPP

Wgrana do projektu biblioteka JDBC:
https://dev.mysql.com/downloads/connector/j/

Wszystkie dostępne opcje działają, zmiany są wykonywane w bazie danych "biblioteka" na tabeli "ksiazki"

CREATE DATABASE IF NOT EXISTS biblioteka;

USE biblioteka;

CREATE TABLE IF NOT EXISTS ksiazki (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tytul VARCHAR(255),
    autor VARCHAR(255),
    isbn VARCHAR(13),
    wypozyczona BOOLEAN DEFAULT FALSE,
    wypozyczajacy VARCHAR(255),
    data_wypozyczenia DATE,
    data_zwrotu DATE
);
