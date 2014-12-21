/**
 * Copyright 2009-2014 the original author or authors.
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
package net.javacrumbs.futureconverter.java8common;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;

class CallbackRegistry<T> {

    private final Queue<Consumer<? super T>> callbacks = new LinkedList<>();

    private boolean done = false;

    private T result = null;

    private final Object mutex = new Object();

    public void addCallback(Consumer<? super T> callback) {
        Objects.requireNonNull(callback, "'callback' must not be null");

        synchronized (mutex) {
            if (done) {
                callback.accept(result);
            } else {
                callbacks.add(callback);
            }
        }
    }

    public void done(T result) {
        synchronized (mutex) {
            done = true;
            this.result = result;

            while (!callbacks.isEmpty()) {
                callbacks.poll().accept(result);
            }
        }
    }
}
