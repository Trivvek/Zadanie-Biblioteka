import java.sql.*;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Biblioteka {

    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/biblioteka";
    static final String USER = "root123";
    static final String PASS = "root123";

    public static void main(String[] args) {
        Connection conn = null;
        Statement stmt = null;

        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            stmt = conn.createStatement();
            createTable(stmt);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("1. Dodaj książkę");
                System.out.println("2. Wyszukaj książki");
                System.out.println("3. Wypożycz książkę");
                System.out.println("4. Wylistuj wypożyczone książki");
                System.out.println("5. Wylistuj przeterminowane książki");
                System.out.println("6. Wylistuj wszystkie książki");
                System.out.println("0. Wyjście");

                try {
                    int choice = scanner.nextInt();
                    scanner.nextLine();

                    switch (choice) {
                        case 1:
                            dodajKsiazke(conn, scanner);
                            break;
                        case 2:
                            wyszukajKsiazki(conn, scanner);
                            break;
                        case 3:
                            wypozyczKsiazke(conn, scanner);
                            break;
                        case 4:
                            wylistujWypozyczone(conn);
                            break;
                        case 5:
                            wylistujPrzeterminowane(conn);
                            break;
                        case 6:
                            wylistujWszystkie(conn);
                            break;
                        case 7:
                            System.out.println("Koniec programu");
                            conn.close();
                            System.exit(0);
                        default:
                            System.out.println("Niepoprawny wybór, spróbuj ponownie");
                    }
                } catch (InputMismatchException e) {
                    System.out.println("Wprowadzono niepoprawny format danych.");
                    scanner.nextLine();
                }
            }

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createTable(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS ksiazki " +
                "(id INTEGER not NULL AUTO_INCREMENT, " +
                " tytul VARCHAR(255), " +
                " autor VARCHAR(255), " +
                " isbn VARCHAR(13), " +
                " wypozyczona BOOLEAN DEFAULT FALSE, " +
                " wypozyczajacy VARCHAR(255), " +
                " data_wypozyczenia DATE, " +
                " data_zwrotu DATE, " +
                " PRIMARY KEY ( id ))";
        stmt.executeUpdate(sql);
    }

    private static void dodajKsiazke(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Podaj tytuł książki:");
        String tytul = scanner.nextLine();

        System.out.println("Podaj autora książki:");
        String autor = scanner.nextLine();

        System.out.println("Podaj ISBN książki:");
        String isbn = scanner.nextLine();

        String sql = "INSERT INTO ksiazki (tytul, autor, isbn) VALUES (?, ?, ?)";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, tytul);
            preparedStatement.setString(2, autor);
            preparedStatement.setString(3, isbn);
            preparedStatement.executeUpdate();
            System.out.println("Książka dodana do biblioteki");
        }
    }

    private static void wyszukajKsiazki(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Podaj tytuł, autora lub ISBN książki:");
        String query = scanner.next();

        String sql = "SELECT * FROM ksiazki WHERE tytul LIKE ? OR autor LIKE ? OR isbn LIKE ?";
        try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setString(1, "%" + query + "%");
            preparedStatement.setString(2, "%" + query + "%");
            preparedStatement.setString(3, "%" + query + "%");

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                System.out.println("ID: " + resultSet.getInt("id"));
                System.out.println("Tytuł: " + resultSet.getString("tytul"));
                System.out.println("Autor: " + resultSet.getString("autor"));
                System.out.println("ISBN: " + resultSet.getString("isbn"));
                System.out.println("Wypożyczona: " + (resultSet.getBoolean("wypozyczona") ? "Tak" : "Nie"));
                if (resultSet.getBoolean("wypozyczona")) {
                    System.out.println("Wypożyczający: " + resultSet.getString("wypozyczajacy"));
                    System.out.println("Data wypożyczenia: " + resultSet.getDate("data_wypozyczenia"));
                    System.out.println("Data zwrotu: " + resultSet.getDate("data_zwrotu"));
                }
                System.out.println("------------------------------");
            }
        }
    }

    private static void wypozyczKsiazke(Connection conn, Scanner scanner) throws SQLException {
        System.out.println("Podaj ID książki do wypożyczenia:");
        int id = scanner.nextInt();
        System.out.println("Podaj imię i nazwisko wypożyczającego:");
        String wypozyczajacy = scanner.next();

        String checkAvailabilitySQL = "SELECT * FROM ksiazki WHERE id = ? AND wypozyczona = FALSE";
        try (PreparedStatement checkAvailabilityStatement = conn.prepareStatement(checkAvailabilitySQL)) {
            checkAvailabilityStatement.setInt(1, id);
            ResultSet resultSet = checkAvailabilityStatement.executeQuery();

            if (resultSet.next()) {
                String wypozyczSQL = "UPDATE ksiazki SET wypozyczona = TRUE, wypozyczajacy = ?, data_wypozyczenia = NOW(), data_zwrotu = DATE_ADD(NOW(), INTERVAL 14 DAY) WHERE id = ?";
                try (PreparedStatement wypozyczStatement = conn.prepareStatement(wypozyczSQL)) {
                    wypozyczStatement.setString(1, wypozyczajacy);
                    wypozyczStatement.setInt(2, id);
                    int updatedRows = wypozyczStatement.executeUpdate();
                    if (updatedRows > 0) {
                        System.out.println("Książka wypożyczona pomyślnie");
                    } else {
                        System.out.println("Błąd podczas wypożyczania książki");
                    }
                }
            } else {
                System.out.println("Książka niedostępna lub nie istnieje");
            }
        }
    }

    private static void wylistujWypozyczone(Connection conn) throws SQLException {
        String sql = "SELECT * FROM ksiazki WHERE wypozyczona = TRUE";
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                System.out.println("ID: " + resultSet.getInt("id"));
                System.out.println("Tytuł: " + resultSet.getString("tytul"));
                System.out.println("Wypożyczający: " + resultSet.getString("wypozyczajacy"));
                System.out.println("Data wypożyczenia: " + resultSet.getDate("data_wypozyczenia"));
                System.out.println("Data zwrotu: " + resultSet.getDate("data_zwrotu"));
                System.out.println("------------------------------");
            }
        }
    }

    private static void wylistujPrzeterminowane(Connection conn) throws SQLException {
        String sql = "SELECT * FROM ksiazki WHERE wypozyczona = TRUE AND data_zwrotu < NOW()";
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                System.out.println("ID: " + resultSet.getInt("id"));
                System.out.println("Tytuł: " + resultSet.getString("tytul"));
                System.out.println("Wypożyczający: " + resultSet.getString("wypozyczajacy"));
                System.out.println("Data wypożyczenia: " + resultSet.getDate("data_wypozyczenia"));
                System.out.println("Data zwrotu: " + resultSet.getDate("data_zwrotu"));
                System.out.println("------------------------------");
            }
        }
    }

    private static void wylistujWszystkie(Connection conn) throws SQLException {
        String sql = "SELECT * FROM ksiazki";
        try (Statement statement = conn.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            while (resultSet.next()) {
                System.out.println("ID: " + resultSet.getInt("id"));
                System.out.println("Tytuł: " + resultSet.getString("tytul"));
                System.out.println("Autor: " + resultSet.getString("autor"));
                System.out.println("ISBN: " + resultSet.getString("isbn"));
                System.out.println("Wypożyczona: " + (resultSet.getBoolean("wypozyczona") ? "Tak" : "Nie"));
                if (resultSet.getBoolean("wypozyczona")) {
                    System.out.println("Wypożyczający: " + resultSet.getString("wypozyczajacy"));
                    System.out.println("Data wypożyczenia: " + resultSet.getDate("data_wypozyczenia"));
                    System.out.println("Data zwrotu: " + resultSet.getDate("data_zwrotu"));
                }
                System.out.println("------------------------------");
            }
        }
    }
}