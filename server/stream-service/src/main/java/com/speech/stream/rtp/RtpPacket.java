package com.speech.stream.rtp;

import lombok.Data;

@Data
public class RtpPacket {
    private byte[] payload;
    private long timestamp;
    private int sequenceNumber;
    private int ssrc;
} 