package day08;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

@Slf4j
public class Task1 {
    private static final Pattern INSTRUCTIONS = Pattern.compile("[RL]+");
    private static final Pattern NODE = Pattern.compile("(\\w{3})\\s=\\s\\((\\w{3}),\\s(\\w{3})\\)");
    private final String file;

    public Task1(String file) {
        this.file = file;
    }

    public static void main(String[] args) {
        new Task1("input_small.txt").run();
        new Task1("input_small2.txt").run();
        new Task1("input.txt").run();
    }

    @SneakyThrows
    private void run() {
        var lines = Files.readAllLines(Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset())
          .stream().filter(s -> !s.isBlank()).toList();

        var iterator = lines.iterator();
        var instructions = parseInstructions(iterator);
        var nodes = parseNodes(iterator);

        var current = "AAA";
        while (!"ZZZ".equals(current)) {
            current = instructions.findNext(nodes.get(current));
        }
        log.info("Part 1 result from '{}': {}", file, instructions.counter);
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
            var index = counter++ % instr.length();
            return switch (instr.charAt(index)) {
                case 'L' -> node.left;
                case 'R' -> node.right;
                default -> throw new IllegalStateException("Unexpected value: " + instr.charAt(index));
            };
        }
    }
}
