package lambdasinaction.chap14;

import java.util.function.Function;

public class Combinators {

    public static void main(String[] args) {
        System.out.println(repeat(3, (Integer x) -> 2 * x).apply(10));
    }

    static <A, B, C> Function<A, C> compose(Function<B, C> g, Function<A, B> f) {
        return x -> g.apply(f.apply(x));
    }

    static <A> Function<A, A> repeat(int n, Function<A, A> f) {
        // 如果 n 的值为 0 直接返回 什么也不做 的标识符
        // 否则执行函数 f, 重复执行 n-1 次，紧接着再执行一次
        return n == 0 ? x -> x : compose(f, repeat(n - 1, f));
    }
}
