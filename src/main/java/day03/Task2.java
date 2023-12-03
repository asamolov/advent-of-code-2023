package day03;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Task2 {
    public static final String HIGHLIGHT_START = "\033[31;1m";
    public static final String HIGHLIGHT_END = "\033[0m";
    public static final String HIGHLIGHT_PATTERN = Matcher.quoteReplacement(HIGHLIGHT_START) + "$0" + Matcher.quoteReplacement(HIGHLIGHT_END);
    private static final Pattern game = Pattern.compile("Game (\\d+):(.*)");
    private static final Pattern cubeSet = Pattern.compile("(\\d+)\\s(\\w+)");
    private final String file;

    public Task2(String file) {
        this.file = file;
    }

    public static void main(String[] args) {
        var task = new Task2("input.txt");
        task.run();
    }

    @SneakyThrows
    private void run() {
        var lines = Files.readAllLines(Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset())
                .stream().filter(s -> !s.isBlank()).map(String::toCharArray).toArray(char[][]::new);

        // builds map of gears
        var map = new int[lines.length][lines[0].length];
        var gear = 0;
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            for (int j = 0; j < line.length; j++) {
                if (map[i][j] == 0) { // not yet marked
                    map[i][j] = -1; // not a gear
                }
                var ch = line[j];
                if (ch == '*') {
                    // gear, mark surroundings with the gear number
                    gear++;
                    set(map, gear, i - 1, j - 1);
                    set(map, gear, i, j - 1);
                    set(map, gear, i + 1, j - 1);
                    set(map, gear, i - 1, j);
                    set(map, gear, i + 1, j);
                    set(map, gear, i - 1, j + 1);
                    set(map, gear, i, j + 1);
                    set(map, gear, i + 1, j + 1);
                }
            }
        }

        var gears = new HashMap<Integer, List<Integer>>();
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
                gear = check_gear(map[i], matcher.start(), matcher.end());
                if (gear != -1) {
                    var ratio = Integer.parseInt(matcher.group());
                    log.info("Line {}, gear# {}, ratio {} from {} to {}", i, gear, ratio, matcher.start(), matcher.end());
                    // list, since we need to only take into account gears with 2 ratios
                    gears.computeIfAbsent(gear, k -> new ArrayList<>()).add(ratio);
                    // highlighting in output
                    matcher.appendReplacement(sb, HIGHLIGHT_PATTERN);
                }
            }
            matcher.appendTail(sb);
            output.add(sb.toString());
        }

        log.info(HIGHLIGHT_START + "Highlighted" + HIGHLIGHT_END + " numbers are gear ratios");
        output.forEach(log::warn);

        // computing the result
        var result = gears.values().stream()
                .filter(ratios -> ratios.size() == 2)
                .map(l -> l.stream().reduce(1, (a, b) -> a * b))
                .reduce(0, Integer::sum);

        log.info("Result: {}", result);
    }

    private int check_gear(int[] map, int start, int end) {
        for (int i = start; i < end; i++) {
            if (map[i] != -1) {
                return map[i];
            }
        }
        return -1;
    }

    private void set(int[][] map, int gear, int row, int col) {
        if (row >= 0 && row < map.length) {
            var r = map[row];
            if (col >= 0 && col < r.length) {
                map[row][col] = gear;
            }
        }
    }

}
