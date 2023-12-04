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
public class Task2 {
    private static final Pattern card = Pattern.compile("(Card\\s+\\d+):([\\d\\s]+)\\|([\\d\\s]+)");
    private final String file;

    public Task2(String file) {
        this.file = file;
    }

    public static void main(String[] args) {
        new Task2("input_small.txt").run();
        new Task2("input.txt").run();
    }

    @SneakyThrows
    private void run() {
        var lines = Files.readAllLines(Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset())
                .stream().filter(s -> !s.isBlank()).toList();

        int acc = 0;
        var counters = new int[lines.size()];
        Arrays.fill(counters, 1);
        for (int i = 0; i < lines.size(); i++) {
            var line = lines.get(i);
            var g = card.matcher(line);
            if (!g.matches()) {
                throw new RuntimeException("incorrect line: " + line);
            }
            var winningNumbers = Set.of(g.group(2).trim().split("\\s+"));
            var yourNumbers = new HashSet<>(Arrays.asList(g.group(3).trim().split("\\s+")));
            yourNumbers.retainAll(winningNumbers);

            var nCopies = counters[i];
            acc += nCopies;
            for (int j = i + 1; j < i + 1 + yourNumbers.size(); j++) {
                counters[j] += nCopies;
            }
            log.warn("{}: {} copies", g.group(1), nCopies);
        }
        log.info("Result from '{}': {}", file, acc);
    }

}
