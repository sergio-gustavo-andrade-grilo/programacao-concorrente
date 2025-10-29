import java.util.concurrent.Semaphore;

class Producer {
    private final Buffer buffer;
    private final int maxItems;
    private final int sleepTime;
    private final int id;
    private final Semaphore mutex;
    private final Semaphore produceSemaphore;
    private final Semaphore consumerSemaphore;
    
    public Producer(int id, Buffer buffer, int maxItems, int sleepTime, Semaphore mutex, Semaphore consumerSemaphore, Semaphore producerSemaphore) {
        this.id = id;
        this.buffer = buffer;
        this.maxItems = maxItems;
        this.sleepTime = sleepTime;
        this.mutex = mutex;
        this.produceSemaphore = producerSemaphore;
        this.consumerSemaphore = consumerSemaphore;
    }
    
    public void produce() {
        for (int i = 0; i < maxItems; i++) {
            try {
                Thread.sleep(sleepTime);
                int item = (int) (Math.random() * 100);
                System.out.println("Producer " + id + " produced item " + item);
                
                produceSemaphore.acquire();
                mutex.acquire();
                buffer.put(item);
                mutex.release();
                consumerSemaphore.release();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
