package day01;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class Task2 {
    private static final String[] DIGITS = {
            "zero",
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
            "nine"
    };
    private static final Map<String, Integer> index;

    private static final Pattern pattern;

    static {
        pattern = Pattern.compile(String.join("|", DIGITS) + "|\\d");

        index = new HashMap<>();
        for (int i = 0; i < DIGITS.length; i++) {
            index.put(DIGITS[i], i);            // 'two' -> 2
            index.put(Integer.toString(i), i);  // '2'   -> 2
        }
    }

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
        var lines = Files.readAllLines(
                Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset());

        int acc = 0;
        for (var line : lines) {
            int hi = 0, lo = 0;
            var matcher = pattern.matcher(line);
            while (matcher.find()) {
                int match = index.get(matcher.group());
                hi = hi > 0 ? hi : match;
                lo = match;
                // taking into account numbers with shared characters e.g. oneight
                // set the region start to continue search after the first character of the match, not the full match
                if (matcher.start() + 1 < line.length()) {
                    matcher.region(matcher.start() + 1, line.length());
                }
            }
            int num = hi * 10 + lo;
            log.info("{}: {}", num, line);
            acc += num;
        }
        log.info("Result: {}", acc);
    }
}