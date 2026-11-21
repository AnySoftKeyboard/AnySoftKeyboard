package com.anysoftkeyboard.phonetic;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
 * Regression gate for conversion quality: converts every sentence of {@code zhuyin_eval.tsv} (made
 * by {@code build_zhuyin_phrases.py --eval-sentences}) and checks the accuracy floors.
 *
 * <p>The readings come from the same dictionary, so this measures segmentation and homophone
 * choice, not reading correctness.
 */
public class ConversionEvalTest {
  static final double MIN_SENTENCE_ACCURACY = 0.90;
  static final double MIN_CHAR_ACCURACY = 0.97;

  @Test
  public void testAccuracy() throws IOException {
    int sentences = 0;
    int sentencesRight = 0;
    int chars = 0;
    int charsRight = 0;
    final StringBuilder misses = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(
            new InputStreamReader(
                getClass().getClassLoader().getResourceAsStream("zhuyin_eval.tsv"),
                StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.startsWith("#") || line.isEmpty()) continue;
        final String[] parts = line.split("\t", -1);
        final ZhuyinComposer composer = new ZhuyinComposer(TestTables.zhuyin(), new UserModel());
        for (String syllable : parts[0].split(" ", -1)) {
          TestTables.type(composer, syllable);
          // No tone mark = first tone, typed with space (as on the real keyboard).
          if (!syllable.matches(".*[ˊˇˋ˙]$")) composer.onKey(' ');
        }
        composer.commitAll();
        final String got = composer.takeCommitText();
        final String expected = parts[1];
        sentences++;
        if (got.equals(expected)) {
          sentencesRight++;
        } else {
          misses.append("  ").append(expected).append(" -> ").append(got).append('\n');
        }
        for (int i = 0; i < expected.length(); i++) {
          chars++;
          if (i < got.length() && got.charAt(i) == expected.charAt(i)) charsRight++;
        }
      }
    }
    final double sentenceAccuracy = sentencesRight / (double) sentences;
    final double charAccuracy = charsRight / (double) chars;
    System.out.printf(
        "Zhuyin eval: sentences %d/%d (%.1f%%), chars %d/%d (%.1f%%)%nMisses:%n%s",
        sentencesRight,
        sentences,
        100 * sentenceAccuracy,
        charsRight,
        chars,
        100 * charAccuracy,
        misses);
    assertTrue("sentence accuracy " + sentenceAccuracy, sentenceAccuracy >= MIN_SENTENCE_ACCURACY);
    assertTrue("char accuracy " + charAccuracy, charAccuracy >= MIN_CHAR_ACCURACY);
  }
}
