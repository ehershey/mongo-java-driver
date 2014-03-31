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

import org.mongodb.Decoder;
import org.mongodb.Document;
import org.mongodb.Encoder;
import org.mongodb.MongoAsyncCursor;
import org.mongodb.MongoCursor;
import org.mongodb.MongoFuture;
import org.mongodb.MongoNamespace;
import org.mongodb.session.Session;

import static org.mongodb.assertions.Assertions.notNull;

public class QueryOperation<T> implements AsyncOperation<MongoAsyncCursor<T>>, Operation<MongoCursor<T>> {
    private final Find find;
    private final Encoder<Document> queryEncoder;
    private final Decoder<T> resultDecoder;
    private final MongoNamespace namespace;

    public QueryOperation(final MongoNamespace namespace, final Find find, final Encoder<Document> queryEncoder,
                          final Decoder<T> resultDecoder) {
        this.namespace = notNull("namespace", namespace);
        this.find = notNull("find", find);
        this.queryEncoder = notNull("queryEncoder", queryEncoder);
        this.resultDecoder = notNull("resultDecoder", resultDecoder);
    }

    @Override
    public MongoCursor<T> execute(final Session session) {
        return new MongoQueryCursor<T>(namespace, find, queryEncoder, resultDecoder, session);
    }

    @Override
    public boolean isQuery() {
        return true;
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }

    public MongoFuture<MongoAsyncCursor<T>> executeAsync(final Session session) {
        return new SingleResultFuture<MongoAsyncCursor<T>>(new MongoAsyncQueryCursor<T>(namespace, find, queryEncoder, resultDecoder,
                                                                                        session), null);
    }
}
