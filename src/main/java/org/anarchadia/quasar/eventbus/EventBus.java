/*
 * Copyright (c) 2024. Vili and contributors.
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 *  file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */

package org.anarchadia.quasar.eventbus;

import java.lang.invoke.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;


public class EventBus implements IEventBus {
    private final Map<Class<? extends QuasarEvent>, CopyOnWriteArrayList<Listener>> listeners = new ConcurrentHashMap<>();

    /**
     * This method handles the registration of an object into the event bus system.
     * It iterates over all the methods of an object and filters those methods that are annotated with `QuasarSubscribe`, accepting only one parameter.
     * For each matched method, the access to that method is set (if it's not accessible), it retrieves the type of the first parameter
     * (which is expected to extend the QuasarEvent class),
     * it checks if the QuasarSubscribe annotation includes a Lambda expression, and it extracts it via the `getLambda` method,
     * and finally constructs a Listener object which includes the registered class, the method and the optional Lambda expression if exists.
     * If the event type related to the matched method is not already available in the listeners map, an empty list is created and added to it, and then this new listener is added to the list of the corresponding event.
     */
    @Override
    public void register(Object registerClass) {
        Arrays.stream(registerClass.getClass().getMethods())
                .filter(method -> method.isAnnotationPresent(QuasarSubscribe.class))
                .filter(method -> method.getParameterCount() == 1)
                .forEach(method -> {
                    if (!method.canAccess(registerClass)) method.setAccessible(true);

                    @SuppressWarnings("unchecked") Class<? extends QuasarEvent> event =
                            (Class<? extends QuasarEvent>) method.getParameterTypes()[0];

                    Consumer<QuasarEvent> lambda = null;
                    if (method.getDeclaredAnnotation(QuasarSubscribe.class).lambda())
                        lambda = getLambda(registerClass, method, event);
                    if (!listeners.containsKey(event)) listeners.put(event, new CopyOnWriteArrayList<>());

                    listeners.get(event).add(new Listener(registerClass, method, lambda));
                });
    }


    /**
     * This method handles the un-registration of an object from the event bus system.
     * It iterates over all the values (which are ArrayLists of Listeners) in the concurrent hashMap named listeners.
     * In each iteration, it invokes the removeIf method of the ArrayList,
     * where the condition for removal is whether the getListenerClass() on the listener object equals the registerClass object passed as argument.
     * When the condition is met, that particular listener is removed from the arrayList.
     * Therefore, this method effectively removes all the listeners associated with the registerClass object from the event bus system.
     */
    @Override
    public void unregister(Object registerClass) {
        listeners.values().forEach(arrayList -> arrayList.removeIf(listener -> listener.getListenerClass().equals(registerClass)));
    }


    /**
     * The post method of the EventBus class, which is used to dispatch a QuasarEvent to all registered listeners.
     * Initially, it retrieves the list of listeners for the class type of the event.
     * If the list is not null, it will iterate through each listener.
     * During iteration, if the event is cancelled, it will terminate the operation immediately.
     * Else if the listener has a lambda function, the event will be posted by invoking the accept function of the lambda with the event as an argument.
     * If the listener does not have a lambda function, the method will try to post the event by invoking the listener's method with two arguments – the listener's class and the event.
     * IllegalAccessException and InvocationTargetException are caught and handled internally.
     */
    @Override
    public void post(QuasarEvent event) {
        List<Listener> listenersList = listeners.get(event.getClass());
        if (listenersList != null) for (Listener listener : listenersList) {
            if (event.isCancelled()) return;
            if (listener.getLambda() != null)
                listener.getLambda().accept(event);
            else {
                try {
                    listener.getMethod().invoke(listener.getListenerClass(), event);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected Consumer<QuasarEvent> getLambda(Object object, Method method, Class<? extends QuasarEvent> event) {
        Consumer<QuasarEvent> eventLambda = null;
        try {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodType subscription = MethodType.methodType(void.class, event);
            MethodHandle target = lookup.findVirtual(object.getClass(), method.getName(), subscription);
            CallSite site = LambdaMetafactory.metafactory(
                    lookup,
                    "accept",
                    MethodType.methodType(Consumer.class, object.getClass()),
                    subscription.changeParameterType(0, Object.class),
                    target,
                    subscription);

            MethodHandle factory = site.getTarget();
            eventLambda = (Consumer<QuasarEvent>) factory.bindTo(object).invokeExact();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return eventLambda;
    }
}
