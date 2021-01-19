package ru.ifmo.java.server_architectures_testing;

import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.nio.ByteBuffer;

public class RequestMessage {
    private final byte[] head;
    private final byte[] body;

    public RequestMessage(Protocol.SortRequest request) {
        body = request.toByteArray();
        int intBytesSize = 4;
        head = ByteBuffer.allocate(intBytesSize).putInt(body.length).array();
    }

    public byte[] getBody() {
        return body;
    }

    public byte[] getHead() {
        return head;
    }
}
