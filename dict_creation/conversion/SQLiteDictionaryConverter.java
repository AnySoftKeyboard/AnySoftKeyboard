// LICENSE: Do what you want. Public domain.
// Creator: Sami Salonen
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;

import javax.xml.parsers.ParserConfigurationException;



public class SQLiteDictionaryConverter {

    static long wordCount = 0;
    static String dictionary = null;

    static String xml = null;

    static String table = null;

    private final static String WORD_COLUMN = "Word";

    private final static String FREQUENCY_COLUMN = "Frequency";

    private final static int FREQUENCY_MIN = 1;

    private final static int FREQUENCY_MAX = Integer.MAX_VALUE;
    private final static String NEWLINE = "\r\n";

    /**
     * @param args
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     */
    public static void main(String[] args) throws ClassNotFoundException, SQLException,
    ParserConfigurationException, SAXException, IOException {
        Class.forName("org.sqlite.JDBC");


        Connection connection = null;
        File file = null;
        BufferedWriter writer = null;

        try {
            // System.out.println(args.length);
            if (args.length != 3 && args.length != 2) {
                System.out.println("Converts SQLite dictionary to xml file");
                System.out.println("Args: <dictionary file> [<table name>] <xml file>");
                return;
            }
            else if (args.length == 2) {
                dictionary = args[0];
                table = dictionary;
                System.out.println("Guessing table name: " + table);
                xml = args[1];
            } else if (args.length == 3) {
                dictionary = args[0];
                table = args[1];
                xml = args[2];
            }
            file = new File(xml);
            try {
                file.createNewFile();
            } catch (final IOException e) {
                System.out.println("XML File creation failed");
                e.printStackTrace();
                return;
            }

            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF8"));
            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
            writer.append(NEWLINE);
            writer.append("<wordlist>");
            writer.append(NEWLINE);

            connection = DriverManager.getConnection("jdbc:sqlite:" + dictionary);
            final Statement statement = connection.createStatement();
            final ResultSet resultSet = statement.executeQuery("SELECT " + WORD_COLUMN + ", "
                    + FREQUENCY_COLUMN + " from " + table + " ORDER BY " + FREQUENCY_COLUMN
                    + " DESC");

            while (resultSet.next()) {
                final String word = resultSet.getString(WORD_COLUMN);
                final int freq = resultSet.getInt(FREQUENCY_COLUMN);
                wordCount++;

                if (freq < FREQUENCY_MIN || freq > FREQUENCY_MAX) {
                    System.out.println("Warning: Frequency is outside of range (" + FREQUENCY_MIN
                            + ", " + FREQUENCY_MAX + ") with word '" + word + "\'");
                }

                writer.append(MessageFormat.format("<w f=\"{0}\">{1}</w>", new Object[] {
                        String.valueOf(freq), word
                }));
                writer.append(NEWLINE);
            }

            writer.append("</wordlist>");

            System.out.println("Output done. Wrote " + wordCount + " words.");



        }
        finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (final SQLException ignored) {
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (final IOException ignored) {
                }

            }
        }
    }

}
