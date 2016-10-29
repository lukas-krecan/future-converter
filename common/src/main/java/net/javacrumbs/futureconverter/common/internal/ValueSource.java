/**
 * Copyright 2009-2016 the original author or authors.
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
package net.javacrumbs.futureconverter.common.internal;

import java.util.function.Consumer;

/**
 * Source of values. Let's say we are converting from RxJava Single to CompletableFuture. In such case Single is
 * value source (the original object) and the library registers CompletableFuture (target object) to listen on Singles
 * events. When someone calls cancel on the CompletableFuture, we want to unsubscribe from the Single, that's why we do
 * have cancel method here.
 */
public interface ValueSource<T> {
    /**
     * Used to notify target object about changes in the original object.
     */
    void addCallbacks(Consumer<T> successCallback, Consumer<Throwable> failureCallback);

    /**
     * Cancels execution of the original object if cancel is called on the target object
     */
    boolean cancel(boolean mayInterruptIfRunning);
}
