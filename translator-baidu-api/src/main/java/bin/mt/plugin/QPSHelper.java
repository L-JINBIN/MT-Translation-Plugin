package bin.mt.plugin;

public class QPSHelper {
    private static long lastTime;
    private static int count;
    private static int qps = 1;

    public static void main(String[] args) {
        setQPS(1);
        System.out.println("QPS=1");
        for (int i = 0; i < 5; i++) {
            waitIfNecessary();
            System.out.println(i + " lastTime=" + lastTime + ", count=" + count);
        }
        System.out.println("QPS=2");
        setQPS(2);
        for (int i = 0; i < 10; i++) {
            waitIfNecessary();
            System.out.println(i);
        }
        System.out.println("QPS=10");
        setQPS(10);
        for (int i = 0; i < 50; i++) {
            waitIfNecessary();
            System.out.println(i);
        }
    }

    public static void setQPS(int qps) {
        if (qps <= 0) {
            qps = 1;
        }
        QPSHelper.qps = qps;
        count = 0;
    }

    public synchronized static void onFail() {
        count--;
    }

    public synchronized static void waitIfNecessary() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime > 1000) {
            count = 1;
            lastTime = currentTime;
            return;
        }
        if (count >= qps) {
            try {
                Thread.sleep(lastTime + 1000 - currentTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count = 1;
            lastTime = System.currentTimeMillis();
            return;
        }
        count++;
    }
}
