/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mongodb.connection.netty

import io.netty.buffer.ByteBufAllocator
import org.bson.ByteBufNIO
import spock.lang.Specification

import java.nio.ByteBuffer

class ByteBufSpecification extends Specification {
    def 'should set position and limit correctly'() {
        expect:
        buf.capacity() == 16
        buf.position() == 0
        buf.limit() == 16

        when:
        buf.put(new byte[10], 0, 10)

        then:
        buf.position() == 10
        buf.limit() == 16

        when:
        buf.flip()

        then:
        buf.position() == 0
        buf.limit() == 10

        when:
        buf.position(3)

        then:
        buf.position() == 3
        buf.limit() == 10

        when:
        buf.limit(7)

        then:
        buf.position() == 3
        buf.limit() == 7

        when:
        buf.get(new byte[4])

        then:
        buf.position() == 7
        buf.limit() == 7

        where:
        buf << [new ByteBufNIO(ByteBuffer.allocate(16)),
                new NettyByteBuf(ByteBufAllocator.DEFAULT.buffer(16))
        ]
    }
}
