package bo.gotthardt.queue.rabbitmq;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import io.dropwizard.jackson.Jackson;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static bo.gotthardt.test.assertj.DropwizardAssertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link bo.gotthardt.queue.rabbitmq.FunctionConsumer}.
 */
public class FunctionConsumerTest {
    private Channel channel = mock(Channel.class);
    private Envelope envelope = mock(Envelope.class);
    private MetricRegistry metrics = new MetricRegistry();
    private byte[] message;

    @Before
    public void setup() throws JsonProcessingException {
        when(envelope.getDeliveryTag()).thenReturn(1L);
        message = Jackson.newObjectMapper().writeValueAsBytes(new TestMsg("test", 2));
    }

    @Test
    public void shouldMapMessageToType() throws IOException {
        FunctionConsumer<TestMsg> consumer = new FunctionConsumer<>(channel, msg -> {
            assertThat(msg.getName()).isEqualTo("test");
            assertThat(msg.getCount()).isEqualTo(2);
            return null;
        }, TestMsg.class, "name", metrics);

        consumer.handleDelivery(null, envelope, null, message);
    }

    @Test
    public void shouldAcknowledgeMessage() throws IOException {
        FunctionConsumer<TestMsg> consumer = new FunctionConsumer<>(channel, msg -> null, TestMsg.class, "name", metrics);

        consumer.handleDelivery(null, envelope, null, message);

        verify(channel).basicAck(1L, false);
    }

    @Test
    public void shouldRejectMessageOnException() throws IOException {
        FunctionConsumer<TestMsg> consumer = new FunctionConsumer<>(channel, msg -> {
            throw new RuntimeException("Message processing failed on purpose.");
        }, TestMsg.class, "name", metrics);

        consumer.handleDelivery(null, envelope, null, message);

        verify(channel).basicNack(1L, false, true);
    }

    @Test
    public void shouldRecordConsumeSuccessMetrics() throws IOException {
        FunctionConsumer<TestMsg> consumer = new FunctionConsumer<>(channel, msg -> null, TestMsg.class, "name", metrics);

        consumer.handleDelivery(null, envelope, null, message);

        assertThat(metrics.meter("queue.TestMsg.name.consume.success.count").getCount()).isEqualTo(1);
        assertThat(metrics.timer("queue.TestMsg.name.consume.success.duration").getCount()).isEqualTo(1);
    }

    @Test
    public void shouldRecordConsumeFailureMetrics() throws IOException {
        FunctionConsumer<TestMsg> consumer = new FunctionConsumer<>(channel, msg -> {
            throw new RuntimeException("Message processing failed on purpose.");
        }, TestMsg.class, "name", metrics);

        consumer.handleDelivery(null, envelope, null, message);

        assertThat(metrics.meter("queue.TestMsg.name.consume.failure.count").getCount()).isEqualTo(1);
        assertThat(metrics.timer("queue.TestMsg.name.consume.failure.duration").getCount()).isEqualTo(1);
    }

    @NoArgsConstructor
    private static class TestMsg {
        @Getter
        private String name;
        @Getter
        private int count;

        private TestMsg(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }
}