import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    protected static BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(20);

    public static void main(String[] args) {  
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.execute(new Producer(200l));
        executorService.execute(new Consumer(200l));

        while (true) {} // block indefinitely
    }

    private static class Producer implements Runnable {
        private long delay;

        public Producer(long delay) {
            this.delay = delay;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    int num = new Random().nextInt(10) + 1;
                    Thread.sleep(this.delay);
                    queue.put(num);
    
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
            while (true) {
                try {
                    Thread.sleep(this.delay);
                    Integer num = queue.take();
                    System.out.println(num);
    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    } 
}
