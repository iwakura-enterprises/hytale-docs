package enterprises.iwakura.docs.listener;

import com.hypixel.hytale.event.IBaseEvent;

/**
 * Base interface for global event listeners.
 *
 * @param <T> the type of event to listen for
 */
public interface BaseGlobalListener<T extends IBaseEvent<?>> {

    Class<T> getEventClass();

    void onEvent(T event);

}
