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

import java.util.function.Consumer;

public class DefaultListenable<T> implements Listenable<T> {
    private final CallbackRegistry<T> successCallbackRegistry = new CallbackRegistry<>();
    private final CallbackRegistry<Throwable> failureCallbackRegistry = new CallbackRegistry<>();

    @Override
    public void addCallbacks(Consumer<? super T> onSuccess, Consumer<Throwable> onFailure) {
        addSuccessCallback(onSuccess);
        addFailureCallback(onFailure);
    }

    void addFailureCallback(Consumer<Throwable> onFailure) {
        failureCallbackRegistry.addCallback(onFailure);
    }

    void addSuccessCallback(Consumer<? super T> onSuccess) {
        successCallbackRegistry.addCallback(onSuccess);
    }

    public void success(T result) {
        successCallbackRegistry.done(result);
    }

    public void failure(Throwable e) {
        failureCallbackRegistry.done(e);
    }
}
