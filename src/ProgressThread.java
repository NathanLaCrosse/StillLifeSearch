public class ProgressThread extends Thread {
    @Override
    public void run() {
        try {
            sleep(100);
        } catch (InterruptedException e) {
            return;
        }
        if(App.complete) {return;}
        System.out.println("Current Progress = " + (100.0 * App.currentIndex / App.permutations) + "%");
        ProgressThread pt = new ProgressThread();
        pt.start();
    }
}
