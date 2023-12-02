package day02;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Pattern;

@Slf4j
public class Task2 {
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
                .stream().filter(s -> !s.isBlank()).toList();

        int lineN = 0;
        int acc = 0;
        game_loop:
        for (var line : lines) {
            lineN++;
            var g = game.matcher(line);
            if (!g.matches()) {
                throw new RuntimeException("incorrect line %d: %s".formatted(lineN, line));
            }
            var id = Integer.parseInt(g.group(1));

            var maxCubes = new HashMap<String, Integer>();
            for (var set : g.group(2).split(";")) {
                var matcher = cubeSet.matcher(set);
                while (matcher.find()) {
                    var count = Integer.parseInt(matcher.group(1));
                    var color = matcher.group(2);
                    maxCubes.merge(color, count, Integer::max);
                }
            }
            if (maxCubes.size() != 3) {
                log.warn("Game {} - only {} colors found!", id, maxCubes.size());
            }
            var power = maxCubes.values().stream().reduce(1, (a, b) -> a * b);
            log.info("Game {} - power {}", id, power);
            acc += power;
        }
        log.info("Result: {}", acc);
    }

}
