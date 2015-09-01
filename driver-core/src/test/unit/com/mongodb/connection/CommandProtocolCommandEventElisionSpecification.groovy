/*
 * Copyright 2015 MongoDB, Inc.
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

package com.mongodb.connection

import com.mongodb.ServerAddress
import com.mongodb.event.CommandStartedEvent
import com.mongodb.event.CommandSucceededEvent
import com.mongodb.internal.validator.NoOpFieldNameValidator
import org.bson.BsonDocument
import org.bson.BsonInt32
import org.bson.codecs.DocumentCodec
import spock.lang.Specification

import static com.mongodb.connection.MessageHelper.buildSuccessfulReply

// Testing security-senstive command elision with a unit test to avoid having to actually execute the commands without error, which can
// get complicated
class CommandProtocolCommandEventElisionSpecification extends Specification {
    private TestInternalConnection connection;

    def setup() {
        connection = new TestInternalConnection(new ServerId(new ClusterId(), new ServerAddress('localhost', 27017)));
    }

    def 'should elide successful security-sensitive commands'() {
        given:
        def securitySensitiveCommandName = securitySensitiveCommand.keySet().iterator().next()
        def protocol = new CommandProtocol('admin', securitySensitiveCommand, new NoOpFieldNameValidator(), new DocumentCodec())
        def commandListener = new TestCommandListener()
        protocol.commandListener = commandListener
        connection.enqueueReply(buildSuccessfulReply('{ok: 1}'));

        when:
        protocol.execute(connection)

        then:
        commandListener.eventsWereDelivered([new CommandStartedEvent(1, connection.getDescription(), 'admin', securitySensitiveCommandName,
                                                                     new BsonDocument()),
                                             new CommandSucceededEvent(1, connection.getDescription(), securitySensitiveCommandName,
                                                                       new BsonDocument(), 1)])
        where:
        securitySensitiveCommand << [new BsonDocument('authenticate', new BsonInt32(1)),
                                     new BsonDocument('saslStart', new BsonInt32(1)),
                                     new BsonDocument('saslContinue', new BsonInt32(1)),
                                     new BsonDocument('getnonce', new BsonInt32(1)),
                                     new BsonDocument('createUser', new BsonInt32(1)),
                                     new BsonDocument('updateUser', new BsonInt32(1)),
                                     new BsonDocument('copydbgetnonce', new BsonInt32(1)),
                                     new BsonDocument('copydbsaslstart', new BsonInt32(1)),
                                     new BsonDocument('copydb', new BsonInt32(1))
        ]
    }

    def 'should not elide successful normal commands'() {
        given:
        def commandName = command.keySet().iterator().next()
        def protocol = new CommandProtocol('admin', command, new NoOpFieldNameValidator(), new DocumentCodec())
        def commandListener = new TestCommandListener()
        protocol.commandListener = commandListener
        connection.enqueueReply(buildSuccessfulReply('{ok: 1}'));

        when:
        protocol.execute(connection)

        then:
        commandListener.eventsWereDelivered([new CommandStartedEvent(1, connection.getDescription(), 'admin', commandName,
                                                                     command),
                                             new CommandSucceededEvent(1, connection.getDescription(), commandName,
                                                                       new BsonDocument('ok', new BsonInt32(1)), 1)])
        where:
        command << [new BsonDocument('isMaster', new BsonInt32(1)),
                    new BsonDocument('ping', new BsonInt32(1))
        ]
    }
}
