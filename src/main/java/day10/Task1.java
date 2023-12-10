package day10;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static day10.Task1.Direction.*;

@Slf4j
public class Task1 {

    private char[][] lines;

    @AllArgsConstructor
    enum Direction {
        N(0, -1), E(1, 0), S(0, 1), W(-1, 0);

        final int dx;
        final int dy;

        int goX(int x) {
            return x + this.dx;
        }

        int goY(int y) {
            return y + this.dy;
        }

        Direction opposite() {
            return switch (this) {
                case N -> S;
                case E -> W;
                case S -> N;
                case W -> E;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    enum Pipe {
        // instead of manually writing possible top/right/bottom/left connectors
        // > VERTICAL('|', Set.of('|', '7', 'F'), Set.of(), Set.of('|', 'L', 'J'), Set.of()),
        // we will build the map using connection properties
        // third iteration - use not bools, but set of directions the symbol connects
        START('S', N, E, S, W), // can connect in any direction
        GROUND('.'),            // no connections at all
        VERTICAL('|', N, S),
        HORIZONTAL('-', E, W),
        NE_BEND('L', N, E),
        NW_BEND('J', N, W),
        SW_BEND('7', S, W),
        SE_BEND('F', E, S);

        static Map<Character, Pipe> INDEX = Arrays.stream(values())
          .collect(Collectors.toMap(Pipe::getSymbol, Function.identity()));

        Pipe(char c, Direction... directions) {
            this.symbol = c;
            this.directions = Set.of(directions);
        }

        static Pipe find(char symbol) {
            return INDEX.get(symbol);
        }

        boolean canConnect(char ch, Direction d) {
            return canConnect(find(ch), d);
        }

        boolean canConnect(Pipe other, Direction d) {
            // this should have connection in direction d
            // and other should have connection in opposite direction
            return this.directions.contains(d) && other.directions.contains(d.opposite());
        }

        private final char symbol;
        private final Set<Direction> directions;
    }

    record Node(int x, int y, Pipe pipe, Direction from, int depth) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return x == node.x && y == node.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }

    private final String file;

    @SneakyThrows
    public Task1(String file) {
        this.file = file;
        this.lines = Files.readAllLines(Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset())
          .stream().filter(s -> !s.isBlank()).map(String::toCharArray).toArray(char[][]::new);
    }

    public static void main(String[] args) {
        new Task1("input_small1.txt").run();
        new Task1("input_small2.txt").run();
        new Task1("input_small3.txt").run();
        new Task1("input.txt").run();
    }

    @SneakyThrows
    private void run() {
        int x = -1, y = -1;

        outer:
        for (int i = 0; i < lines.length; i++) {
            var line = lines[i];
            for (int j = 0; j < line.length; j++) {
                if ('S' == line[j]) {
                    x = j;
                    y = i;
                    break outer;
                }
            }
        }

        var start = Pipe.START;

        var n = new Node(x, y, start, null, 0);
        var nodes = new ArrayList<Node>();
        for (Direction value : values()) {
            attempt(n, value).ifPresent(nodes::add);
        }

        do {
            nodes.replaceAll(this::step);
        } while (notSame(nodes));

        log.info("Part 1 result from '{}': {}", file, nodes.get(0).depth);
    }

    private boolean notSame(List<Node> nodes) {
        return nodes.stream().distinct().count() > 1;
    }

    private Node step(Node node) {
        return node.pipe.directions.stream().filter(d -> d != node.from)
          .flatMap(d -> attempt(node, d).stream())
          .findFirst()
          .get();
    }

    private Optional<Node> attempt(Node n, Direction direction) {
        var newY = direction.goY(n.y);
        if (invalidIndex(newY, lines.length)) {
            return Optional.empty();
        }
        var line = lines[newY];
        var newX = direction.goX(n.x);
        if (invalidIndex(newX, line.length)) {
            return Optional.empty();
        }
        var ch = line[newX];
        var newPipe = Pipe.find(ch);
        if (n.pipe.canConnect(ch, direction)) {
            return Optional.of(new Node(newX, newY, newPipe, direction.opposite(), n.depth + 1));
        } else {
            return Optional.empty();
        }
    }

    private boolean invalidIndex(int index, int length) {
        return index < 0 || index >= length;
    }

}
