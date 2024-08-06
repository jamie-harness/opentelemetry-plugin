package com.dynatrace.agent.introspection.span.opentelemetry;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

public final class DynatraceCompositeSpan implements Span {
   @Nonnull
   private final Span otelSpan;
   @Nonnull
   private final Span dtSpan;

   public DynatraceCompositeSpan(@Nonnull Span otelSpan, @Nonnull Span dtSpan) {
      this.otelSpan = otelSpan;
      this.dtSpan = dtSpan;
   }

   public <T> Span setAttribute(AttributeKey<T> key, T value) {
      this.otelSpan.setAttribute(key, value);
      this.dtSpan.setAttribute(key, value);
      return this;
   }

   public Span addEvent(String name, Attributes attributes) {
      this.otelSpan.addEvent(name, attributes);
      this.dtSpan.addEvent(name, attributes);
      return this;
   }

   public Span addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit) {
      this.otelSpan.addEvent(name, attributes, timestamp, unit);
      this.dtSpan.addEvent(name, attributes, timestamp, unit);
      return this;
   }

   public Span setStatus(StatusCode statusCode, String description) {
      this.otelSpan.setStatus(statusCode, description);
      this.dtSpan.setStatus(statusCode, description);
      return this;
   }

   public Span recordException(Throwable exception, Attributes additionalAttributes) {
      this.otelSpan.recordException(exception, additionalAttributes);
      this.dtSpan.recordException(exception, additionalAttributes);
      return this;
   }

   public Span updateName(String name) {
      this.otelSpan.updateName(name);
      this.dtSpan.updateName(name);
      return this;
   }

   public void end() {
      this.otelSpan.end();
      this.dtSpan.end();
   }

   public void end(long timestamp, TimeUnit unit) {
      this.otelSpan.end(timestamp, unit);
      this.dtSpan.end(timestamp, unit);
   }

   public SpanContext getSpanContext() {
      return this.otelSpan.getSpanContext();
   }

   public boolean isRecording() {
      return this.otelSpan.isRecording();
   }

   public Span getOtelSpan() {
      return this.otelSpan;
   }

   public Span getDynatraceSpan() {
      return this.dtSpan;
   }
}
