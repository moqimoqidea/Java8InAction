package lambdasinaction.chap6;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.stream.Collector.Characteristics.*;

public class ToListCollector<T> implements Collector<T, List<T>, List<T>> {

    @Override
    public Supplier<List<T>> supplier() {
        // 创建集合操作的起始点
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        // 累积遍历过的元素，原位修改累加器
        return List::add;
    }

    @Override
    public Function<List<T>, List<T>> finisher() {
        // 恒等函数
        return Function.identity();
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        // 修改第一个累加器，将其与第二个累加器的内容合并
        return (list1, list2) -> {
            list1.addAll(list2);
            // 返回修改后的第一个累加器
            return list1;
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        // 为收集器增加 IDENTITY_FINISH 和 CONCURRENT 标志
        return Collections.unmodifiableSet(EnumSet.of(IDENTITY_FINISH, CONCURRENT));
    }
}
