package lambdasinaction.chap11;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Util {

    private static final Random RANDOM = new Random(0);
    private static final DecimalFormat formatter = new DecimalFormat("#.##", new DecimalFormatSymbols(Locale.US));

    public static void delay() {
        int delay = 1000;
        // dynamic delay could show `CompletableFuture's` ability better.
        //     [BestPrice price is 110.93, LetsSaveBig price is 135.58, MyFavoriteShop price is 192.72, BuyItAll price is 184.74, ShopEasy price is 167.28]
        //     sequential done in 15281 msecs
        //     [BestPrice price is 117.57, LetsSaveBig price is 174.03, MyFavoriteShop price is 173.77, BuyItAll price is 169.89, ShopEasy price is 176.43]
        //     parallel done in 3966 msecs
        //     [BestPrice price is 204.78, LetsSaveBig price is 190.85, MyFavoriteShop price is 128.92, BuyItAll price is 140.31, ShopEasy price is 166.1]
        //     composed CompletableFuture done in 3784 msecs
        //     BuyItAll price is 111.53 (done in 1530 msecs)
        //     MyFavoriteShop price is 119.11 (done in 2916 msecs)
        //     BestPrice price is 127.88 (done in 3393 msecs)
        //     LetsSaveBig price is 147.21 (done in 3910 msecs)
        //     ShopEasy price is 224.23 (done in 4556 msecs)
        //     All shops have now responded in 4556 msecs
        // int delay = 500 + RANDOM.nextInt(2000);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static double format(double number) {
        synchronized (formatter) {
            return new Double(formatter.format(number));
        }
    }

    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
/*
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v ->
                futures.stream().
                        map(future -> future.join()).
                        collect(Collectors.<T>toList())
        );
*/
        return CompletableFuture.supplyAsync(() -> futures.stream().
                map(future -> future.join()).
                collect(Collectors.<T>toList()));
    }
}
