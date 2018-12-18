/*
 * Copyright © 2014-2019 the original author or authors.
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
package net.javacrumbs.futureconverter.common.test.spring;

import net.javacrumbs.futureconverter.common.test.AbstractConverterTest;
import net.javacrumbs.futureconverter.common.test.OriginalFutureTestHelper;
import net.javacrumbs.futureconverter.common.test.common.CommonOriginalFutureTestHelper;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class SpringOriginalFutureTestHelper extends CommonOriginalFutureTestHelper implements OriginalFutureTestHelper<ListenableFuture<String>> {

    private final AsyncListenableTaskExecutor executor = new TaskExecutorAdapter(Executors.newCachedThreadPool());

    @Override
    public ListenableFuture<String> createExceptionalFuture(final Exception exception) {
        return executor.submitListenable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                throw exception;
            }
        });
    }

    @Override
    public ListenableFuture<String> createFinishedFuture() {
        SettableListenableFuture<String> future = new SettableListenableFuture<>();
        future.set(AbstractConverterTest.VALUE);
        return future;
    }

    @Override
    public ListenableFuture<String> createRunningFuture() {
        return executor.submitListenable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                waitForSignal();
                return AbstractConverterTest.VALUE;
            }
        });
    }
}
