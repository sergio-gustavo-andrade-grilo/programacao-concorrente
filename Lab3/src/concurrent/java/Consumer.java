import java.util.concurrent.Semaphore;

class Consumer {
    private final Buffer buffer;
    private final int sleepTime;
    private final int id;
    private final Semaphore mutex;
    private final Semaphore produceSemaphore;
    private final Semaphore consumerSemaphore;

    public Consumer(int id, Buffer buffer, int sleepTime, Semaphore mutex, Semaphore consumerSemaphore,
            Semaphore producerSemaphore) {
        this.id = id;
        this.buffer = buffer;
        this.sleepTime = sleepTime;
        this.mutex = mutex;
        this.produceSemaphore = producerSemaphore;
        this.consumerSemaphore = consumerSemaphore;
    }

    public void process() {
        while (true) {
            int item = -1;
            
            try{
                consumerSemaphore.acquire();
                mutex.acquire();
                item = buffer.remove();
                mutex.release();
                produceSemaphore.release();
            } catch (Exception e) {
            }

            if (item == -1) break;
            System.out.println("Consumer " + id + " consumed item " + item);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}