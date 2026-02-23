package edu.uob;

import java.util.List;
import java.util.Map;

public class Print {

    public static void printAttributeMap() {
        Map<String, List<String>> attributeMap = AnalyzeXML.getAttributeMap();
        System.out.println("\nAttribute Map:");

        for (Map.Entry<String, List<String>> entry : attributeMap.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            System.out.println(String.format("[LIST] %s: %s", key, values));

        }
        System.out.println("\n");
    }
}

