package com.kegelapps.palace;

import com.google.protobuf.Message;

/**
 * Created by Ryan on 1/25/2016.
 */
public interface Serializer {
    void ReadBuffer(Message msg);
    Message WriteBuffer();
}
