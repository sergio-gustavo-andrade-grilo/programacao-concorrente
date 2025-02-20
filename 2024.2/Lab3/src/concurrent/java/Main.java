import java.util.concurrent.Semaphore;

public class Main {
    public static void main(String[] args) {
        if (args.length != 5) {
            System.out.println("Use: java Main <num_producers> <max_items_per_producer> <producing_time> <num_consumers> <consuming_time>");
            return;
        }


        Semaphore mutex = new Semaphore(1);
        Semaphore consumerSemaphore = new Semaphore(0);
        Semaphore producerSemaphore = new Semaphore(100);
        
        int numProducers = Integer.parseInt(args[0]);
        int maxItemsPerProducer = Integer.parseInt(args[1]);
        int producingTime = Integer.parseInt(args[2]);
        int numConsumers = Integer.parseInt(args[3]);
        int consumingTime = Integer.parseInt(args[4]);

        Buffer buffer = new Buffer();
        Thread[] threadsProdu = new Thread[numProducers];
        Thread[] threadsConsu = new Thread[numConsumers];
        
        for (int i = 1; i <= numProducers; i++) {
            Producer producer = new Producer(i, buffer, maxItemsPerProducer, producingTime, mutex, consumerSemaphore, producerSemaphore);
            
            ProduceThread produtorTh = new ProduceThread(producer);
            threadsProdu[i-1] = new Thread(produtorTh);
        }
        
        for (int i = 1; i <= numConsumers; i++) {
            Consumer consumer = new Consumer(i, buffer, consumingTime, mutex, consumerSemaphore, producerSemaphore);

            ConsumeThread consumeTh = new ConsumeThread(consumer);
            threadsConsu[i-1] = new Thread(consumeTh);
        }

        for(Thread producer : threadsProdu){
            producer.start();
        }

        for(Thread consumer : threadsConsu){
            consumer.start();
        }

        for(Thread producer : threadsProdu){
            try {
                producer.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for(Thread consumer : threadsConsu){
            try {
                consumer.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    static class ProduceThread implements Runnable {

        Producer producer;

        public ProduceThread(Producer produce) {
            this.producer = produce;
        }

        @Override
        public void run() {
            producer.produce();
        }
        
    } 

    static class ConsumeThread implements Runnable {

        Consumer consumer;

        public ConsumeThread(Consumer c){
            this.consumer = c;
        }

        @Override
        public void run() {
            consumer.process();
        }
        
    }
}
