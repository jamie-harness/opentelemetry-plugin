package io.jenkins.plugins.opentelemetry;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

final class InMemorySpanExporter implements SpanExporter {
    private final Queue<SpanData> finishedSpanItems = new ConcurrentLinkedQueue();
    private boolean isStopped = false;

    public static InMemorySpanExporter create() {
        return new InMemorySpanExporter();
    }

    public List<SpanData> getFinishedSpanItems() {
        return Collections.unmodifiableList(new ArrayList(this.finishedSpanItems));
    }

    public void reset() {
        this.finishedSpanItems.clear();
    }

    public CompletableResultCode export(Collection<SpanData> spans) {
        if (this.isStopped) {
            return CompletableResultCode.ofFailure();
        } else {
            this.finishedSpanItems.addAll(spans);
            return CompletableResultCode.ofSuccess();
        }
    }

    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    public CompletableResultCode shutdown() {
        this.finishedSpanItems.clear();
        this.isStopped = true;
        return CompletableResultCode.ofSuccess();
    }

    private InMemorySpanExporter() {
    }
}

