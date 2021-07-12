package lambdasinaction.chap7;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.LongStream;

import static lambdasinaction.chap7.ParallelStreamsHarness.FORK_JOIN_POOL;

/**
 * 继承 RecursiveTask 来创建可以用于 fork/join 框架的任务
 */
public class ForkJoinSumCalculator extends RecursiveTask<Long> {

    // 不再将任务分解为子任务的数组大小
    public static final long THRESHOLD = 10_000;

    // 要求和的数组
    private final long[] numbers;
    // 子任务处理的数组的起始位置
    private final int start;
    // 子任务处理的数组的终止位置
    private final int end;

    // 公共构造函数用于创建主任务
    public ForkJoinSumCalculator(long[] numbers) {
        this(numbers, 0, numbers.length);
    }

    // 私有构造函数用于以递归的方式为主任务创建子任务
    private ForkJoinSumCalculator(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    // 覆盖 RecursiveTask 抽象方法
    @Override
    protected Long compute() {
        // 该任务负责求和的部分的大小
        int length = end - start;
        if (length <= THRESHOLD) {
            // 如果大小小于或者等于阈值，则顺序计算结果
            return computeSequentially();
        }
        // 创建一个子任务来为数组的前一半求和
        ForkJoinSumCalculator leftTask = new ForkJoinSumCalculator(numbers, start, start + length/2);
        // 利用另一个 ForkJoinPool 线程异步执行新创建的子任务
        leftTask.fork();
        // 创建一个子任务来为数组的后一半求和
        ForkJoinSumCalculator rightTask = new ForkJoinSumCalculator(numbers, start + length/2, end);
        // 同步执行第二个子任务，有可能允许进一步递归划分
        Long rightResult = rightTask.compute();
        // 读取第一个子任务的结果，如果尚未完成就等待
        Long leftResult = leftTask.join();
        // 该任务的结果是两个子任务结果的组合
        return leftResult + rightResult;
    }

    // 在子任务不再可分时计算结果的简单算法
    private long computeSequentially() {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }

    public static long forkJoinSum(long n) {
        long[] numbers = LongStream.rangeClosed(1, n).toArray();
        ForkJoinTask<Long> task = new ForkJoinSumCalculator(numbers);
        return FORK_JOIN_POOL.invoke(task);
    }
}