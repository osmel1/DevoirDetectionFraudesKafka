package org.example;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.time.Instant;

public class InfluxDBWriter {

    private static final String token = "h4Mf3Hihu3wR8kAOUQ36FmAqChsCIq81ZdrYuabiTcWZHvt0HmEV1lbnFEdlpgKkll0gZRUU-XOUlFY1xs7isw==";
    private static final String bucket = "fraud-detection-bucket";
    private static final String org = "fraud-detection-org";
    private static final InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:8086", token.toCharArray());

    public static void writeTransaction(String userId, double amount, String timestamp) {
        Point point = Point.measurement("suspicious_transactions")
                .addTag("userId", userId)
                .addField("amount", amount)
                .time(Instant.parse(timestamp), WritePrecision.MS);
        client.getWriteApiBlocking().writePoint(bucket, org, point);
    }
}
