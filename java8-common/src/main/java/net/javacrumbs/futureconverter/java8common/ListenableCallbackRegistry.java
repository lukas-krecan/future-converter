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

/**
 * Registry for Consumer callbacks
 *
 * <p>Inspired by {@code org.springframework.util.concurrent.ListenableFutureCallbackRegistry}
 *
 */
class ListenableCallbackRegistry<T> {

	private final Queue<Consumer<? super T>> successCallbacks = new LinkedList<>();
	private final Queue<Consumer<Throwable>> failureCallbacks = new LinkedList<>();

	private State state = State.NEW;

	private T result = null;

	private Throwable failure = null;

	private final Object mutex = new Object();


	/**
	 * Adds the given callback to this registry.
	 * @param callback the callback to add
	 */
	public void addSuccessCallback(Consumer<? super T> callback) {
		Objects.requireNonNull(callback, "'callback' must not be null");

		synchronized (mutex) {
			switch (state) {
				case NEW:
                    successCallbacks.add(callback);
					break;
				case SUCCESS:
					callback.accept(result);
					break;
				case FAILURE:
                    // do nothing
					break;
			}
		}
	}

    /**
   	 * Adds the given callback to this registry.
   	 * @param callback the callback to add
   	 */
   	public void addFailureCallback(Consumer<Throwable> callback) {
   		Objects.requireNonNull(callback, "'callback' must not be null");

   		synchronized (mutex) {
   			switch (state) {
   				case NEW:
                       failureCallbacks.add(callback);
   					break;
   				case SUCCESS:
                    // do nothing
   					break;
   				case FAILURE:
                    callback.accept(failure);
   					break;
   			}
   		}
   	}

	public void success(T result) {
		synchronized (mutex) {
			state = State.SUCCESS;
			this.result = result;

			while (!successCallbacks.isEmpty()) {
                successCallbacks.poll().accept(result);
			}
            failureCallbacks.clear();
		}
	}

	public void failure(Throwable t) {
		synchronized (mutex) {
			state = State.FAILURE;
			this.failure = t;

			while (!failureCallbacks.isEmpty()) {
                failureCallbacks.poll().accept(t);
			}
            successCallbacks.clear();
		}
	}

	private enum State {NEW, SUCCESS, FAILURE}

}
