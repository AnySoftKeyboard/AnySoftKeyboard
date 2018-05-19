package emoji;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class UnicodeOrgEmojiTestDataParser {

    //group start: "# group: Flags"
    private static final Pattern GROUP_ROW_PATTERN = Pattern.compile("^#\\s+group:\\s+(.+)$");
    //sub-group start: "# subgroup: flag"
    private static final Pattern SUB_GROUP_ROW_PATTERN = Pattern.compile("^#\\s+subgroup:\\s+(.+)$");
    //data row: "26AB                                       ; fully-qualified     # ⚫ black circle"
    private static final Pattern DATA_PART_ROW_PATTERN = Pattern.compile("^([0-9A-F ]+)\\s+;\\s+fully-qualified\\s*$");
    private static final Pattern TAGS_PART_ROW_PATTERN = Pattern.compile("([\\w\\s]+)$");

    static List<EmojiData> parse(File testDataFile) throws IOException {
        List<EmojiData> parsedEmojiData = new ArrayList<>();

        String group = "";
        String subgroup = "";

        int emojis = 0;
        EmojiData previousEmoji = null;

        try (FileReader fileReader = new FileReader(testDataFile)) {
            try (BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.flush();
                    final Matcher groupMatcher = GROUP_ROW_PATTERN.matcher(line);
                    if (groupMatcher.find()) {
                        group = groupMatcher.group(1);
                        subgroup = "";
                        System.out.println("New emoji group " + group);
                    } else {
                        final Matcher subGroupMatcher = SUB_GROUP_ROW_PATTERN.matcher(line);
                        if (subGroupMatcher.find()) {
                            subgroup = subGroupMatcher.group(1);
                            System.out.println("Entering emoji subgroup " + group + "/" + subgroup );
                        } else {
                            final int tagsIndex = line.lastIndexOf("#");
                            if (tagsIndex > 0) {
                                final String data = line.substring(0, tagsIndex);
                                final boolean isVariant = line.substring(tagsIndex).contains(":");
                                final Matcher dataRowMatcher = DATA_PART_ROW_PATTERN.matcher(data);
                                final Matcher tagsRowMatcher = TAGS_PART_ROW_PATTERN.matcher(line.substring(tagsIndex));
                                if (dataRowMatcher.find() && tagsRowMatcher.find()) {
                                    emojis++;
                                    List<String> tags = Arrays.stream(tagsRowMatcher.group(1).split("\\s+"))
                                            .filter(s -> !s.isEmpty())
                                            .collect(Collectors.toList());

                                    EmojiData emojiData = new EmojiData(emojis,
                                            group.replace(' ', '-') + "-" + subgroup.replace(' ', '-'),
                                            convertToEscapeCodes(dataRowMatcher.group(1)), tags);

                                    if (isVariant) {
                                        previousEmoji.addVariant(emojiData);
                                    } else {
                                        previousEmoji = emojiData;
                                        parsedEmojiData.add(previousEmoji);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return parsedEmojiData;
    }

    private static final StringBuilder msEscapeCodesBuilder = new StringBuilder(32);
    private static String convertToEscapeCodes(String hexString) {
        msEscapeCodesBuilder.setLength(0);

        hexString = hexString.trim();
        String[] parts = hexString.split("\\s+");

        for (String part : parts) {
            msEscapeCodesBuilder.append(Character.toChars(Integer.parseInt(part, 16)));
        }

        return msEscapeCodesBuilder.toString();
    }
}
