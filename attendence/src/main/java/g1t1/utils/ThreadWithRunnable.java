package g1t1.utils;

public class ThreadWithRunnable<T extends Runnable> extends Thread {
    private final T runnable;

    public ThreadWithRunnable(T runnable) {
        super(runnable);
        this.runnable = runnable;
    }

    public T getRunnable() {
        return this.runnable;
    }
}
