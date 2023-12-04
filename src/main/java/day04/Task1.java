package day04;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class Task1 {
    private static final Pattern card = Pattern.compile("(Card\\s+\\d+):([\\d\\s]+)\\|([\\d\\s]+)");
    private final String file;

    public Task1(String file) {
        this.file = file;
    }

    public static void main(String[] args) {
        new Task1("input_small.txt").run();
        new Task1("input.txt").run();
    }

    @SneakyThrows
    private void run() {
        var lines = Files.readAllLines(Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset())
                .stream().filter(s -> !s.isBlank()).toList();

        int acc = 0;
        for (var line : lines) {
            var g = card.matcher(line);
            if (!g.matches()) {
                throw new RuntimeException("incorrect line: " + line);
            }
            var winningNumbers = Set.of(g.group(2).trim().split("\\s+"));
            var yourNumbers = new HashSet<>(Arrays.asList(g.group(3).trim().split("\\s+")));
            yourNumbers.retainAll(winningNumbers);
            int points = 0;
            if (!yourNumbers.isEmpty()) {
                points = 1 << yourNumbers.size() - 1;
            }
            log.warn("{} gives {} points", g.group(1), points);
            acc += points;
        }
        log.info("Result from '{}': {}", file, acc);
    }

}
