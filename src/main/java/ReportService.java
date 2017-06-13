import de.vandermeer.asciitable.*;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;

import static de.vandermeer.asciithemes.TA_GridOptions.*;

/**
 * Created by ilya on 02.06.17.
 */
public class ReportService {
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-16");
    private static final String NL = System.lineSeparator();

    private final String settingsFilename;
    private final Path inputPath;
    private final Path outputPath;

    private Settings settings;
    private String[][] inputData;

    public ReportService(String settingsFilename, String inputFilename, String outputFilename) {
        this.settingsFilename = settingsFilename;
        inputPath = Paths.get(inputFilename);
        outputPath = Paths.get(outputFilename);
    }

    public Settings getSettings() {
        return settings;
    }

    public String[][] getInputData() {
        return inputData;
    }

    public void read() throws IOException {
        settings = Settings.parse(settingsFilename);
        inputData = Files.lines(inputPath, DEFAULT_CHARSET).map(x -> x.split("\t")).toArray(String[][]::new);
    }

    public void write() throws IOException {
        String[] columnTitles = settings.columns.stream().map(x -> x.title).toArray(String[]::new);
        String atStr = renderTable(columnTitles, inputData);

        String[] atLines = atStr.split(NL);
        List<List<String>> groups = splitLines(atLines);
        atStr = makePages(groups);

        Files.write(outputPath, atStr.getBytes(DEFAULT_CHARSET));
        System.out.println(atStr);
    }

    private String renderTable(String[] titles, String[][] rows) {
        AsciiTable at = new AsciiTable();

        at.addRule();
        at.addRow(titles);

        for (String[] row : rows) {
            at.addRule();
            at.addRow(row);
        }

        at.getContext().setGridTheme(
            HAS_MID_CONNECTOR |
            HAS_MID_BORDER_LEFT |
            HAS_MID_BORDER_RIGHT |
            HAS_MID_LINE |
            HAS_CONTENT_LEFT |
            HAS_CONTENT_MID |
            HAS_CONTENT_RIGHT
        );

        at.setTextAlignment(TextAlignment.LEFT);
        at.setPaddingLeft(1);
        at.setPaddingRight(1);

        CWC_LongestLine cwc = new CWC_LongestLine();

        settings.columns.forEach(x -> {
            cwc.add(x.width + 2, x.width + 2);
        });

        at.getRenderer().setCWC(cwc);

        return at.render();
    }

    private List<List<String>> splitLines(String[] lines) {
        List<String> currentGroup = new ArrayList<>();
        List<List<String>> groups = new ArrayList<>();

        groups.add(new ArrayList<String>() {{ add(lines[1]); }});

        for (int i = 2; i < lines.length; i++) {
            String line = lines[i];

            if (!currentGroup.isEmpty() && !line.contains(" ")) {
                groups.add(currentGroup);
                currentGroup = new ArrayList<>();
            }

            currentGroup.add(line);
        }

        if (!currentGroup.isEmpty())
            groups.add(currentGroup);

        return groups;
    }

    private String makePages(List<List<String>> groups) {
        List<String> result = new ArrayList<>();

        final String splitter = groups.get(0).get(0);

        for (int i = 0, sum = 0; i < groups.size(); i++) {
            List<String> group = groups.get(i);

            sum += group.size();

            if (sum > settings.page.height) {
                result.add("~");
                result.add(splitter);
                sum = 0;
            }

            result.addAll(group);
        }

        return String.join(NL, result);
    }
}
