package com.tonip.base;

import com.vaadin.flow.shared.Registration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Pub/sub fan-out for entity-change notifications. Listeners are dispatched on a
 * dedicated daemon thread so the publishing transaction can commit before any
 * listener queries the database.
 */
@Component
public class Broadcaster {

    private final Executor executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "broadcaster");
        t.setDaemon(true);
        return t;
    });

    private final List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();

    public synchronized Registration register(Consumer<String> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public void broadcast(String topic) {
        for (var listener : listeners) {
            executor.execute(() -> listener.accept(topic));
        }
    }
}
