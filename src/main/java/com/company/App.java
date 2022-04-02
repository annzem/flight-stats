package com.company;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length == 0) {
            String resourcePath = "/tickets.json";
            InputStream input = App.class.getResourceAsStream(resourcePath);
            if (input == null) {
                throw new FileNotFoundException("resource " + resourcePath + " not found");
            }
            start(input);
        } else {
            start(new FileInputStream(new File(args[0])));
        }
    }

    private static void start(InputStream input) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Map<String, Object>> tickets = (List) (mapper.readValue(input, Map.class)).get("tickets");

            List<Long> data = new ArrayList<>();
            for (Map map : tickets) {
                LocalDateTime departure = LocalDateTime.parse((map.get("departure_time")) + ", " + (map.get("departure_date")), DateTimeFormatter.ofPattern("H:mm, dd.MM.uu"));
                LocalDateTime arrival = LocalDateTime.parse((map.get("arrival_time")) + ", " + (map.get("arrival_date")), DateTimeFormatter.ofPattern("H:mm, dd.MM.uu"));
                data.add(Duration.between(departure, arrival).toMinutes());
            }

            long average = average(data);
            long percentile = percentile(data, 90);

            System.out.printf("Среднее арифметическое времени полета: %s ч %s мин\n",
                    Duration.ofMinutes(average).toHoursPart(),
                    Duration.ofMinutes(average).toMinutesPart());

            System.out.printf("90-й процентиль времени полета: %s ч %s мин\n",
                    Duration.ofMinutes(percentile).toHoursPart(),
                    Duration.ofMinutes(percentile).toMinutesPart());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Long average(List<Long> data) {
        Duration sum = Duration.ofMinutes(0);
        for (Long time : data) {
            sum = sum.plus(Duration.ofMinutes(time));
        }
        return sum.toMinutes() / data.size();
    }

    public static Long percentile(List<Long> data, double percentile) {
        int index = (int) Math.ceil(percentile / 100.0 * data.size());
        return data.get(index - 1);
    }


}
