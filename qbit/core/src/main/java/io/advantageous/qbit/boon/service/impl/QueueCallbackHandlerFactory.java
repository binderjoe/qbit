/*
 * Copyright (c) 2015. Rick Hightower, Geoff Chandler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  		http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * QBit - The Microservice lib for Java : JSON, WebSocket, REST. Be The Web!
 */

package io.advantageous.qbit.boon.service.impl;

import io.advantageous.boon.core.reflection.ClassMeta;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.queue.QueueCallBackHandler;
import io.advantageous.qbit.boon.service.impl.queuecallbacks.AnnotationDrivenQueueCallbackHandler;

/**
 * created by rhightower on 2/10/15.
 */
public class QueueCallbackHandlerFactory {


    public static final String QUEUE_CALLBACK_ANNOTATION_NAME = "QueueCallback";

    static QueueCallBackHandler createQueueCallbackHandler(Object service) {

        if (service instanceof QueueCallBackHandler) {
            return (QueueCallBackHandler) service;
        } else {
            if (hasQueueCallbackAnnotations(service)) {
                return new AnnotationDrivenQueueCallbackHandler(service);
            } else {
                //return new DynamicQueueCallbackHandler(service);
                return new QueueCallBackHandler() {

                    @Override
                    public void queueLimit() {

                    }

                    @Override
                    public void queueEmpty() {

                    }
                };
            }
        }

    }

    private static boolean hasQueueCallbackAnnotations(Object service) {
        @SuppressWarnings("unchecked") ClassMeta<Class<?>> classMeta = (ClassMeta<Class<?>>) ClassMeta.classMeta(service.getClass());
        final Iterable<MethodAccess> methods = classMeta.methods();

        for (MethodAccess methodAccess : methods) {
            if (methodAccess.hasAnnotation(QUEUE_CALLBACK_ANNOTATION_NAME)) {
                return true;
            }

        }
        return false;
    }
}
