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

    @SneakyThrows
    private void run() {
        var lines = Files.readAllLines(Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset())
          .stream().toList();

        var iterator = lines.listIterator();
        var seeds = parseSeeds(iterator);
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
        log.info("Result from '{}': {}", file, result);
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
        }
    }

    record Range(long dst, long src, long length) {
        public boolean covers(long key) {
            return key >= src && key < src + length;
        }

        public long map(long key) {
            return dst + (key - src);
        }
    }
}
