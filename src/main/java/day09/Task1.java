package day09;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Slf4j
public class Task1 {
    private static final Pattern NUMBER = Pattern.compile("-?\\d+");
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


        var result = lines.stream()
          .map(this::parseSequence)
          .mapToLong(this::calculateNext)
          .sum();

        log.info("Part 1 result from '{}': {}", file, result);

        var result2 = lines.stream()
          .map(this::parseSequence)
          .mapToLong(this::calculatePrev)
          .sum();

        log.info("Part 2 result from '{}': {}", file, result2);
    }

    // recursion version
    private long calculateNext(long[] sequence) {
        // just one number
        if (sequence.length == 1) {
            log.warn("reached end of sequence");
            return sequence[0]; // return it
        }

        var prev = sequence[0];
        var diff = new long[sequence.length - 1];
        boolean allZeroes = true;
        for (int i = 1; i < sequence.length; i++) {
            var currentDiff = sequence[i] - prev;
            prev = sequence[i];
            diff[i - 1] = currentDiff;
            allZeroes &= currentDiff == 0;
        }
        // exit if all zeroes, returning same sequence
        if (allZeroes) {
            return sequence[0];
        }
        // recursion step
        return sequence[sequence.length - 1] + calculateNext(diff);
    }

    // recursion version
    private long calculatePrev(long[] sequence) {
        // just one number
        if (sequence.length == 1) {
            log.warn("reached end of sequence");
            return sequence[0]; // return it
        }

        var prev = sequence[0];
        var diff = new long[sequence.length - 1];
        boolean allZeroes = true;
        for (int i = 1; i < sequence.length; i++) {
            var currentDiff = sequence[i] - prev;
            prev = sequence[i];
            diff[i - 1] = currentDiff;
            allZeroes &= currentDiff == 0;
        }
        // exit if all zeroes, returning same sequence
        if (allZeroes) {
            return sequence[0];
        }
        // recursion step
        return sequence[0] - calculatePrev(diff);
    }

    private long[] parseSequence(String line) {
        return NUMBER.matcher(line).results()
          .map(MatchResult::group)
          .mapToLong(Long::parseLong)
          .toArray();
    }
}
