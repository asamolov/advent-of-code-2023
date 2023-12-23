package day12;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class Task1 {
    private static final Pattern RECORD = Pattern.compile("([.?#]+)\\s+([\\d,]+)");
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
          .map(this::parseRecord)
          .mapToInt(this::calculateArrangements)
          .sum();

        log.info("Part 1 result from '{}': {}", file, result);
    }

    private int calculateArrangements(Springs record) {
        // input: ???.### 1,1,3
        // 1. build regexp from groups to match. e.g. 1,1,3 should be .*#.+#.+###.*
        var regexp = buildRegexp(record.groups());
        // 2. either ? can be . or #. we need to recursively try to match the input
        // conditions line with the regexp we created.
        var matchers = new ArrayList<DFA>();
        matchers.add(regexp);
        log.warn("Checking {}", record);
        for (int i = 0; i < record.conditions().length(); i++) {
            var ch = record.conditions().charAt(i);

            // iterate over matchers
            var it = matchers.listIterator();
            while (it.hasNext()) {
                var dfa = it.next();
                log.warn("{}: {} <-- {}", i, dfa, ch);
                it.remove();
                if (ch == '.' || ch == '?') {
                    var dotDfa = dfa.dup();
                    if (dotDfa.accept('.')) {
                        it.add(dotDfa);
                    }
                }
                if (ch == '#' || ch == '?') {
                    var hashDfa = dfa.dup();
                    if (hashDfa.accept('#')) {
                        it.add(hashDfa);
                    }
                }
            }
        }

        log.debug("final arrangements:");
        matchers.forEach(dfa -> log.debug("\t{}", dfa));
        // remove non fully matched lines
        matchers.removeIf(dfa -> !dfa.atEnd());

        // 3. each matching line counts as 1 arrangement
        log.debug("{}: {} arrangements", record.conditions, matchers.size());
        return matchers.size();
    }

    private DFA buildRegexp(int[] groups) {
        // see https://cyberzhg.github.io/toolbox/min_dfa?regex=LiojLisjLisjIyMuKg==
        var dfa = new DFABuilder();
        dfa.star('.'); // 0 or more . at the start
        for (int i = 0; i < groups.length; i++) {
            if (i != 0) {
                dfa.ch('.', 1); // . is requited between a pair of #
            }
            dfa.star('.'); // .+ == ..*
            dfa.ch('#', groups[i]);
        }
        dfa.star('.'); // 0 or more . at the end

        return dfa.build();
    }

    static class Node {
        Map<Character, Node> transitions = new HashMap<>();

        public void on(char ch, Node node) {
            transitions.put(ch, node);
        }

        public Node next(char ch) {
            return transitions.get(ch);
        }
    }

    static class DFA {
        private final Node root;
        private final Node last;
        private Node current;
        private final List<Character> accepted;

        public DFA(Node root, Node last) {
            this(root, last, root, new ArrayList<>());
        }

        public DFA(Node root, Node last, Node current, List<Character> accepted) {
            this.root = root;
            this.last = last;
            this.current = current;
            this.accepted = new ArrayList<>(accepted);
        }

        public DFA dup() {
            return new DFA(root, last, current, accepted);
        }

        public boolean atEnd() {
            return current == last;
        }

        public boolean accept(char ch) {
            var next = current.next(ch);
            if (next != null) {
                current = next;
                accepted.add(ch);
            }
            return next != null;
        }

        @Override
        public String toString() {
            return "end: " + atEnd() + " acc: " + accepted.stream()
              .map(Objects::toString)
              .collect(Collectors.joining(""));
        }
    }

    static class DFABuilder {
        Node root, last;

        public DFABuilder() {
            root = last = new Node(); // adds root
        }

        public void star(char ch) {
            last.on(ch, last); // transition onto itself
        }

        public void ch(char ch, int count) {
            for (int i = 0; i < count; i++) {
                var next = new Node();
                last.on(ch, next);
                last = next;
            }
        }

        public DFA build() {
            return new DFA(root, last);
        }
    }

    record Springs(String conditions, int[] groups) {
        @Override
        public String toString() {
            return "{" +
              "conditions=" + conditions +
              ", groups=" + Arrays.toString(groups) +
              '}';
        }
    }

    private Springs parseRecord(String line) {
        var matcher = RECORD.matcher(line);
        if (!matcher.matches()) {
            throw new RuntimeException("Cannot parse: " + line);
        }
        return new Springs(
          matcher.group(1),
          Arrays.stream(matcher.group(2).split(","))
            .mapToInt(Integer::parseInt).toArray()
        );
    }
}
