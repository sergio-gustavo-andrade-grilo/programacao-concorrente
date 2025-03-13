import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Sum {

    private static Thread[] threads;
    private static long[] sums;
    private static Semaphore multiplex;
    private static Map<Long, List<String>> map;

    private static Long totalSum;
    private static Semaphore mutex;

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

        int num_files = args.length;

        totalSum = 0l;
        threads = new Thread[num_files];
        sums = new long[num_files];
        map = new HashMap<>();
        mutex = new Semaphore(1);

        multiplex = new Semaphore(num_files / 2);
        if (num_files == 1) {
            multiplex.release(); // gambiarra para não travar
        }

        for (int i = 0; i < num_files; i++) {
            Thread thread = new Thread(new SumThread(args[i], i, sums, multiplex, totalSum, mutex));
            threads[i] = thread;

            multiplex.acquire();
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (int i = 0; i < num_files; i++) {
            totalSum += sums[i];
        }

        System.out.println(totalSum);

        for (int i = 0; i < num_files; i++) {
            List<String> tempList;

            if (map.get(sums[i]) == null) {
                tempList = new ArrayList<>();
            } else {
                tempList = map.get(sums[i]);
            }

            tempList.add(args[i]);
            map.put(sums[i], tempList);
        }

        for (long sum : map.keySet()) {
            String str = "";
            List<String> tempList = map.get(sum);

            if (tempList.size() > 1) {
                str = str + sum + " ";

                for (String path : tempList) {
                    str = str + path + " ";
                }

                str = str.trim();
                System.out.println(str);

            }
        }
    }

    static class SumThread implements Runnable {
        String path;
        int id;
        long[] sums;
        Semaphore multiplex;
        Long totalSum;
        Semaphore mutex;

        public SumThread(String path, int id, long[] sums, Semaphore multiplex, Long totalSum, Semaphore mutex) {
            this.path = path;
            this.id = id;
            this.sums = sums;
            this.multiplex = multiplex;
            this.totalSum = totalSum;
            this.mutex = mutex;
        }

        public int sum(FileInputStream fis) throws IOException {

            int byteRead;
            int sum = 0;

            while ((byteRead = fis.read()) != -1) {
                sum += byteRead;
            }

            try {
                mutex.acquire();
                totalSum += sum;
                System.out.println(path + " : " + sum);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mutex.release();
            }

            return sum;
        }

        public void sum() throws IOException {

            Path filePath = Paths.get(this.path);
            if (Files.isRegularFile(filePath)) {
                FileInputStream fis = new FileInputStream(filePath.toString());
                sums[id] = sum(fis);
            } else {
                throw new RuntimeException("Non-regular file: " + path);
            }
        }

        @Override
        public void run() {
            try {
                this.sum();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            } finally {
                multiplex.release();
            }
        }
    }
}
