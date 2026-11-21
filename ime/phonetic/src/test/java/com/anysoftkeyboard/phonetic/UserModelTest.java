package com.anysoftkeyboard.phonetic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class UserModelTest {
  private final List<Syllable> mReadings =
      Arrays.asList(Syllable.parse("ㄋㄧˇ"), Syllable.parse("ㄏㄠˇ"));

  @Test
  public void testBonusAfterLearnIsToneAgnostic() {
    final UserModel model = new UserModel();
    assertEquals(0f, model.bonus(mReadings, 0, 1, "妳"), 0f);
    model.learn(mReadings, 0, 1, "妳");
    assertTrue(model.bonus(mReadings, 0, 1, "妳") >= UserModel.PICK_BONUS);
    final List<Syllable> toneless = Arrays.asList(mReadings.get(0).withTone(0));
    assertTrue(model.bonus(toneless, 0, 1, "妳") > 0f);
    assertEquals(0f, model.bonus(mReadings, 0, 1, "你"), 0f);
  }

  @Test
  public void testForgetUndoesLearn() {
    final UserModel model = new UserModel();
    model.learn(mReadings, 0, 1, "妳");
    model.learn(mReadings, 0, 1, "妳");
    model.forget(mReadings, 0, 1, "妳");
    assertTrue(model.bonus(mReadings, 0, 1, "妳") > 0f);
    model.forget(mReadings, 0, 1, "妳");
    assertEquals(0f, model.bonus(mReadings, 0, 1, "妳"), 0f);
    model.forget(mReadings, 0, 1, "妳"); // no-op
  }

  @Test
  public void testRoundTrip() throws Exception {
    final UserModel model = new UserModel();
    model.learn(mReadings, 0, 2, "妳好");
    model.learn(mReadings, 0, 2, "妳好");
    assertTrue(model.isDirty());
    final StringWriter out = new StringWriter();
    model.write(out);
    assertFalse(model.isDirty());

    final UserModel read = UserModel.read(new StringReader(out + "garbage\nx\ty\tz\n"));
    assertEquals(model.bonus(mReadings, 0, 2, "妳好"), read.bonus(mReadings, 0, 2, "妳好"), 0f);
  }

  @Test
  public void testCapacityIsBounded() throws Exception {
    final UserModel model = new UserModel();
    for (int i = 0; i < UserModel.CAPACITY + 50; i++) model.learn(mReadings, 0, 1, "字" + i);
    final StringWriter out = new StringWriter();
    model.write(out);
    assertEquals(UserModel.CAPACITY, out.toString().split("\n", -1).length - 1);
    assertEquals(0f, model.bonus(mReadings, 0, 1, "字0"), 0f);
  }
}
