package g1t1.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class EventEmitter<EventType> {
    private final Map<Class<? extends EventType>, ArrayList<Consumer<EventType>>> listeners = new HashMap<>();

    public <T extends EventType> void subscribe(Class<T> eventClass, Consumer<T> listener) {
        listeners.computeIfAbsent(eventClass, k -> new ArrayList<>())
                .add((Consumer<EventType>) listener);
    }

    public <T extends EventType> void emit(T eventData) {
        Class<? extends EventType> eventClass = (Class<? extends EventType>) eventData.getClass();
        if (listeners.containsKey(eventClass)) {
            for (Consumer<EventType> listener : listeners.get(eventClass)) {
                listener.accept(eventData);
            }
        }
    }
}
