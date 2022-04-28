package com.example.demo;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Random;

@RestController
public class DemoController {

    private final MeterRegistry meterRegistry;

    private Counter counter;

    public DemoController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.initCounters();
    }

    private void initCounters() {
        counter = Counter.builder("demo.counter")
                .tag("release", "1.2.3")
                .description("The number of requests")
                .register(meterRegistry);
    }

    @GetMapping("/hello")
    public Map<String, Object> getSomething() {
        counter.increment();
        return Map.of("message", "Hello world");
    }

    @GetMapping("/slow")
    public Map<String, Object> getSomethingSlow() {
        return Timer.builder("demo.invoice")
                .description("Processing time for invoice requests")
                .register(meterRegistry)
                .record(() -> doSomethingSlow());
    }

    @GetMapping("/speed")
    public Map<String, Object> getGauge() {
        return Timer.builder("demo.invoice")
                .description("Processing time for invoice requests")
                .tags("invoice", "yes")
                .register(meterRegistry)
                .record(() -> doSomethingSlow());
    }

    @GetMapping("/doc")
    public Map<String, Object> getDocument() {
        long length = 100000 + new Random().nextInt(10000);
        DistributionSummary.builder("demo.doc.size")
                .description("Size of output document produced by invoice service")
                .baseUnit("bytes")
                .register(meterRegistry)
                .record(length);
        return Map.of("message", "Document created");
    }

    private Map<String, Object> doSomethingSlow() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Map.of("message", "Sorry for the delay");
    }

}
