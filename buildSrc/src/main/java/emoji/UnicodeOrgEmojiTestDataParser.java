package emoji;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class UnicodeOrgEmojiTestDataParser {
    // group start: "# group: Flags"
    private static final Pattern GROUP_ROW_PATTERN = Pattern.compile("^#\\s+group:\\s+(.+)$");
    // sub-group start: "# subgroup: flag"
    private static final Pattern SUB_GROUP_ROW_PATTERN =
            Pattern.compile("^#\\s+subgroup:\\s+(.+)$");
    // data row: "26AB                                       ; fully-qualified     # ⚫ black circle"
    private static final Pattern DATA_PART_ROW_PATTERN =
            Pattern.compile("^([0-9A-F ]+)\\s+;\\s+fully-qualified\\s*$");
    // # 🇬🇳 E2.0 flag: Guinea
    private static final Pattern TAGS_PART_ROW_PATTERN =
            Pattern.compile("#\\s+.+E\\d+\\.\\d+\\s+(.+)$");
    private static final StringBuilder msEscapeCodesBuilder = new StringBuilder(32);

    static List<EmojiData> parse(File testDataFile, Map<String, List<String>> extraTags)
            throws IOException {
        List<EmojiData> parsedEmojiData = new ArrayList<>();

        String group = "";
        String subgroup = "";

        int emojis = 0;

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
                            System.out.println("Entering emoji subgroup " + group + "/" + subgroup);
                        } else {
                            final int tagsIndex = line.lastIndexOf("#");
                            if (tagsIndex > 0) {
                                final String data = line.substring(0, tagsIndex);
                                final Matcher dataRowMatcher = DATA_PART_ROW_PATTERN.matcher(data);
                                final Matcher tagsRowMatcher =
                                        TAGS_PART_ROW_PATTERN.matcher(line.substring(tagsIndex));
                                if (dataRowMatcher.find() && tagsRowMatcher.find()) {
                                    emojis++;
                                    final String description = tagsRowMatcher.group(1);
                                    List<String> tags =
                                            Arrays.stream(description.split("[:,]", -1))
                                                    .filter(s -> !s.isEmpty())
                                                    .collect(Collectors.toList());
                                    final String output =
                                            convertToEscapeCodes(dataRowMatcher.group(1));
                                    if (extraTags.containsKey(output)) {
                                        extraTags
                                                .get(output)
                                                .forEach(
                                                        newTag -> {
                                                            if (!tags.contains(newTag))
                                                                tags.add(newTag);
                                                        });
                                    }

                                    EmojiData emojiData =
                                            new EmojiData(
                                                    emojis,
                                                    description,
                                                    group.replace(' ', '-')
                                                            + "-"
                                                            + subgroup.replace(' ', '-'),
                                                    output,
                                                    tags);

                                    parsedEmojiData.add(emojiData);
                                }
                            }
                        }
                    }
                }
            }
        }

        return parsedEmojiData;
    }

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
