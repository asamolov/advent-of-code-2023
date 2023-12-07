package day06;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class Task1 {
    private static final Pattern DIGIT = Pattern.compile("\\d+");
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

        var it = lines.iterator();
        var time = parseDigits(it);
        var distance = parseDigits(it);

        assert time.length == distance.length;

        long acc = 1;
        for (int i = 0; i < time.length; i++) {
            var nOptions = winningOptions(time[i], distance[i]);
            log.debug("Race {} ({}, {}): {} winning options", i, time[i], distance[i], nOptions);
            acc *= nOptions;
        }

        log.info("Part 1 result from '{}': {}", file, acc);

        it = lines.iterator();
        var totalTime = parseLongNumber(it);
        var totalDistance = parseLongNumber(it);
        log.info("Part 2 result from '{}': {}", file, winningOptions(totalTime, totalDistance));
    }

    private long parseLongNumber(Iterator<String> iterator) {
        var longNumber = DIGIT.matcher(iterator.next()).results()
          .map(MatchResult::group)
          .collect(Collectors.joining());
        return Long.parseLong(longNumber);
    }

    private long winningOptions(long time, long distance) {
        // solving square equation
        var discriminator = Math.sqrt(time * time - 4 * distance);
        var minRoot = (time - discriminator) / 2;
        var min = Math.round(Math.floor(minRoot + 1));
        var maxRoot = (time + discriminator) / 2;
        var max = Math.round(Math.ceil(maxRoot - 1)); // since the solution should not include the roots
        log.debug("\t min root: {}, max root: {}", minRoot, maxRoot);
        return max - min + 1;
    }

    private long[] parseDigits(Iterator<String> iterator) {
        return DIGIT.matcher(iterator.next()).results()
          .map(MatchResult::group)
          .mapToLong(Long::parseLong)
          .toArray();
    }

}
