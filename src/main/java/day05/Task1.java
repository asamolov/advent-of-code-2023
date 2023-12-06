package day05;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class Task1 {
    private static final Pattern MAP_HEADER = Pattern.compile("(\\w+)-to-(\\w+) map:");
    private static final Pattern MAP_LINE = Pattern.compile("(\\d+)\\s+(\\d+)\\s+(\\d+)");
    private static final Pattern SEED = Pattern.compile("\\d+");

    private final String file;

    public Task1(String file) {
        this.file = file;
    }

    public static void main(String[] args) {
        new Task1("input_small.txt").run();
        new Task1("input.txt").run();
    }

    private static long totalLength(List<Range> seedRanges) {
        return seedRanges.stream().mapToLong(Range::length).sum();
    }

    @SneakyThrows
    private void run() {
        var lines = Files.readAllLines(Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset())
          .stream().toList();

        var iterator = lines.listIterator();
        var seeds = parseSeeds(iterator);
        var seedRanges = makeRanges(seeds);
        consumeEmptyLines(iterator);
        var maps = new ArrayList<IntervalMap>();
        while (iterator.hasNext()) {
            maps.add(parseMap(iterator));
            consumeEmptyLines(iterator);
        }

        log.info("Seeds: {}", seeds);
        for (IntervalMap map : maps) {
            map.apply(seeds);
            log.info("Mapping {} -> {}: {}", map.from, map.to, seeds);
        }

        var result = Arrays.stream(seeds).min().getAsLong();
        log.info("Part 1 result from '{}': {}", file, result);

        log.info("Initial ranges: {}", seedRanges.size());
        var totalLength = totalLength(seedRanges);
        for (IntervalMap map : maps) {
            seedRanges = map.apply(seedRanges);
            log.info("Mapping {} -> {}: got {} ranges", map.from, map.to, seedRanges.size());
            if (totalLength != totalLength(seedRanges)) {
                log.error("Total length diverged!");
            }
            seedRanges.forEach(range -> log.debug(range.toString()));
        }

        var result2 = seedRanges.stream().mapToLong(Range::src).min().getAsLong();
        log.info("Part 2 result from '{}': {}", file, result2);
    }

    private List<Range> makeRanges(long[] seeds) {
        assert seeds.length % 2 == 0;
        var ranges = new ArrayList<Range>();
        for (int i = 0; i < seeds.length / 2; i++) {
            ranges.add(new Range(seeds[i * 2], seeds[i * 2], seeds[i * 2 + 1]));
        }
        return ranges;
    }

    private IntervalMap parseMap(ListIterator<String> iterator) {
        var map = parseMapHeader(iterator);
        var lines = parseMapLines(iterator);
        map.init(lines);
        return map;
    }

    private List<Range> parseMapLines(ListIterator<String> iterator) {
        var result = new ArrayList<Range>();
        while (iterator.hasNext()) {
            var line = iterator.next();
            var matcher = MAP_LINE.matcher(line);
            if (!matcher.matches()) {
                // found empty line, return it to the iterator
                iterator.previous();
                break;
            }
            result.add(new Range(
              Long.parseLong(matcher.group(1)),
              Long.parseLong(matcher.group(2)),
              Long.parseLong(matcher.group(3))
            ));
        }
        return result;
    }

    private IntervalMap parseMapHeader(ListIterator<String> iterator) {
        var line = iterator.next();
        var matcher = MAP_HEADER.matcher(line);
        if (!matcher.matches()) {
            throw new RuntimeException("Line %d: Cannot parse map header: %s".formatted(iterator.nextIndex(), line));
        }
        // seed-to-soil map:
        return new IntervalMap(matcher.group(1), matcher.group(2));
    }

    private void consumeEmptyLines(ListIterator<String> iterator) {
        while (iterator.hasNext()) {
            if (!iterator.next().isEmpty()) {
                iterator.previous(); // step back
                break;
            }
        }
    }

    private long[] parseSeeds(Iterator<String> iterator) {
        return SEED.matcher(iterator.next()).results()
          .map(MatchResult::group)
          .mapToLong(Long::parseLong)
          .toArray();
    }

    @Value
    static class IntervalMap {
        String from;
        String to;
        List<Range> lines = new ArrayList<>();

        public long get(long key) {
            // should sort the lines and use binary search?
            for (Range line : lines) {
                if (line.covers(key)) {
                    return line.map(key);
                }
            }
            return key;
        }

        public void apply(long[] seeds) {
            for (int i = 0; i < seeds.length; i++) {
                seeds[i] = this.get(seeds[i]);
            }
        }

        public void init(List<Range> lines) {
            this.lines.addAll(lines);
            this.lines.sort(Comparator.comparingLong(Range::src));
            var min = this.lines.getFirst();
            var max = this.lines.getLast();
            if (min.src != 0) {
                this.lines.add(0, new Range(0, 0, min.src)); // from 0 till SRC
            }
            var prev = this.lines.getFirst();
            var ranges = this.lines.listIterator(1);
            while (ranges.hasNext()) {
                var current = ranges.next();
                var prevEnd = prev.src + prev.length;
                var gap = current.src - prevEnd;
                if (gap > 0) {
                    // insert a spacer
                    var spacer = new Range(prevEnd, prevEnd, gap);
                    ranges.set(spacer);
                    ranges.add(current);
                }
                prev = current;
            }
            var highLimit = max.src + max.length;
            this.lines.add(new Range(highLimit, highLimit, 1_000_000_000_000_000_000L)); // from last src till MAX
        }

        public List<Range> apply(List<Range> seedRanges) {
            return seedRanges
              .stream()
              .flatMap(range -> {
                           var newRanges = new ArrayList<Range>();
                           Range reminder = range;
                           for (Range line : lines) {
                               //if (line.covers(reminder)) {
                               if (line.covers(reminder.src)) {
                                   var res = line.map(reminder);
                                   newRanges.add(res.range);
                                   reminder = res.reminder;
                                   if (reminder.isEmpty()) {
                                       break;
                                   }
                               }
                           }
                           return newRanges.stream();
                       }
              ).sorted(Comparator.comparingLong(Range::src))
              .collect(Collectors.toList());
        }
    }

    record Range(long dst, long src, long length) {
        static Range EMPTY = new Range(0, 0, 0);
        public boolean covers(long key) {
            return key >= src && key < src + length;
        }

        public boolean covers(Range range) {
            return range.src < src + length && // covers start
              range.src + range.length > src;  // covers end
        }

        public long map(long key) {
            return dst + (key - src);
        }

        public MappedRange map(Range range) {
            if (!covers(range.src)) {
                throw new RuntimeException("%s not covers %s".formatted(this, range));
            }
            var uncoveredLength = range.src + range.length - (src + length); // negative
            var coveredLength = range.length - Math.max(0, uncoveredLength);
            // what can we map
            var newRange = new Range(map(range.dst), map(range.src), coveredLength);

            // reminder
            var reminder = new Range(range.dst + coveredLength,
                                     range.src + coveredLength,
                                     Math.max(0, uncoveredLength));

            return new MappedRange(newRange, reminder);
        }

        public boolean isEmpty() {
            return length == 0;
        }
    }

    record MappedRange(Range range, Range reminder) {
    }
}
