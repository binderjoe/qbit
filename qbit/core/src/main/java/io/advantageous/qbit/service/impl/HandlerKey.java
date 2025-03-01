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

package io.advantageous.qbit.service.impl;

/**
 * Maps an incoming call to a response handler.
 * This uniquely identifies a method call based on its message id and return address combo.
 * We use this as a key into the
 */
class HandlerKey {
    final String returnAddress;
    final String address;
    final long messageId;
    final long timestamp;


    HandlerKey(String returnAddress, String address, long messageId, long timestamp) {
        this.returnAddress = returnAddress;
        this.address = address;
        this.messageId = messageId;
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HandlerKey)) return false;

        HandlerKey that = (HandlerKey) o;

        if (messageId != that.messageId) return false;
        if (returnAddress != null ? !returnAddress.equals(that.returnAddress) : that.returnAddress != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = returnAddress != null ? returnAddress.hashCode() : 0;
        result = 31 * result + (int) (messageId ^ (messageId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "HandlerKey{" +
                "returnAddress='" + returnAddress + '\'' +
                ", address='" + address + '\'' +
                ", messageId=" + messageId +
                ", timestamp=" + timestamp +
                '}';
    }
}
