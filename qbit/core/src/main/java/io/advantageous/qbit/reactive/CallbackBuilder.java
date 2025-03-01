package io.advantageous.qbit.reactive;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * You need this is you want to do error handling (Exception) from a callback.
 * Callback Builder
 * created by rhightower on 3/23/15.
 */
@SuppressWarnings("UnusedReturnValue")
public class CallbackBuilder {

    private Reactor reactor;
    private Callback callback;
    private Runnable onTimeout;
    private long timeoutDuration = -1;
    private TimeUnit timeoutTimeUnit = TimeUnit.SECONDS;
    private Consumer<Throwable> onError;

    /**
     * @param reactor reactor
     */
    private CallbackBuilder(final Reactor reactor) {
        this.reactor = reactor;
    }

    /**
     *
     */
    private  CallbackBuilder() {
    }

    /**
     * Deprecated.  use newBuilderWithReactor(Reactor r) instead
     *
     * @param reactor reactor
     * @return CallbackBuilder
     */
    @Deprecated
    public static CallbackBuilder callbackBuilder(final Reactor reactor) {
        return new CallbackBuilder(reactor);
    }


    /**
     *
     * Creating callback builder.
     * @param reactor reactor
     * @return CallbackBuilder
     */
    public static CallbackBuilder newCallbackBuilderWithReactor(final Reactor reactor) {
        return new CallbackBuilder(reactor);
    }

    /**
     * Deprecated.  use newBuilder() instead
     *
     * @return CallbackBuilder
     */
    @Deprecated
    public static CallbackBuilder callbackBuilder() {
        return new CallbackBuilder();
    }


    /**
     *
     * @return CallbackBuilder
     */
    public static CallbackBuilder newCallbackBuilder() {
        return new CallbackBuilder();
    }

    /**
     * This is Deprecated. this will become private. Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return Reactor
     */
    @Deprecated
    public Reactor getReactor() {
        return reactor;
    }

    /**
     * This is Deprecated.  this will become private.  Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return callback
     */
    @Deprecated
    public <T> Callback<T> getCallback() {
        //noinspection unchecked
        return callback;
    }

    /**
     * Builder method to add a callback handler.  This is depricated.  Use withCallback instead.
     *
     * @param callback callback
     * @return this
     */
    @Deprecated
    public CallbackBuilder setCallback(final Callback callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Builder method to add a callback handler.  This is depricated.  Use withCallback instead.
     *
     * @param returnType returnType
     * @param callback callback
     * @param <T> T
     * @return this
     */
    @Deprecated
    public <T> CallbackBuilder setCallback(final Class<T> returnType, final Callback<T> callback) {
        return withCallback(returnType, callback);
    }

    /**
     * Builder method to set the callback handler.
     *
     * @param returnType returnType
     * @param callback callback
     * @param <T> T
     * @return this
     */
    public <T> CallbackBuilder withCallback(final Class<T> returnType,
                                            final Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Builder method to set the callback handler.
     *
     * @param callback callback
     * @param <T> T
     * @return callback
     */
    public <T> CallbackBuilder withCallback(final Callback<T> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Builder method to set callback handler that takes a list
     * @param componentClass  componentClass
     * @param callback callback
     * @param <T> T
     * @return this
     */
    public <T> CallbackBuilder withListCallback(final Class<T> componentClass,
                                                       final Callback<List<T>> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a set
     * @param componentClass  componentClass
     * @param callback callback
     * @param <T> T
     * @return this
     */
    public <T> CallbackBuilder withSetCallback(final Class<T> componentClass,
                                                     final Callback<Set<T>> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a collection
     * @param componentClass  componentClass
     * @param callback callback
     * @param <T> T
     * @return this
     */
    public <T> CallbackBuilder withCollectionCallback(final Class<T> componentClass,
                                                            final Callback<Collection<T>> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a map
     * @param keyClass  keyClass
     * @param valueClass  valueClass
     * @param callback callback
     * @param <K> key type
     * @param <V> value type
     * @return this
     */
    public <K, V> CallbackBuilder withMapCallback(final Class<K> keyClass,
                                                        final Class<V> valueClass,
                                                        final Callback<Map<K, V>> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a boolean
     * @param callback callback
     * @return this
     */
    public CallbackBuilder withBooleanCallback(final Callback<Boolean> callback) {
        this.callback = callback;
        return this;
    }

    /**
     * Builder method to set callback handler that takes a integer
     * @param callback callback
     * @return this
     */
    public CallbackBuilder withIntCallback(final Callback<Integer> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a long
     * @param callback callback
     * @return this
     */
    public CallbackBuilder withLongCallback(final Callback<Long> callback) {
        this.callback = callback;
        return this;
    }


    /**
     * Builder method to set callback handler that takes a string
     * @param callback callback
     * @return this
     */
    public CallbackBuilder withStringCallback(final Callback<String> callback) {
        this.callback = callback;
        return this;
    }




    /**
     * This is Deprecated.  this will become private.  Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return runnable
     */
    @Deprecated
    public Runnable getOnTimeout() {
        return onTimeout;
    }

    /**
     * Deprecated.  use withTimeoutHandler instead.
     *
     * @param onTimeout onTimeout
     * @return this
     */
    @Deprecated
    public CallbackBuilder setOnTimeout(final Runnable onTimeout) {
        this.onTimeout = onTimeout;
        return this;
    }

    /**
     * Add a timeout handler to the callback.
     *
     * @param timeoutHandler timeoutHandler
     * @return this
     */
    public CallbackBuilder withTimeoutHandler(final Runnable timeoutHandler) {
        this.onTimeout = timeoutHandler;
        return this;
    }

    /**
     * This is Deprecated.  this will become private.  Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return timeout duration
     */
    @Deprecated
    public long getTimeoutDuration() {
        return timeoutDuration;
    }

    /**
     * Deprecated. use withTimeoutInstead
     * @param timeoutDuration timeoutDuration
     * @return this
     */
    @Deprecated
    public CallbackBuilder setTimeoutDuration(@SuppressWarnings("SameParameterValue") long timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }


    /**
     *
     * @param timeoutDuration timeoutDuration
     * @return this
     */
    public CallbackBuilder withTimeout(@SuppressWarnings("SameParameterValue") long timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
        return this;
    }

    /**
     * This is Deprecated.  this will become private.  Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return time unit
     */
    @Deprecated
    public TimeUnit getTimeoutTimeUnit() {
        return timeoutTimeUnit;
    }

    /**
     * Deprecated.  use withTimeoutTimeUnit instead.
     * @param timeoutTimeUnit timeoutTimeUnit
     * @return this
     */
    @Deprecated
    public CallbackBuilder setTimeoutTimeUnit(final TimeUnit timeoutTimeUnit) {
        this.timeoutTimeUnit = timeoutTimeUnit;
        return this;
    }


    /**
     * @param timeoutTimeUnit timeoutTimeUnit
     * @return this
     */
    public CallbackBuilder withTimeoutTimeUnit(final TimeUnit timeoutTimeUnit) {
        this.timeoutTimeUnit = timeoutTimeUnit;
        return this;
    }

    /**
     * This is Deprecated.  this will become private.  Builders should be only used to build in a local scope so this is
     * something that you should have just set.
     *
     * @return error handler
     */
    @Deprecated
    public Consumer<Throwable> getOnError() {
        return onError;
    }

    /**
     * Deprecated. use withErrorHandler instead.
     *
     * @return this
     */
    @Deprecated
    public CallbackBuilder setOnError(Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

    /**
     * Add an error handler to the callback.
     *
     * @param onError onerror
     * @return this
     */
    public CallbackBuilder withErrorHandler(final Consumer<Throwable> onError) {
        this.onError = onError;
        return this;
    }

    public <T> AsyncFutureCallback<T> build() {

        if (getOnError() != null || getOnTimeout() != null || timeoutDuration != -1) {

            if (timeoutDuration == -1) {
                timeoutDuration = 30;
            }

            if (reactor != null) {
                //noinspection unchecked
                return reactor.callbackWithTimeoutAndErrorHandlerAndOnTimeout(
                        (Callback<T>) getCallback(),
                        getTimeoutDuration(),
                        getTimeoutTimeUnit(),
                        getOnTimeout(),
                        getOnError());
            } else {
                return new AsyncFutureCallback<T>() {

                    @Override
                    public boolean checkTimeOut(long now) {

                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public void run() {
                        throw new IllegalStateException("You need to register a reactor to use this feature");

                    }

                    @Override
                    public boolean cancel(boolean mayInterruptIfRunning) {
                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public boolean isCancelled() {
                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public boolean isDone() {
                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public T get() {
                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public T get(long timeout, TimeUnit unit) {
                        throw new IllegalStateException("You need to register a reactor to use this feature");
                    }

                    @Override
                    public void accept(T t) {
                        getCallback().accept(t);
                    }

                    @Override
                    public void onError(final Throwable error) {

                        getOnError().accept(error);
                    }

                    @Override
                    public void onTimeout() {

                        getOnTimeout().run();
                    }
                };
            }

        }

        if (reactor != null) {
            return reactor.callback(this.getCallback());
        } else {

            final Callback callback = this.getCallback();
            return new AsyncFutureCallback<T>() {
                @Override
                public boolean checkTimeOut(long now) {
                    return false;
                }

                @Override
                public void accept(T t) {

                    callback.accept(t);
                }

                @Override
                public void onError(Throwable error) {

                    callback.onError(error);
                }

                @Override
                public void run() {

                }

                @Override
                public boolean cancel(boolean mayInterruptIfRunning) {
                    throw new IllegalStateException("You need to register a reactor to use this feature");

                }

                @Override
                public boolean isCancelled() {
                    throw new IllegalStateException("You need to register a reactor to use this feature");
                }

                @Override
                public boolean isDone() {
                    throw new IllegalStateException("You need to register a reactor to use this feature");
                }

                @Override
                public T get() {
                    throw new IllegalStateException("You need to register a reactor to use this feature");
                }

                @Override
                public T get(long timeout, TimeUnit unit) {
                    throw new IllegalStateException("You need to register a reactor to use this feature");
                }
            };
        }
    }

    public <T> AsyncFutureCallback<T> build(Class<T> returnType) {

        return build();
    }


    public <T> AsyncFutureCallback<T> build(Callback<T> callback) {

        this.withCallback(callback);

        return build();
    }


}
