package emoji;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        EmojiData lastRootEmoji = null;
        for (Element anElement : aTableElement.getAllElements().stream().filter(element -> element.tagName().equals("tr")).collect(Collectors.toList())) {
            EmojiData emojiData = createEmojiDataFromTableChildElements(lastRootEmoji, anElement.getAllElements().stream().filter(element -> element.tagName().equals("td")).collect(Collectors.toList()));
            if (emojiData != null) {
                lastRootEmoji = emojiData;
                parsedEmojiData.add(emojiData);
            }
        }
    }

    private static EmojiData createEmojiDataFromTableChildElements(EmojiData lastRootEmojiData, List<Element> allElements) {
        Element index = findTagElementWithClass(allElements, "rchars");
        Element output = findTagElementWithClass(allElements, "chars");
        Element name = findTagElementWithClass(allElements, "name");

        if (index != null && output != null && name != null) {
            if (index.text().matches("\\d+") &&
                    output.text().length() > 0) {
                final EmojiData currentEmoji = new EmojiData(
                        Integer.parseInt(index.text()),
                        output.text(),
                        name.text(),
                        getTagsFromNameElement(name.text()));

                if (currentEmoji.name.matches("^.+:\\s.+$")/*this is a variant emoji*/) {
                    //we will only use it if we have the correct root emoji
                    if (lastRootEmojiData != null) {
                        final String rootEmojiName = lastRootEmojiData.name.toLowerCase(Locale.US);
                        final String currentEmojiName = currentEmoji.name.toLowerCase(Locale.US);
                        if (currentEmojiName.startsWith(rootEmojiName + ": ")) {
                            lastRootEmojiData.addVariant(currentEmoji);
                        }
                    }
                    return null;
                } else {
                    return currentEmoji;
                }
            }
        }

        return null;
    }

    private static Element findTagElementWithClass(List<Element> allElements, String className) {
        final Optional<Element> td = allElements.stream().filter(element -> element.tagName().equals("td")).filter(element -> element.classNames().contains(className)).findFirst();
        return td.orElse(null);
    }

    /*
        private static List<String> getTagsFromTagsElement(Element tags, String name) {
            if (name.indexOf(":") > 0) {
                name = name.substring(0, name.indexOf(":"));
            }

            List<String> nameTokens = Arrays.asList(name.replace(' ', ',').replace("'", "").replace("-", "").split(","));
            List<String> tagsTokens = Arrays.asList(tags.text().replace('|', ',').split(","));

            Stream<String> tagsStream = Stream.concat(nameTokens.stream(), tagsTokens.stream());

            HashSet<String> tagsSeen = new HashSet<>(nameTokens.size() + tagsTokens.size());

            return tagsStream.map(String::trim).map(String::toLowerCase).filter(s -> !s.isEmpty()).filter(tagsSeen::add).collect(Collectors.toList());
        }
    */
    private static List<String> getTagsFromNameElement(String name) {
        if (name.indexOf(":") > 0) {
            name = name.substring(0, name.indexOf(":"));
        }

        List<String> nameTokens = Arrays.asList(name.replace(' ', ',').replace("&", "").replace("'", "").replace("-", "").split(","));
        //List<String> tagsTokens = Arrays.asList(tags.text().replace('|', ',').split(","));

        //Stream<String> tagsStream = Stream.concat(nameTokens.stream(), tagsTokens.stream());

        HashSet<String> tagsSeen = new HashSet<>(nameTokens.size());

        return nameTokens.stream().map(String::trim).map(String::toLowerCase).filter(s -> !s.isEmpty()).filter(tagsSeen::add).collect(Collectors.toList());
    }
}
