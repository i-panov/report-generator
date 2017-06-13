import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.*;

/**
 * Created by ilya on 02.06.17.
 */
@Root(name = "settings")
public class Settings {
    public static class Page {
        @Element
        public Integer width;

        @Element
        public Integer height;
    }

    public static class Column {
        @Element
        public String title;

        @Element
        public Integer width;
    }

    @Element
    public Page page;

    @ElementList
    public List<Column> columns;

    public Boolean isValid() {
        Integer sum = columns
                .stream()
                .flatMapToInt(x -> IntStream.of(x.width + 3)) // 1 разделитель + 2 пробела
                .sum() + 1; // + 1 разделитель на последний столбец

        return page.width.equals(sum);
    }

    public static Settings parse(String filename) {
        Settings settings = tryParse(filename);

        if (settings == null || !settings.isValid()) {
            throw new IllegalArgumentException("Файл настроек содержал некорректные данные.");
        }

        return settings;
    }

    private static Settings tryParse(String filename) {
        try {
            try (Reader reader = Files.newBufferedReader(Paths.get(filename))) {
                return new Persister().read(Settings.class, reader);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
