package emoji;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

class UnicodeOrgEmojiHtmlParser {
    static List<EmojiData> parse(File htmlFile) throws IOException {
        List<EmojiData> parsedEmojiData = new ArrayList<>();
        Document document = org.jsoup.Jsoup.parse(htmlFile, "UTF-8");
        for (Element anElement : document.getAllElements()) {
            if (anElement.tagName().equals("table")) {
                parseEmojisTable(anElement, parsedEmojiData);
                return parsedEmojiData;
            }
        }


        return parsedEmojiData;
    }

    private static void parseEmojisTable(Element aTableElement, List<EmojiData> parsedEmojiData) {
        for (Element anElement : aTableElement.getAllElements().stream().filter(element -> element.tagName().equals("tr")).collect(Collectors.toList())) {
            EmojiData emojiData = createEmojiDataFromTableChildElements(anElement.getAllElements().stream().filter(element -> element.tagName().equals("td")).collect(Collectors.toList()));
            if (emojiData != null) {
                parsedEmojiData.add(emojiData);
            }
        }
    }

    private static EmojiData createEmojiDataFromTableChildElements(List<Element> allElements) {
        /*System.out.println("Have "+allElements.size()+":");
        for (int i=0; i<allElements.size(); i++) {
            StringBuilder builder = new StringBuilder("#").append(i).append(": ")
                    .append(allElements.get(i).tagName())
                    .append(" ").append(allElements.get(i).text());
            System.out.println(builder);
        }*/
        if (allElements.size() < 18) return null;

        Element index = allElements.get(0);
        Element output = allElements.get(2);
        Element name = allElements.get(15);
        Element tags = allElements.get(18);

        if (index.tagName().equals("td") &&
                output.tagName().equals("td") &&
                name.tagName().equals("td") &&
                tags.tagName().equals("td")) {
            //System.out.println(String.format(Locale.US, "emoji %s of code %s, name '%s'.", index.text(), code.text(), name.text()));
            if (index.text().matches("\\d+") &&
                    output.text().length() > 0 &&
                    !name.text().contains("type-")) {
                return new EmojiData(
                        Integer.parseInt(index.text()),
                        output.text(),
                        name.text(),
                        getTagsFromTagsElement(tags));
            }
        }

        return null;
    }

    private static String[] getTagsFromTagsElement(Element tags) {
        return tags.text().replace(" ", "").split(",");
    }
}
