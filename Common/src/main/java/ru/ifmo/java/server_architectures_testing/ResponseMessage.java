package ru.ifmo.java.server_architectures_testing;

import ru.ifmo.java.server_architectures_testing.protocol.Protocol;

import java.nio.ByteBuffer;

public class ResponseMessage {
    private final byte[] head;
    private final byte[] body;

    public ResponseMessage(Protocol.SortResponse response) {
        body = response.toByteArray();
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
