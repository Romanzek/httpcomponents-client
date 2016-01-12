/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.http.client.methods;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.hc.core5.concurrent.Cancellable;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.AbstractHttpMessage;

public abstract class AbstractExecutionAwareRequest extends AbstractHttpMessage implements HttpExecutionAware, HttpRequest {

    private final AtomicBoolean aborted;
    private final AtomicReference<Cancellable> cancellableRef;

    protected AbstractExecutionAwareRequest() {
        super();
        this.aborted = new AtomicBoolean(false);
        this.cancellableRef = new AtomicReference<>(null);
    }

    public void abort() {
        if (this.aborted.compareAndSet(false, true)) {
            final Cancellable cancellable = this.cancellableRef.getAndSet(null);
            if (cancellable != null) {
                cancellable.cancel();
            }
        }
    }

    @Override
    public boolean isAborted() {
        return this.aborted.get();
    }

    /**
     * @since 4.2
     */
    @Override
    public void setCancellable(final Cancellable cancellable) {
        if (!this.aborted.get()) {
            this.cancellableRef.set(cancellable);
        }
    }

    /**
     * @since 4.2
     */
    public void completed() {
        this.cancellableRef.set(null);
    }

    /**
     * Resets internal state of the request making it reusable.
     *
     * @since 4.2
     */
    public void reset() {
        final Cancellable cancellable = this.cancellableRef.getAndSet(null);
        if (cancellable != null) {
            cancellable.cancel();
        }
        this.aborted.set(false);
    }

}
