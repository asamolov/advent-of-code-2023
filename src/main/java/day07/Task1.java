package day07;

import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Pattern;

@Slf4j
public class Task1 {
    private static final Pattern HAND_AND_BID = Pattern.compile("([\\w\\d]{5})\\s(\\d+)");
    private final String file;
    private final BiFunction<String, Integer, Hand> handFactory;
    private final boolean withJokers;
    private final Comparator<Hand> handComparator;

    public Task1(String file, boolean withJokers) {
        this.file = file;
        this.handFactory = withJokers ? Hand::withJokers : Hand::withoutJokers;
        this.handComparator = withJokers ? Hand.HAND_COMPARATOR_WITH_JOKERS : Hand.HAND_COMPARATOR;
        this.withJokers = withJokers;
    }

    public static void main(String[] args) {
        new Task1("input_small.txt", false).run();
        new Task1("input.txt", false).run();
        new Task1("input_small.txt", true).run();
        new Task1("input.txt", true).run();
    }

    @SneakyThrows
    private void run() {
        var lines = Files.readAllLines(Paths.get(this.getClass().getResource(file).toURI()), Charset.defaultCharset())
          .stream().filter(s -> !s.isBlank()).toList();

        var hands = lines.stream().map(this::parseHand).sorted(handComparator).toList();
        var acc = 0;
        for (int i = 0; i < hands.size(); i++) {
            log.info("Rank {}: {}", i + 1, hands.get(i));
            acc += (i + 1) * hands.get(i).bid;
        }

        log.info("Part with{} jokers result from '{}': {}",
                 withJokers ? "" : "out",
                 file, acc);
    }

    private Hand parseHand(String line) {
        var matcher = HAND_AND_BID.matcher(line);
        if (!matcher.matches()) throw new RuntimeException("Cannot parse line: " + line);
        return handFactory.apply(matcher.group(1), Integer.parseInt(matcher.group(2)));
    }

    @Value
    static class Hand {
        String hand;
        int bid;
        int strength;

        static final Map<Character, Integer> cardStrength;
        static final Map<Character, Integer> cardStrengthWithJokers;

        static {
            cardStrength = new HashMap<>();
            cardStrength.put('A', 14);
            cardStrength.put('K', 13);
            cardStrength.put('Q', 12);
            cardStrength.put('J', 11);
            cardStrength.put('T', 10);
            cardStrength.put('9', 9);
            cardStrength.put('8', 8);
            cardStrength.put('7', 7);
            cardStrength.put('6', 6);
            cardStrength.put('5', 5);
            cardStrength.put('4', 4);
            cardStrength.put('3', 3);
            cardStrength.put('2', 2);

            cardStrengthWithJokers = new HashMap<>(cardStrength);
            cardStrengthWithJokers.put('J', 1);
        }

        static final Comparator<Hand> HAND_COMPARATOR = getHandComparator(cardStrength);
        static final Comparator<Hand> HAND_COMPARATOR_WITH_JOKERS = getHandComparator(cardStrengthWithJokers);

        private static Comparator<Hand> getHandComparator(Map<Character, Integer> cardStrength) {
            return Comparator.comparing(Hand::getStrength)
              .thenComparing((o1, o2) -> {
                  for (int i = 0; i < o1.hand.length(); i++) {
                      var ch1 = o1.hand.charAt(i);
                      var ch2 = o2.hand.charAt(i);
                      if (ch1 != ch2) {
                          return Integer.compare(
                            cardStrength.get(ch1),
                            cardStrength.get(ch2)
                          );
                      }
                  }
                  return 0;
              });
        }

        static Hand withJokers(String hand, int bid) {
            var map = new HashMap<Character, Integer>();
            for (int i = 0; i < hand.length(); i++) {
                var ch = hand.charAt(i);
                map.merge(ch, 1, Integer::sum);
            }
            var nJokers = map.getOrDefault('J', 0);
            map.remove('J');

            var array = map.values().stream().sorted(Collections.reverseOrder()).mapToInt(Integer::intValue).toArray();

            if (array.length == 0) {
                // it's 5 of jokers
                array = new int[1];
            }
            // use jokers for the top combination
            array[0] += nJokers;

            int strength = 0;
            for (int n : array) { // 5 of kind are 50000, 4 of kind are transformed to 41000. 3 + 2 = 32000 etc.
                strength += n;
                strength *= 10;
            }
            for (int i = array.length; i < hand.length(); i++) {
                strength *= 10;
            }
            return new Hand(hand, bid, strength);
        }

        static Hand withoutJokers(String hand, int bid) {
            var map = new HashMap<Character, Integer>();
            for (int i = 0; i < hand.length(); i++) {
                var ch = hand.charAt(i);
                map.merge(ch, 1, Integer::sum);
            }
            var array = map.values().stream().sorted(Collections.reverseOrder()).mapToInt(Integer::intValue).toArray();

            int strength = 0;
            for (int n : array) { // 4 of kind are transformed to 41000. 3 + 2 = 32000 etc.
                strength += n;
                strength *= 10;
            }
            for (int i = array.length; i < hand.length(); i++) {
                strength *= 10;
            }
            return new Hand(hand, bid, strength);
        }
    }
}
