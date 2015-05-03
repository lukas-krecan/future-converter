/**
 * Copyright 2009-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.javacrumbs.futureconverter.springguava;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import net.javacrumbs.futureconverter.common.FutureWrapper;
import net.javacrumbs.futureconverter.common.spring.AbstractSpringListenableFutureWrapper;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.Executor;

/**
 * Converts between Guava {@link com.google.common.util.concurrent.ListenableFuture} and Spring 4 {@link org.springframework.util.concurrent.ListenableFuture}.
 */
public class FutureConverter {

    /**
     * Converts Guava {@link com.google.common.util.concurrent.ListenableFuture} to Spring 4 {@link org.springframework.util.concurrent.ListenableFuture}
     *
     * @param guavaListenableFuture
     * @param <T>
     * @return
     */
    public static <T> ListenableFuture<T> toSpringListenableFuture(com.google.common.util.concurrent.ListenableFuture<T> guavaListenableFuture) {
        if (guavaListenableFuture instanceof GuavaListenableWrappingSpringListenableFuture) {
            return ((GuavaListenableWrappingSpringListenableFuture<T>) guavaListenableFuture).getWrappedFuture();
        } else {
            return new SpringListenableWrappingGuavaListenableFuture<T>(guavaListenableFuture);
        }
    }

    /**
     * Converts Spring 4 {@link org.springframework.util.concurrent.ListenableFuture}
     * to Guava {@link com.google.common.util.concurrent.ListenableFuture}.
     *
     * @param springListenableFuture
     * @param <T>
     * @return
     */
    public static <T> com.google.common.util.concurrent.ListenableFuture<T> toGuavaListenableFuture(ListenableFuture<T> springListenableFuture) {
        if (springListenableFuture instanceof SpringListenableWrappingGuavaListenableFuture) {
            return ((SpringListenableWrappingGuavaListenableFuture<T>) springListenableFuture).getWrappedFuture();
        } else {
            return new GuavaListenableWrappingSpringListenableFuture<T>(springListenableFuture);
        }
    }

    /**
     * Wraps Guava ListenableFuture to Spring ListenableFuture.
     *
     * @param <T>
     */
    private static class SpringListenableWrappingGuavaListenableFuture<T> extends AbstractSpringListenableFutureWrapper<T> implements ListenableFuture<T> {
        public SpringListenableWrappingGuavaListenableFuture(com.google.common.util.concurrent.ListenableFuture<T> guavaListenableFuture) {
            super(guavaListenableFuture);
            Futures.addCallback(getWrappedFuture(), new FutureCallback<T>() {
                @Override
                public void onSuccess(T result) {
                    success(result);
                }

                @Override
                public void onFailure(Throwable t) {
                    failure(t);
                }
            }, MoreExecutors.directExecutor());
        }

        @Override
        public com.google.common.util.concurrent.ListenableFuture<T> getWrappedFuture() {
            return (com.google.common.util.concurrent.ListenableFuture<T>) super.getWrappedFuture();
        }
    }

    private static class GuavaListenableWrappingSpringListenableFuture<T> extends FutureWrapper<T> implements com.google.common.util.concurrent.ListenableFuture<T> {

        public GuavaListenableWrappingSpringListenableFuture(ListenableFuture<T> springListenableFuture) {
            super(springListenableFuture);
        }

        @Override
        public void addListener(final Runnable command, final Executor executor) {
            getWrappedFuture().addCallback(new ListenableFutureCallback<T>() {
                @Override
                public void onSuccess(T result) {
                    executor.execute(command);
                }

                @Override
                public void onFailure(Throwable t) {
                    executor.execute(command);
                }
            });
        }

        @Override
        public ListenableFuture<T> getWrappedFuture() {
            return (ListenableFuture<T>) super.getWrappedFuture();
        }
    }
}
