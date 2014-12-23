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

class DefaultListenable<T> implements Listenable<T> {
    private final ListenableCallbackRegistry<T> callbackRegistry = new ListenableCallbackRegistry<>();

    @Override
    public void addCallbacks(Consumer<? super T> onSuccess, Consumer<Throwable> onFailure) {
        callbackRegistry.addCallbacks(onSuccess, onFailure, SimpleCompletionStage.SAME_THREAD_EXECUTOR);
    }


    public void success(T result) {
        callbackRegistry.success(result);
    }

    public void failure(Throwable e) {
        callbackRegistry.failure(e);
    }
}
