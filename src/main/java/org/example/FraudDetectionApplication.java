package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Properties;
public class FraudDetectionApplication {
    public static void main(String[] args) {
        // Configuration des propriétés de l'application Kafka Streams

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "fraud-detection-app");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // Création d'un constructeur de flux (StreamsBuilder) pour définir le traitement des données

        StreamsBuilder builder = new StreamsBuilder();
        // Création d'un flux de données en consommant les messages depuis le topic "transactions-input"

        KStream<String, String> transactions = builder.stream("transactions-input");


        // Filtrage des transactions suspectes (montant supérieur à 10 000)
        KStream<String, String> suspiciousTransactions = transactions.filter((key, value) -> {
            try {
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(value, JsonObject.class);
                return jsonObject.get("amount").getAsDouble() > 10000;
            } catch (Exception e) {
                return false;
            }
        });

        // Traitement des transactions suspectes pour écrire dans une base de données InfluxDB

        suspiciousTransactions.foreach((key, value) -> {
            try {
                Gson gson = new Gson();
                JsonObject json = gson.fromJson(value, JsonObject.class);
                String userId = json.get("userId").getAsString();
                double amount = json.get("amount").getAsDouble();
                String timestamp = json.get("timestamp").getAsString();
                InfluxDBWriter.writeTransaction(userId, amount, timestamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Écriture des transactions suspectes vers un autre topic Kafka "fraud-alerts"

        suspiciousTransactions.to("fraud-alerts", Produced.with(Serdes.String(), Serdes.String()));

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();
    }
}