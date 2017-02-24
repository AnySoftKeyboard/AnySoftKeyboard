package com.anysoftkeyboard.keyboards.views;

import android.content.Context;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;

import com.anysoftkeyboard.AnySoftKeyboardBaseTest;
import com.anysoftkeyboard.dictionaries.Suggest;
import com.anysoftkeyboard.gesturetyping.GestureTypingDetector;
import com.anysoftkeyboard.gesturetyping.Point;
import com.anysoftkeyboard.keyboards.AnyKeyboard;
import com.anysoftkeyboard.keyboards.Keyboard;
import com.anysoftkeyboard.keyboards.KeyboardFactory;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

// This class is here so that PointerTracker and the other keyboards.views.* classes can remain
//  package local
@RunWith(RobolectricTestRunner.class)
public class GestureWordsTest extends AnySoftKeyboardBaseTest {

    protected OnKeyboardActionListener mMockKeyboardListener;
    private AnyKeyboardViewBase mUnderTest;
    protected AnyKeyboard mEnglishKeyboard;
    private PointerTracker mMockPointerTrack;

    @Before
    public void setUp() throws Exception {
        mMockPointerTrack = Mockito.mock(PointerTracker.class);
        mMockKeyboardListener = Mockito.mock(OnKeyboardActionListener.class);
        AnyKeyboardViewBase view = createViewToTest(RuntimeEnvironment.application);
        setCreatedKeyboardView(view);
        mUnderTest.setOnKeyboardActionListener(mMockKeyboardListener);

        mEnglishKeyboard = KeyboardFactory.getEnabledKeyboards(RuntimeEnvironment.application)
                .get(0)
                .createKeyboard(RuntimeEnvironment.application, Keyboard.KEYBOARD_ROW_MODE_NORMAL);
        mEnglishKeyboard.loadKeyboard(mUnderTest.getThemedKeyboardDimens());

        mUnderTest.setKeyboard(mEnglishKeyboard, 0);
    }

    @CallSuper
    protected void setCreatedKeyboardView(@NonNull AnyKeyboardViewBase view) {
        mUnderTest = view;
    }

    protected AnyKeyboardViewBase createViewToTest(Context context) {
        return new AnyKeyboardViewBase(context, null);
    }

    public void testGivenInput(String expected, int[] keyCodesInPath, Point... recorded) {
        List<Keyboard.Key> keys = mAnySoftKeyboardUnderTest.getCurrentKeyboardForTests().getKeys();
        int width = mUnderTest.getWidth();
        int height = mUnderTest.getHeight();

        System.out.println("Got mock keyboard with dimensions " + width + "x" + height + " and " + keys.size() + " keys.");

        List<Point> gestureInput = new ArrayList<>();

        for (Point p : recorded) {
            gestureInput.add(new Point(p.x*width, p.y*height));
        }

        Suggest mSuggest = mAnySoftKeyboardUnderTest.getSpiedSuggest();

        List<CharSequence> wordsInPath = mSuggest.getWordsForPath(false, false,
                keyCodesInPath, keyCodesInPath.length,
                GestureTypingDetector.nearbyKeys(keys, gestureInput.get(0)),
                GestureTypingDetector.nearbyKeys(keys, gestureInput.get(gestureInput.size() - 1)), keys);
        List<Integer> frequenciesInPath = mSuggest.getFrequenciesForPath();
        List<CharSequence> gestureTypingPossibilities = GestureTypingDetector.getGestureWords(gestureInput,
                wordsInPath, frequenciesInPath, keys);

        System.out.println("For " + expected + " got " + gestureTypingPossibilities);
        Assert.assertTrue(gestureTypingPossibilities.size()>=1);
        Assert.assertEquals(gestureTypingPossibilities.get(0), expected);
    }

    // Put tests here from logcat here
    @Test public void test_the() {testGivenInput("the", new int[] {116, 103, 104, 103, 102, 114, 101, }, new Point(0.4398148f, 0.27906978f), new Point(0.4398148f, 0.27906978f), new Point(0.44351852f, 0.28036177f), new Point(0.46666667f, 0.29586563f), new Point(0.48240742f, 0.3113695f), new Point(0.5f, 0.33333334f), new Point(0.5175926f, 0.3604651f), new Point(0.53425926f, 0.3901809f), new Point(0.54907405f, 0.41860464f), new Point(0.56203705f, 0.44315246f), new Point(0.5712963f, 0.4625323f), new Point(0.5796296f, 0.47674417f), new Point(0.58981484f, 0.49354005f), new Point(0.59537035f, 0.502584f), new Point(0.5972222f, 0.50516796f), new Point(0.5972222f, 0.50516796f), new Point(0.59537035f, 0.50516796f), new Point(0.5935185f, 0.503876f), new Point(0.587037f, 0.5f), new Point(0.57592595f, 0.49224806f), new Point(0.55833334f, 0.48062015f), new Point(0.537037f, 0.46770027f), new Point(0.51296294f, 0.45478037f), new Point(0.48981482f, 0.44056848f), new Point(0.47037038f, 0.42894056f), new Point(0.4537037f, 0.41989663f), new Point(0.43888888f, 0.4121447f), new Point(0.42314816f, 0.4018088f), new Point(0.40555555f, 0.39147288f), new Point(0.38703704f, 0.37855297f), new Point(0.36574075f, 0.3630491f), new Point(0.34444445f, 0.34625322f), new Point(0.32592592f, 0.33074936f), new Point(0.30925927f, 0.31653747f), new Point(0.29537037f, 0.30490956f), new Point(0.28425926f, 0.29586563f), new Point(0.27592593f, 0.28940567f), new Point(0.2685185f, 0.28294572f), new Point(0.26296297f, 0.28036177f), new Point(0.25833333f, 0.2777778f), new Point(0.2537037f, 0.2751938f), new Point(0.25092593f, 0.27260983f), new Point(0.24907407f, 0.27002585f), new Point(0.24722221f, 0.26744187f), new Point(0.2462963f, 0.2648579f), new Point(0.2462963f, 0.2635659f), new Point(0.24722221f, 0.2635659f));}
    @Test public void test_quick() {testGivenInput("quick", new int[] {113, 119, 101, 114, 116, 121, 117, 105, 104, 103, 118, 99, 118, 104, 106, 107, }, new Point(0.06666667f, 0.3126615f), new Point(0.074074075f, 0.30620155f), new Point(0.09259259f, 0.2984496f), new Point(0.12222222f, 0.28811368f), new Point(0.15833333f, 0.28165373f), new Point(0.19629629f, 0.27906978f), new Point(0.23888889f, 0.28165373f), new Point(0.28611112f, 0.2855297f), new Point(0.3425926f, 0.28940567f), new Point(0.4074074f, 0.29328164f), new Point(0.47407407f, 0.29457363f), new Point(0.537037f, 0.29198965f), new Point(0.587963f, 0.29198965f), new Point(0.62314814f, 0.29198965f), new Point(0.6472222f, 0.29457363f), new Point(0.6666667f, 0.29715762f), new Point(0.68703705f, 0.29715762f), new Point(0.70555556f, 0.29328164f), new Point(0.72314817f, 0.28811368f), new Point(0.73796296f, 0.2842377f), new Point(0.74814814f, 0.28036177f), new Point(0.7546296f, 0.2777778f), new Point(0.7601852f, 0.2777778f), new Point(0.76203704f, 0.2777778f), new Point(0.76296294f, 0.2777778f), new Point(0.76111114f, 0.2777778f), new Point(0.7546296f, 0.2855297f), new Point(0.7351852f, 0.30878553f), new Point(0.6962963f, 0.35529715f), new Point(0.6472222f, 0.41472867f), new Point(0.59907407f, 0.4728682f), new Point(0.5564815f, 0.5245478f), new Point(0.52037036f, 0.5697674f), new Point(0.49166667f, 0.60594314f), new Point(0.46944445f, 0.6356589f), new Point(0.45092592f, 0.65891474f), new Point(0.43703705f, 0.6744186f), new Point(0.42685184f, 0.68475455f), new Point(0.42037037f, 0.69121444f), new Point(0.41666666f, 0.6963824f), new Point(0.4138889f, 0.6989664f), new Point(0.41203704f, 0.7002584f), new Point(0.4101852f, 0.7002584f), new Point(0.41111112f, 0.7002584f), new Point(0.41111112f, 0.7002584f), new Point(0.41759259f, 0.6963824f), new Point(0.44722223f, 0.6744186f), new Point(0.51666665f, 0.625323f), new Point(0.58981484f, 0.57622737f), new Point(0.64444447f, 0.54521966f), new Point(0.67777777f, 0.5271318f), new Point(0.6944444f, 0.51808786f), new Point(0.70648146f, 0.5129199f), new Point(0.71574074f, 0.50775194f), new Point(0.7268519f, 0.503876f), new Point(0.7361111f, 0.498708f), new Point(0.74444443f, 0.49483204f), new Point(0.7509259f, 0.49224806f), new Point(0.75648147f, 0.4883721f), new Point(0.76203704f, 0.4857881f), new Point(0.7675926f, 0.48320413f), new Point(0.77037036f, 0.48062015f), new Point(0.7740741f, 0.47803617f), new Point(0.7777778f, 0.47545218f), new Point(0.7824074f, 0.4715762f));}
    @Test public void test_brown() {testGivenInput("brown", new int[] {98, 103, 102, 114, 116, 121, 117, 105, 117, 121, 116, 114, 101, 119, 101, 114, 102, 103, 104, 106, 110, }, new Point(0.62222224f, 0.7338501f), new Point(0.6157407f, 0.72739017f), new Point(0.6111111f, 0.71834624f), new Point(0.6037037f, 0.70413435f), new Point(0.5935185f, 0.68217057f), new Point(0.5787037f, 0.6537468f), new Point(0.5611111f, 0.61886305f), new Point(0.5416667f, 0.5839793f), new Point(0.51944447f, 0.54521966f), new Point(0.49537036f, 0.50775194f), new Point(0.4712963f, 0.4741602f), new Point(0.44537038f, 0.44056848f), new Point(0.4185185f, 0.4121447f), new Point(0.39814815f, 0.3888889f), new Point(0.38055557f, 0.36821705f), new Point(0.3638889f, 0.3475452f), new Point(0.35f, 0.32945737f), new Point(0.34166667f, 0.31782946f), new Point(0.3361111f, 0.30749354f), new Point(0.33240741f, 0.3010336f), new Point(0.32962963f, 0.29586563f), new Point(0.3287037f, 0.29198965f), new Point(0.3287037f, 0.28940567f), new Point(0.3314815f, 0.2868217f), new Point(0.33888888f, 0.2842377f), new Point(0.3611111f, 0.2842377f), new Point(0.40648147f, 0.28165373f), new Point(0.47592592f, 0.2764858f), new Point(0.55f, 0.27131784f), new Point(0.60925925f, 0.27131784f), new Point(0.6574074f, 0.28036177f), new Point(0.69166666f, 0.28811368f), new Point(0.72407407f, 0.29586563f), new Point(0.7490741f, 0.2984496f), new Point(0.76203704f, 0.3010336f), new Point(0.7722222f, 0.30361757f), new Point(0.78055555f, 0.30620155f), new Point(0.7861111f, 0.30749354f), new Point(0.7916667f, 0.30749354f), new Point(0.79444444f, 0.30490956f), new Point(0.7972222f, 0.30232558f), new Point(0.79907405f, 0.2984496f), new Point(0.8009259f, 0.29586563f), new Point(0.80185187f, 0.29328164f), new Point(0.80185187f, 0.28940567f), new Point(0.8009259f, 0.2842377f), new Point(0.79907405f, 0.28036177f), new Point(0.7907407f, 0.27390182f), new Point(0.7740741f, 0.26873386f), new Point(0.7490741f, 0.26873386f), new Point(0.71481484f, 0.27002585f), new Point(0.6722222f, 0.27390182f), new Point(0.62777776f, 0.2764858f), new Point(0.5675926f, 0.2764858f), new Point(0.5037037f, 0.2764858f), new Point(0.43611112f, 0.2777778f), new Point(0.37962964f, 0.28165373f), new Point(0.32777777f, 0.2855297f), new Point(0.28333333f, 0.28940567f), new Point(0.24814814f, 0.29198965f), new Point(0.22037037f, 0.29457363f), new Point(0.20092593f, 0.29586563f), new Point(0.18703704f, 0.29586563f), new Point(0.17962962f, 0.29586563f), new Point(0.17592593f, 0.29586563f), new Point(0.17314816f, 0.29586563f), new Point(0.17222223f, 0.29586563f), new Point(0.17222223f, 0.29586563f), new Point(0.175f, 0.29586563f), new Point(0.18888889f, 0.30232558f), new Point(0.2175926f, 0.31653747f), new Point(0.2611111f, 0.3372093f), new Point(0.31944445f, 0.36692506f), new Point(0.39537036f, 0.40568477f), new Point(0.4712963f, 0.44573644f), new Point(0.54351854f, 0.48966408f), new Point(0.6018519f, 0.52067184f), new Point(0.6435185f, 0.5542636f), new Point(0.67407405f, 0.58010334f), new Point(0.69074076f, 0.59431523f), new Point(0.7f, 0.60465115f), new Point(0.7083333f, 0.61627907f), new Point(0.71481484f, 0.63049096f), new Point(0.7212963f, 0.64599484f), new Point(0.7277778f, 0.65891474f), new Point(0.7361111f, 0.67054266f), new Point(0.7398148f, 0.6744186f));}
    @Test public void test_fox() {testGivenInput("fox", new int[] {102, 103, 117, 105, 117, 104, 103, 102, 99, 120, }, new Point(0.39259258f, 0.49483204f), new Point(0.39259258f, 0.49483204f), new Point(0.3935185f, 0.49224806f), new Point(0.40092593f, 0.48449612f), new Point(0.42314816f, 0.46640828f), new Point(0.4611111f, 0.44056848f), new Point(0.5092593f, 0.40697673f), new Point(0.55925924f, 0.373385f), new Point(0.60925925f, 0.3385013f), new Point(0.65092593f, 0.31007752f), new Point(0.68703705f, 0.2868217f), new Point(0.71944445f, 0.26744187f), new Point(0.74814814f, 0.251938f), new Point(0.7675926f, 0.24160206f), new Point(0.78055555f, 0.23385014f), new Point(0.7888889f, 0.22868218f), new Point(0.7953704f, 0.2260982f), new Point(0.8009259f, 0.22351421f), new Point(0.8037037f, 0.22093023f), new Point(0.8055556f, 0.22093023f), new Point(0.8074074f, 0.22093023f), new Point(0.80925924f, 0.22093023f), new Point(0.80925924f, 0.22093023f), new Point(0.80925924f, 0.22093023f), new Point(0.80925924f, 0.22093023f), new Point(0.80925924f, 0.22093023f), new Point(0.8074074f, 0.22222222f), new Point(0.8037037f, 0.2260982f), new Point(0.7953704f, 0.23514211f), new Point(0.77592593f, 0.250646f), new Point(0.7425926f, 0.28036177f), new Point(0.69166666f, 0.32041344f), new Point(0.6324074f, 0.36950904f), new Point(0.5694444f, 0.4237726f), new Point(0.4611111f, 0.51937985f), new Point(0.41666666f, 0.55813956f), new Point(0.38425925f, 0.5852713f), new Point(0.35185185f, 0.6098191f), new Point(0.32685184f, 0.63178295f), new Point(0.30648148f, 0.6498708f), new Point(0.29166666f, 0.6653747f), new Point(0.28055555f, 0.6770026f), new Point(0.27314815f, 0.68475455f), new Point(0.2685185f, 0.68992245f), new Point(0.26481482f, 0.69250643f), new Point(0.26203704f, 0.6950904f), new Point(0.26018518f, 0.6976744f), new Point(0.25833333f, 0.7002584f), new Point(0.2574074f, 0.70284235f), new Point(0.2574074f, 0.70542634f), new Point(0.2574074f, 0.70542634f));}
    @Test public void test_jumped() {testGivenInput("jumped", new int[] {106, 117, 105, 106, 107, 109, 108, 112, 111, 105, 117, 121, 116, 114, 101, 100, }, new Point(0.71666664f, 0.52196383f), new Point(0.71574074f, 0.52067184f), new Point(0.71018517f, 0.5155039f), new Point(0.70648146f, 0.50904393f), new Point(0.70092595f, 0.49354005f), new Point(0.6953704f, 0.4625323f), new Point(0.6898148f, 0.4237726f), new Point(0.68333334f, 0.3875969f), new Point(0.67777777f, 0.35271317f), new Point(0.67407405f, 0.3255814f), new Point(0.66944444f, 0.30490956f), new Point(0.6675926f, 0.28940567f), new Point(0.6657407f, 0.2777778f), new Point(0.6657407f, 0.27002585f), new Point(0.6657407f, 0.26614988f), new Point(0.6657407f, 0.2635659f), new Point(0.6657407f, 0.26098192f), new Point(0.6675926f, 0.2648579f), new Point(0.67407405f, 0.27390182f), new Point(0.6851852f, 0.29586563f), new Point(0.6990741f, 0.33074936f), new Point(0.7138889f, 0.374677f), new Point(0.7268519f, 0.4237726f), new Point(0.7407407f, 0.4728682f), new Point(0.75277776f, 0.5142119f), new Point(0.76296294f, 0.5503876f), new Point(0.77037036f, 0.57622737f), new Point(0.77592593f, 0.6020672f), new Point(0.7814815f, 0.624031f), new Point(0.78518516f, 0.6421189f), new Point(0.7888889f, 0.6550388f), new Point(0.7907407f, 0.6640827f), new Point(0.7925926f, 0.66925067f), new Point(0.79351854f, 0.67312664f), new Point(0.79351854f, 0.6744186f), new Point(0.79351854f, 0.67312664f), new Point(0.7953704f, 0.6679587f), new Point(0.80185187f, 0.65633076f), new Point(0.81574076f, 0.625323f), new Point(0.8324074f, 0.5826873f), new Point(0.8509259f, 0.53229976f), new Point(0.8675926f, 0.4870801f), new Point(0.8842593f, 0.4392765f), new Point(0.90833336f, 0.36434108f), new Point(0.9157407f, 0.33333334f), new Point(0.9212963f, 0.31007752f), new Point(0.925f, 0.29069766f), new Point(0.9287037f, 0.27390182f), new Point(0.9305556f, 0.25839794f), new Point(0.9324074f, 0.24547803f), new Point(0.9324074f, 0.23772609f), new Point(0.9324074f, 0.23255815f), new Point(0.9324074f, 0.22739018f), new Point(0.9324074f, 0.22222222f), new Point(0.9324074f, 0.21705426f), new Point(0.9324074f, 0.21317829f), new Point(0.9324074f, 0.20930232f), new Point(0.9305556f, 0.20671834f), new Point(0.9287037f, 0.20413436f), new Point(0.92685187f, 0.2015504f), new Point(0.91851854f, 0.19896641f), new Point(0.9027778f, 0.19767442f), new Point(0.87314814f, 0.19767442f), new Point(0.82592595f, 0.19896641f), new Point(0.7601852f, 0.20671834f), new Point(0.6925926f, 0.21705426f), new Point(0.61851853f, 0.23385014f), new Point(0.55f, 0.24806201f), new Point(0.4861111f, 0.25710595f), new Point(0.42962962f, 0.26098192f), new Point(0.38333333f, 0.26098192f), new Point(0.3462963f, 0.25710595f), new Point(0.32037038f, 0.251938f), new Point(0.29814816f, 0.249354f), new Point(0.28148147f, 0.24677002f), new Point(0.26944444f, 0.24677002f), new Point(0.2611111f, 0.24677002f), new Point(0.2537037f, 0.24677002f), new Point(0.25f, 0.24677002f), new Point(0.24722221f, 0.24677002f), new Point(0.24537037f, 0.24677002f), new Point(0.24351852f, 0.24677002f), new Point(0.24351852f, 0.251938f), new Point(0.2462963f, 0.2648579f), new Point(0.2537037f, 0.29198965f), new Point(0.26296297f, 0.32816538f), new Point(0.27314815f, 0.36821705f), new Point(0.2824074f, 0.39922482f), new Point(0.2888889f, 0.42118862f), new Point(0.2962963f, 0.44444445f), new Point(0.30185184f, 0.45994833f), new Point(0.30555555f, 0.4715762f), new Point(0.30925927f, 0.48191214f), new Point(0.31296295f, 0.49095607f), new Point(0.31666666f, 0.498708f), new Point(0.31944445f, 0.503876f), new Point(0.3212963f, 0.50775194f), new Point(0.32314816f, 0.5103359f), new Point(0.32592592f, 0.5129199f), new Point(0.3314815f, 0.51808786f));}
}
