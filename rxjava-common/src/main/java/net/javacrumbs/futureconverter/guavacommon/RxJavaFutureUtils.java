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
package net.javacrumbs.futureconverter.guavacommon;

import net.javacrumbs.futureconverter.common.internal.CommonCallback;
import net.javacrumbs.futureconverter.common.internal.CommonListenable;
import net.javacrumbs.futureconverter.common.internal.OriginSource;
import net.javacrumbs.futureconverter.common.internal.SettableFuture;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscription;
import rx.functions.Action1;

public class RxJavaFutureUtils {

    public static <T> void waitForResults(Single<T> single, final SettableFuture<T> settableFuture) {
        final Subscription subscription = single.subscribe(
            new Action1<T>() {
                @Override
                public void call(T t) {
                    settableFuture.setResult(t);
                }
            },
            new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    settableFuture.setException(throwable);
                }
            }
        );
        settableFuture.setCancellationCallback(new Runnable() {
            @Override
            public void run() {
                subscription.unsubscribe();
            }
        });
    }

    public static <T> Single<T> createSingle(CommonListenable<T> commonListenable) {
        return new CommonListenableObservable<>(commonListenable);
    }

    private static class CommonListenableObservable<T> extends Single<T> implements OriginSource {
        private final CommonListenable<T> commonListenable;

        CommonListenableObservable(CommonListenable<T> commonListenable) {
            super(onSubscribe(commonListenable));
            this.commonListenable = commonListenable;
        }

        private static <T> OnSubscribe<T> onSubscribe(final CommonListenable<T> commonListenable) {
            return new OnSubscribe<T>() {
                @Override
                public void call(final SingleSubscriber<? super T> subscriber) {
                    commonListenable.addSuccessCallback(new CommonCallback<T>() {
                        @Override
                        public void process(T value) {
                            if (!subscriber.isUnsubscribed()) {
                                try {
                                    subscriber.onSuccess(value);
                                } catch (Throwable e) {
                                    subscriber.onError(e);
                                }
                            }
                        }
                    });

                    commonListenable.addFailureCallback(new CommonCallback<Throwable>() {
                        @Override
                        public void process(Throwable throwable) {
                            if (!subscriber.isUnsubscribed()) {
                                subscriber.onError(throwable);
                            }
                        }
                    });
                }
            };
        }

        public CommonListenable<T> getCommonListenable() {
            return commonListenable;
        }

        @Override
        public Object getOrigin() {
            return commonListenable.getOrigin();
        }
    }
}
