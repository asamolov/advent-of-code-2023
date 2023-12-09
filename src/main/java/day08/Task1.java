package day08;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
    private static final Pattern INSTRUCTIONS = Pattern.compile("[RL]+");
    private static final Pattern NODE = Pattern.compile("(\\w{3})\\s=\\s\\((\\w{3}),\\s(\\w{3})\\)");
    private final String file;
    private final boolean findAll;

    public Task1(String file, boolean findAll) {
        this.file = file;
        this.findAll = findAll;
    }

    public static void main(String[] args) {
//        new Task1("input_small.txt", false).run();
//        new Task1("input_small2.txt", false).run();
//        new Task1("input.txt", false).run();
        new Task1("input_small3.txt", true).run();
        new Task1("input.txt", true).run();
    }

    @SneakyThrows
    private void run() {
        var lines = Files.readAllLines(Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset())
          .stream().filter(s -> !s.isBlank()).toList();

        var iterator = lines.iterator();
        var instructions = parseInstructions(iterator);
        var nodes = parseNodes(iterator);

        List<String> allStarts;
        if (this.findAll) {
            allStarts = nodes.keySet().stream().filter(id -> id.endsWith("A")).collect(Collectors.toList());
        } else {
            allStarts = new ArrayList<>();
            allStarts.add("AAA");
        }

        var loops = new ArrayList<>();
        for (String start : allStarts) {
            instructions.reset();
            var current = start;
            var visited = new HashMap<String, Integer>();
            Integer lastVisitedOn;
            while (true) {
                current = instructions.findNext(nodes.get(current));
                lastVisitedOn = visited.put(current + instructions.index(), instructions.counter);
                if (lastVisitedOn == null) {
                    instructions.next();
                } else {
                    // found loop
                    log.info("Found loop after {} iterations", instructions.counter);
                    log.info("Start: {}, period: {}", lastVisitedOn, instructions.counter - lastVisitedOn);
                    break;
                }
            }
        }

        log.info("Part {} result from '{}': {}", findAll ? 2 : 1, file, instructions.counter);
    }

    private boolean shouldStop(List<String> current) {
        if (findAll) {
            return current.stream().allMatch(id -> id.endsWith("Z"));
        } else {
            return current.stream().allMatch("ZZZ"::equals);
        }
    }

    private Map<String, Node> parseNodes(Iterator<String> iterator) {
        var result = new HashMap<String, Node>();
        while (iterator.hasNext()) {
            var line = iterator.next();
            log.debug(line);
            var matcher = NODE.matcher(line);
            if (!matcher.matches()) {
                break;
            }

            var node = new Node(
              matcher.group(1),
              matcher.group(2),
              matcher.group(3)
            );
            log.debug(node.toString());
            result.put(node.id, node);
        }
        return result;
    }

    private Instructions parseInstructions(Iterator<String> iterator) {
        return new Instructions(
          INSTRUCTIONS.matcher(iterator.next())
            .results()
            .map(MatchResult::group)
            .findFirst().orElseThrow()
        );
    }

    record Node(String id, String left, String right) {
    }

    @RequiredArgsConstructor
    static class Instructions {
        final String instr;
        int counter = 0;

        String findNext(Node node) {
            return switch (direction()) {
                case 'L' -> node.left;
                case 'R' -> node.right;
                default -> throw new IllegalStateException("Unexpected value: " + direction());
            };
        }

        char direction() {
            var index = index();
            return instr.charAt(index);
        }

        int index() {
            return counter % instr.length();
        }

        void next() {
            counter++;
        }

        void reset() {
            counter = 0;
        }
    }
}
