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
package net.javacrumbs.futureconverter.common.test.common;

import java.util.concurrent.CountDownLatch;

public class CommonOriginalFutureTestHelper {
    private final CountDownLatch waitLatch = new CountDownLatch(1);

    protected void waitForSignal() throws InterruptedException {
        waitLatch.await();
    }

    public void finishRunningFuture() {
        waitLatch.countDown();
    }
}
