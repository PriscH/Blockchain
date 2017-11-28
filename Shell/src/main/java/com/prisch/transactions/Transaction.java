package com.prisch.transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.immutables.value.Value;

import java.util.List;
import java.util.Map;

@Value.Immutable
@JsonSerialize
@JsonDeserialize
public interface Transaction {

    int version();

    List<Input> inputs();
    List<Output> outputs();

    String hash();
    String signature();
    String publicKey();

    // lockHeight
    // stopJaco
    Map<String, String> properties();

    @Value.Immutable
    interface Input {
        int blockHeight();
        String transactionHash();

        int index();
    }

    @Value.Immutable
    interface Output {
        int index();

        String address();
        int amount();
    }

    default String toJson() throws JsonProcessingException {
        return new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(this);
    }
}
