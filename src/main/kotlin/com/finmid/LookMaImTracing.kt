package com.finmid

import io.micrometer.core.instrument.kotlin.asContextElement
import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import io.micrometer.tracing.handler.DefaultTracingObservationHandler
import io.micrometer.tracing.otel.bridge.OtelBaggageManager
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext
import io.micrometer.tracing.otel.bridge.OtelTracer
import io.micrometer.tracing.otel.bridge.Slf4JBaggageEventListener
import io.micrometer.tracing.otel.bridge.Slf4JEventListener
import io.opentelemetry.context.Context
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.extension.trace.propagation.B3Propagator
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

fun micrometerRawTracing() {
    val log = LoggerFactory.getLogger("main")

    val tracer = otelTracer()

    val newSpan = tracer.nextSpan().name("testing-span")
    log.info("outside span")
    tracer.withSpan(newSpan.start()).use {
        log.info("inside span")

        runBlocking {
            CoroutineScope(Dispatchers.IO + Context.current().asContextElement()).launch {
                newSpan.tag("test", "42")
                log.info("after tag")
            }.join()
        }

        newSpan.event("something happened")
    }
    newSpan.end()
    log.info("after span")
}

fun micrometerObservationsTracing() {
    val log = LoggerFactory.getLogger("main")
    val registry = ObservationRegistry.create()
    val tracer = otelTracer()

    registry.observationConfig().observationHandler(
        DefaultTracingObservationHandler(tracer)
    )

    log.info("outside span")
    Observation.start("testing-span", registry).observe {
        log.info("inside span")

        runBlocking {
            CoroutineScope(Dispatchers.IO + registry.asContextElement()).launch {
                log.info("inside coroutine")
            }.join()
        }
    }
    log.info("after span")
}

private fun otelTracer(): OtelTracer {
    val sdkTracerProvider = SdkTracerProvider.builder()
        .setSampler(alwaysOn())
        .build()

    val openTelemetrySdk = OpenTelemetrySdk.builder()
        .setTracerProvider(sdkTracerProvider)
        .setPropagators(ContextPropagators.create(B3Propagator.injectingSingleHeader()))
        .build()
    val otelTracer = openTelemetrySdk.tracerProvider["io.micrometer.micrometer-tracing"]

    val otelCurrentTraceContext = OtelCurrentTraceContext()
    val slf4JEventListener = Slf4JEventListener()
    val slf4JBaggageEventListener = Slf4JBaggageEventListener(emptyList())
    val tracer = OtelTracer(otelTracer, otelCurrentTraceContext, { event: Any ->
        slf4JEventListener.onEvent(event)
        slf4JBaggageEventListener.onEvent(event)
    }, OtelBaggageManager(otelCurrentTraceContext, emptyList<String>(), emptyList<String>()))
    return tracer
}

fun main() {
    println(">>>>> This is raw micrometer tracing")
    micrometerRawTracing()

    println("\n\n>>>>> This is micrometer observations")
    micrometerObservationsTracing()
}