package day03;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Task1 {
    public static final String HIGHLIGHT_START = "\033[31;1m";
    public static final String HIGHLIGHT_END = "\033[0m";
    public static final String HIGHLIGHT_PATTERN = Matcher.quoteReplacement(HIGHLIGHT_START) + "$0" + Matcher.quoteReplacement(HIGHLIGHT_END);
    private static final Pattern game = Pattern.compile("Game (\\d+):(.*)");
    private static final Pattern cubeSet = Pattern.compile("(\\d+)\\s(\\w+)");
    private final String file;

    public Task1(String file) {
        this.file = file;
    }

    public static void main(String[] args) {
        var task = new Task1("input_small.txt");
        task.run();
    }

    @SneakyThrows
    private void run() {
        var lines = Files.readAllLines(Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset())
                .stream().filter(s -> !s.isBlank()).map(String::toCharArray).toArray(char[][]::new);

        // builds map of touched cells
        var map = new char[lines.length][lines[0].length];
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            for (int j = 0; j < line.length; j++) {
                if (map[i][j] == '\0') { // not yet marked
                    map[i][j] = '.';
                }
                var ch = line[j];
                if (ch != '.' && !Character.isDigit(ch)) {
                    // symbol, mark surroundings
                    set(map, i - 1, j - 1);
                    set(map, i, j - 1);
                    set(map, i + 1, j - 1);
                    set(map, i, j - 1);
                    set(map, i, j + 1);
                    set(map, i - 1, j + 1);
                    set(map, i, j + 1);
                    set(map, i + 1, j + 1);
                }
            }
        }

        var acc = 0;
        var num = Pattern.compile("\\d+");
        var sb = new StringBuilder();
        var output = new ArrayList<String>();
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            log.info("{}", CharBuffer.wrap(line));
            var matcher = num.matcher(CharBuffer.wrap(line));
            sb.setLength(0);
            sb.append("%04d: ".formatted(i + 1));
            while (matcher.find()) {
                if (check(map[i], matcher.start(), matcher.end())) {
                    var part = Integer.parseInt(matcher.group());
                    log.info("Line {}, part# {} from {} to {}", i, part, matcher.start(), matcher.end());
                    acc += part;
                } else {
                    // highlighting in output
                    matcher.appendReplacement(sb, HIGHLIGHT_PATTERN);
                }
            }
            matcher.appendTail(sb);
            output.add(sb.toString());
        }

        log.info("Result: {}", acc);
        log.info(HIGHLIGHT_START + "Highlighted" + HIGHLIGHT_END + " numbers are not adjacent to a symbol");
        output.forEach(log::warn);
    }

    private boolean check(char[] map, int start, int end) {
        for (int i = start; i < end; i++) {
            if (map[i] == 'x') {
                return true;
            }
        }
        return false;
    }

    private void set(char[][] map, int row, int col) {
        if (row >= 0 && row < map.length) {
            var r = map[row];
            if (col >= 0 && col < r.length) {
                map[row][col] = 'x';
            }
        }
    }

}
