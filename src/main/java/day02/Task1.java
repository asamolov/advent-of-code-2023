package day02;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;

@Slf4j
public class Task1 {
    public static final Map<String, Integer> maxCubes = Map.of(
            "red", 12,
            "green", 13,
            "blue", 14
    );
    private static final Pattern game = Pattern.compile("Game (\\d+):(.*)");
    private static final Pattern cubeSet = Pattern.compile("(\\d+)\\s(\\w+)");
    private final String file;
    public Task1(String file) {
        this.file = file;
    }

    public static void main(String[] args) {
        var task = new Task1("input.txt");
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
            for (var set : g.group(2).split(";")) {
                var matcher = cubeSet.matcher(set);
                while (matcher.find()) {
                    var count = Integer.parseInt(matcher.group(1));
                    var color = matcher.group(2);
                    if (count > maxCubes.get(color)) {
                        log.warn("Game {} - impossible combination of {} {} cubes", id, count, color);
                        continue game_loop;
                    }
                }
            }
            acc += id;
        }
        log.info("Result: {}", acc);
    }

}
