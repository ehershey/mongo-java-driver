/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.operation;

import org.mongodb.session.Session;

/**
 * An Operation is something that can be run against a MongoDB instance.  This includes CRUD operations and Commands.
 *
 * @param <T> the return type of the execute method
 */
public interface Operation<T> {
    /**
     * General execute which can return anything of type T
     *
     * @return T, the results of the execution
     * @param session
     */
    T execute(final Session session);

    /**
     * The operation should return true if this operation is a query, and therefore has no side effects.
     *
     * @return if this operation is a query.
     */
    boolean isQuery();

    /**
     * The operation should return true if the side-effects of N > 0 identical executions of this opereration is the same as for a single
     * execution.
     *
     * @return if it's a query.
     */
    boolean isIdempotent();
}
