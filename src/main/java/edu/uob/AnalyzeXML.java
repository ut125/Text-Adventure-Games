package edu.uob;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;


public class AnalyzeXML {

    //accessing XML parsed attributes
    private static final Map<String, List<String>> attributeMap = new HashMap<>();

    public static List<GameAction> AnalyzeXMLs(File actionsFile, Set<String> validTriggers, Set<String> validSubjects) {
        List<GameAction> actions = new LinkedList<>();

        try {
            // XML parsed
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(actionsFile);

            // clear attributeMap to store new data
            attributeMap.clear();

            // nodeList is a list of XML tags
            NodeList actionNodes = doc.getElementsByTagName("action");
            for (int i = 0; i < actionNodes.getLength(); i++) {
                // get the current <action> tag
                Element actionElement = (Element) actionNodes.item(i);

                // analyze the triggers, subjects, consumed, produced, narration
                LinkedList<String> triggers = new LinkedList<>(AnalyzeXML.getElementsText(actionElement, "triggers", "keyphrase"));
                LinkedList<String> subjects = new LinkedList<>(AnalyzeXML.getElementsText(actionElement, "subjects", "entity"));
                LinkedList<String> consumed = new LinkedList<>(AnalyzeXML.getElementsText(actionElement, "consumed", "entity"));
                LinkedList<String> produced = new LinkedList<>(AnalyzeXML.getElementsText(actionElement, "produced", "entity"));

                // read the text of the <narration>
                String narration = AnalyzeXML.getElementText(actionElement);

                // add to collection
                validTriggers.addAll(triggers);
                validSubjects.addAll(subjects);

                // arrange in order（A → Z）
                Collections.sort(triggers);
                Collections.sort(subjects);

                // add to attributeMap
                AnalyzeXML.addToAttributeMap("triggers", triggers);
                AnalyzeXML.addToAttributeMap("subjects", subjects);
                AnalyzeXML.addToAttributeMap("consumed", consumed);
                AnalyzeXML.addToAttributeMap("produced", produced);

                GameAction action = new GameAction(triggers, subjects, consumed, produced, narration);
                actions.add(action);
            }
        } catch (Exception e) {
            // Handle any parsing or runtime errors
            System.out.println(String.format("[Error] parsing XML: " , e.getMessage()));
            e.printStackTrace();
        }

        return actions;
    }


    //parses the contents of all sub-tags within a tag
    private static List<String> getElementsText(Element parent, String tagName, String childTagName) {
        List<String> texts = new LinkedList<>();

        //find All <tagName> Tags
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return texts;
        }

        //first <tagName>, ex.<triggers>
        Element element = (Element) nodes.item(0);
        NodeList childNodes = element.getElementsByTagName(childTagName);

        // ex.<keyphrase>
        for (int i = 0; i < childNodes.getLength(); i++) {
            String text = childNodes.item(i).getTextContent().trim();
            if (!text.isEmpty()) {
                texts.add(text);
            }
        }
        return texts;
    }

    //get the content of the <narration> text in the XML
    private static String getElementText(Element parent) {
        NodeList nodes = parent.getElementsByTagName("narration");

        //if is empty
        if (nodes.getLength() == 0) {
            return "";
        }

        return nodes.item(0).getTextContent().trim();
    }


    //Adding to a Map
    private static void addToAttributeMap(String key, List<String> values) {
        //create a new list if the key doesn't exist
        if (!attributeMap.containsKey(key)) {
            attributeMap.put(key, new LinkedList<>());
        }
        //add the values to the existing list
        List<String> existingValues = attributeMap.get(key);
        if (existingValues != null) {
            existingValues.addAll(values);
        }
    }

    //getter for attributeMap
    public static Map<String, List<String>> getAttributeMap() {
        return attributeMap;
    }
}
