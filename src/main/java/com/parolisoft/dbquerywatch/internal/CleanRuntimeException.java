package com.parolisoft.dbquerywatch.internal;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CleanRuntimeException extends RuntimeException {
    // Having my own message in order to prevent the error message printed TWICE on console.
    protected final String detailMessage;

    @Override
    public String getLocalizedMessage() {
        return detailMessage;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // Hack to suppress the StackTrace
        return this;
    }
}
