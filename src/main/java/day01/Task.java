package day01;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class Task {
    private final String file;

    public Task(String file) {
        this.file = file;
    }

    public static void main(String[] args) {
        var task = new Task("input.txt");
        task.run();
    }

    @SneakyThrows
    private void run() {
        var lines = Files.readAllLines(
                Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset());

        int acc = 0;
        for (var line : lines) {
            char hi = 0, lo = 0;
            for (char ch : line.toCharArray()) {
                if (Character.isDigit(ch)) {
                    hi = hi > 0 ? hi : ch;
                    lo = ch;
                }
            }
            int num = (hi - '0') * 10 + (lo - '0');
            log.info("{}: {}", num, line);
            acc += num;
        }
        log.info("Result: {}", acc);
    }
}