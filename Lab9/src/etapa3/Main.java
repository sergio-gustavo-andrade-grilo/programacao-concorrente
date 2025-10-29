import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    private static final int TOTAL_AMOUNT = 10_000;
    protected static PriorityBlockingQueue<Integer> queue = new PriorityBlockingQueue<>(TOTAL_AMOUNT);

    protected static AtomicInteger numbersLeftToProduce = new AtomicInteger(TOTAL_AMOUNT);
    protected static AtomicInteger numbersLeftToConsume = new AtomicInteger(TOTAL_AMOUNT);

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.execute(new Producer(100l));
        executorService.execute(new Consumer(100l));

        executorService.shutdown();
    }

    private static class Producer implements Runnable {
        private long delay;

        public Producer(long delay) {
            this.delay = delay;
        }

        @Override
        public void run() {
            while (numbersLeftToProduce.get() > 0) {
                try {
                    int num = new Random().nextInt(10) + 1;
                    Thread.sleep(this.delay);
                    queue.put(num);
                    numbersLeftToProduce.decrementAndGet();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class Consumer implements Runnable {
        private long delay;

        public Consumer(long delay) {
            this.delay = delay;
        }

        @Override
        public void run() {
            while (numbersLeftToConsume.get() > 0) {
                try {
                    Thread.sleep(this.delay);
                    Integer num = queue.poll(600l, TimeUnit.MILLISECONDS);

                    if (num == null) {
                        numbersLeftToProduce.set(0);
                        break;
                    }

                    System.out.println(num);
                    numbersLeftToConsume.decrementAndGet();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
