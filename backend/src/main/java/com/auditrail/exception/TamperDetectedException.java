package com.auditrail.exception;

public class TamperDetectedException extends RuntimeException {

    private final long sequenceNumber;
    private final String expectedHash;
    private final String actualHash;

    public TamperDetectedException(String message, long sequenceNumber, String expectedHash, String actualHash) {
        super(message);
        this.sequenceNumber = sequenceNumber;
        this.expectedHash = expectedHash;
        this.actualHash = actualHash;
    }

    public long getSequenceNumber() { return sequenceNumber; }
    public String getExpectedHash() { return expectedHash; }
    public String getActualHash() { return actualHash; }
}
