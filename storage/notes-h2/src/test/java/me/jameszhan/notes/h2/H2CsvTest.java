package me.jameszhan.notes.h2;

import lombok.extern.slf4j.Slf4j;
import org.h2.tools.Csv;
import org.h2.tools.SimpleResultSet;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;

/**
 * Created by IntelliJ IDEA.
 * User: James Zhan
 * Email: zhiqiangzhan@gmail.com
 * Date: 2018-12-31
 * Time: 00:38
 */
@Slf4j
public class H2CsvTest {

    private final static URL openCoursesUrl = Thread.currentThread().getContextClassLoader().getResource("opencourses.csv");

    @Test
    public void readingCsvFile() throws SQLException {
        try (ResultSet rs = new Csv().read(openCoursesUrl.toExternalForm(), null, null)) {
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                for (int i = 0; i < meta.getColumnCount(); i++) {
                    if (i == meta.getColumnCount() - 1) {
                        log.info("{}: {}\n", meta.getColumnLabel(i + 1), rs.getString(i + 1));
                    } else {
                        log.info("{}: {}", meta.getColumnLabel(i + 1), rs.getString(i + 1));
                    }
                }
            }
        }
    }

    @Test
    public void writingCsvFile() throws SQLException, IOException {
        File targetFile = File.createTempFile("h2database", ".csv");
        try {
            try (SimpleResultSet rs = new SimpleResultSet()) {
                rs.addColumn("NAME", Types.VARCHAR, 255, 0);
                rs.addColumn("EMAIL", Types.VARCHAR, 255, 0);
                rs.addRow("Bob Meier", "bob.meier@abcde.abc");
                rs.addRow("John Jones", "john.jones@abcde.abc");
                try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(targetFile), Charset.forName("UTF-8"))) {
                    new Csv().write(out, rs);
                    log.info("Write rs to {} success.", targetFile);
                }
            }

            try (ResultSet rs = new Csv().read(targetFile.getCanonicalPath(), null, null)) {
                ResultSetMetaData meta = rs.getMetaData();
                while (rs.next()) {
                    for (int i = 0; i < meta.getColumnCount(); i++) {
                        if (i == meta.getColumnCount() - 1) {
                            log.info("{}: {}\n", meta.getColumnLabel(i + 1), rs.getString(i + 1));
                        } else {
                            log.info("{}: {}", meta.getColumnLabel(i + 1), rs.getString(i + 1));
                        }
                    }
                }
            }
        } finally {
            targetFile.deleteOnExit();
        }
    }

    @Test
    public void importingFromCsv() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:")) {
            try (Statement statement = connection.createStatement()) {
                String createSQL = "CREATE TABLE courses AS SELECT * FROM CSVREAD('" + openCoursesUrl + "')";
                statement.execute(createSQL);
            }

            try (Statement statement = connection.createStatement()) {
                String sql = "SELECT * FROM courses";
                try (ResultSet rs = statement.executeQuery(sql)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    while (rs.next()) {
                        for (int i = 0; i < meta.getColumnCount(); i++) {
                            if (i == meta.getColumnCount() - 1) {
                                log.info("{}: {}\n", meta.getColumnLabel(i + 1), rs.getString(i + 1));
                            } else {
                                log.info("{}: {}", meta.getColumnLabel(i + 1), rs.getString(i + 1));
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void importingFromCsvWithSchema() throws SQLException {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:")) {
            try (Statement statement = connection.createStatement()) {
                String createSQL = "CREATE TABLE courses(ID INT PRIMARY KEY, NAME VARCHAR(255), TEACHER VARCHAR(255), "
                        + "PUBLISHER VARCHAR(255), TYPE VARCHAR(255), URL VARCHAR(255)) "
                        + "AS SELECT * FROM CSVREAD('" + openCoursesUrl + "')";
                statement.execute(createSQL);
            }

            try (Statement statement = connection.createStatement()) {
                String sql = "SELECT * FROM courses";
                try (ResultSet rs = statement.executeQuery(sql)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    while (rs.next()) {
                        for (int i = 0; i < meta.getColumnCount(); i++) {
                            if (i == meta.getColumnCount() - 1) {
                                log.info("{}: {}\n", meta.getColumnLabel(i + 1), rs.getString(i + 1));
                            } else {
                                log.info("{}: {}", meta.getColumnLabel(i + 1), rs.getString(i + 1));
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    public void exportingCsvFile() throws SQLException, IOException {
        File targetFile = File.createTempFile("h2database", ".csv");
        try {
            try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:")) {
                try (Statement statement = connection.createStatement()) {
                    String sql = "CREATE TABLE courses(ID INT PRIMARY KEY, NAME VARCHAR(255), TEACHER VARCHAR(255), "
                            + "PUBLISHER VARCHAR(255), TYPE VARCHAR(255), URL VARCHAR(255)) "
                            + "AS SELECT * FROM CSVREAD('" + openCoursesUrl + "')";
                    statement.execute(sql);
                }

                try (Statement statement = connection.createStatement()) {
                    String sql = "CALL CSVWRITE('" + targetFile + "', 'SELECT * FROM courses');";
                    statement.execute(sql);
                }
            }

            URI uri = targetFile.toURI();
            log.info("Begin to read {}.", uri);
            Files.readAllLines(Paths.get(uri), Charset.forName("UTF-8")).forEach(log::info);
        } finally {
            targetFile.deleteOnExit();
        }
    }

}
