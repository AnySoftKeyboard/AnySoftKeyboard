package net.evendanan.pixel;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class UiUtils {
  /**
   * Will set the title in the hosting Activity's title. Will only set the title if the fragment is
   * hosted by the Activity's manager, and not inner one.
   */
  public static void setActivityTitle(Fragment fragment, CharSequence title) {
    FragmentActivity activity = fragment.requireActivity();
    if (true /*activity.getSupportFragmentManager() == fragment.getParentFragmentManager()*/) {
      activity.setTitle(title);
    }
  }

  public static void setActivityTitle(Fragment fragment, @StringRes int title) {
    setActivityTitle(fragment, fragment.getString(title));
  }

  public static void setupLink(
      View root, int showMoreLinkId, ClickableSpan clickableSpan, boolean reorderLinkToLastChild) {
    TextView clickHere = root.findViewById(showMoreLinkId);
    if (reorderLinkToLastChild) {
      ViewGroup rootContainer = (ViewGroup) root;
      rootContainer.removeView(clickHere);
      rootContainer.addView(clickHere);
    }

    SpannableStringBuilder sb = new SpannableStringBuilder(clickHere.getText());
    sb.clearSpans(); // removing any previously (from instance-state) set click spans.
    sb.setSpan(clickableSpan, 0, clickHere.getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    clickHere.setMovementMethod(LinkMovementMethod.getInstance());
    clickHere.setText(sb);
  }
}
