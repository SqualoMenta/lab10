package it.unibo.mvc;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {
    private static final String PATH = "src/main/resources/config.yml";

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *              the views to attach
     */
    public DrawNumberApp(final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        final Map<String, Integer> map = new HashMap<>();
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view : views) {
            view.setObserver(this);
            view.start();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(PATH))) {
            reader.lines().forEach(e -> {
                String[] arr = e.split(": ");
                map.put(arr[0], Integer.parseInt(arr[1]));
            });
        } catch (Exception e) {
        }
        this.model = new DrawNumberImpl(map.getOrDefault("minimum", 0), map.getOrDefault("maximum", 0),
                map.getOrDefault("attemps", 0));
    }

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view : views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view : views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *             ignored
     * @throws FileNotFoundException
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp(new DrawNumberViewImpl(), new DrawNumberViewImpl(), new PrintStreamView(System.out),
                new PrintStreamView("output.log"));
    }

}
