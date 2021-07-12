package lambdasinaction.chap7;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public class WordCount {

    public static final String SENTENCE =
            " Nel   mezzo del cammin  di nostra  vita " +
            "mi  ritrovai in una  selva oscura" +
            " che la  dritta via era   smarrita ";

    public static void main(String[] args) {
        System.out.println("Found " + countWordsIteratively(SENTENCE) + " words");
        System.out.println("Found " + countWords(SENTENCE) + " words");
    }

    public static int countWordsIteratively(String s) {
        int counter = 0;
        boolean lastSpace = true;
        for (char c : s.toCharArray()) {
            if (Character.isWhitespace(c)) {
                lastSpace = true;
            } else {
                if (lastSpace) counter++;
                lastSpace = Character.isWhitespace(c);
            }
        }
        return counter;
    }

    public static int countWords(String s) {
        //Stream<Character> stream = IntStream.range(0, s.length())
        //                                    .mapToObj(SENTENCE::charAt).parallel();
        Spliterator<Character> spliterator = new WordCounterSpliterator(s);
        Stream<Character> stream = StreamSupport.stream(spliterator, true);

        return countWords(stream);
    }

    private static int countWords(Stream<Character> stream) {
        WordCounter wordCounter = stream.reduce(new WordCounter(0, true),
                                                WordCounter::accumulate,
                                                WordCounter::combine);
        return wordCounter.getCounter();
    }

    private static class WordCounter {
        private final int counter;
        private final boolean lastSpace;

        public WordCounter(int counter, boolean lastSpace) {
            this.counter = counter;
            this.lastSpace = lastSpace;
        }

        /**
         * 和迭代算法一样，accumulate 方法一个个遍历 Character
         */
        public WordCounter accumulate(Character c) {
            if (Character.isWhitespace(c)) {
                // 上一个字符是空格，而当前遍历的字符不是空格时，将单词计数器加一
                return lastSpace ? this : new WordCounter(counter, true);
            } else {
                return lastSpace ? new WordCounter(counter+1, false) : this;
            }
        }

        /**
         * 合并两个 WordCounter, 把其计数器加起来
         */
        public WordCounter combine(WordCounter wordCounter) {
            // 仅需要计数器的总和，无需关系 lastSpace
            return new WordCounter(counter + wordCounter.counter, wordCounter.lastSpace);
        }

        public int getCounter() {
            return counter;
        }
    }

    private static class WordCounterSpliterator implements Spliterator<Character> {

        private final String string;
        private int currentChar = 0;

        private WordCounterSpliterator(String string) {
            this.string = string;
        }

        @Override
        public boolean tryAdvance(Consumer<? super Character> action) {
            // 处理当前字符
            action.accept(string.charAt(currentChar++));
            // 如果还有字符要处理，则返回 true
            return currentChar < string.length();
        }

        @Override
        public Spliterator<Character> trySplit() {
            int currentSize = string.length() - currentChar;
            if (currentSize < 10) {
                // 返回 null 表示要解析的 String 已经足够小，可以顺序处理
                return null;
            }
            for (int splitPos = currentSize / 2 + currentChar;
                 // 将试探拆分位置设定为要解析的 String 的中间
                 splitPos < string.length();
                 splitPos++) {
                // 让拆分位置前进直到下一个空格
                if (Character.isWhitespace(string.charAt(splitPos))) {
                    // 创建一个新的 WordCounterSpliterator 来解析 String 从开始到拆分位置的部分
                    Spliterator<Character> spliterator = new WordCounterSpliterator(string.substring(currentChar, splitPos));
                    // 将这个 WordCounterSpliterator 的起始位置设为拆分位置
                    currentChar = splitPos;
                    return spliterator;
                }
            }
            return null;
        }

        @Override
        public long estimateSize() {
            return string.length() - currentChar;
        }

        @Override
        public int characteristics() {
            return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
        }
    }
}
