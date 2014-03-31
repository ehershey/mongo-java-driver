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

import org.mongodb.Codec;
import org.mongodb.CommandResult;
import org.mongodb.Document;
import org.mongodb.MongoCommandFailureException;
import org.mongodb.MongoNamespace;
import org.mongodb.codecs.DocumentCodec;
import org.mongodb.protocol.CommandProtocol;
import org.mongodb.session.Session;

import static org.mongodb.operation.OperationHelper.executeProtocol;

public class DropIndexOperation implements Operation<CommandResult> {
    private final Codec<Document> commandCodec = new DocumentCodec();
    private final MongoNamespace namespace;
    private final Document dropIndexesCommand;

    public DropIndexOperation(final MongoNamespace namespace, final String indexName) {
        this.namespace = namespace;
        this.dropIndexesCommand = new Document("dropIndexes", namespace.getCollectionName()).append("index", indexName);
    }

    @Override
    public CommandResult execute(final Session session) {
        try {
            return executeProtocol(new CommandProtocol(namespace.getDatabaseName(), dropIndexesCommand, commandCodec, commandCodec),
                                   session);
        } catch (MongoCommandFailureException e) {
            return ignoreNamespaceNotFoundExceptions(e);
        }
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }

    //TODO: work out a way to reuse this
    private CommandResult ignoreNamespaceNotFoundExceptions(final MongoCommandFailureException e) {
        if (!e.getCommandResult().getErrorMessage().contains("ns not found")) {
            throw e;
        }
        return e.getCommandResult();
    }

}
