package g1t1.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class EventEmitter<EventType> {
    private final ConcurrentHashMap<Class<? extends EventType>, CopyOnWriteArrayList<Consumer<EventType>>> listeners =
            new ConcurrentHashMap<>();

    public <T extends EventType> void subscribe(Class<T> eventClass, Consumer<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                .add((Consumer<EventType>) listener);
    }

    public <T extends EventType> void emit(T eventData) {
        Class<? extends EventType> eventClass = (Class<? extends EventType>) eventData.getClass();
        CopyOnWriteArrayList<Consumer<EventType>> eventListeners = listeners.get(eventClass);
        if (eventListeners != null) {
            for (Consumer<EventType> listener : eventListeners) {
                listener.accept(eventData);
            }
        }
    }

    // Added unsubscribe for completeness
    public <T extends EventType> boolean unsubscribe(Class<T> eventClass, Consumer<T> listener) {
        CopyOnWriteArrayList<Consumer<EventType>> eventListeners = listeners.get(eventClass);
        return eventListeners != null && eventListeners.remove(listener);
    }
}