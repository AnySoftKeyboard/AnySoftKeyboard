//package com.menny.android.anysoftkeyboard;
//
//import android.content.res.Resources;
//import android.graphics.drawable.Drawable;
//import android.util.TypedValue;
//
//import org.mockito.Mockito;
//import org.robolectric.annotation.Implementation;
//import org.robolectric.annotation.Implements;
//import org.robolectric.shadows.ShadowResources;
//
//@Implements(value = Resources.class)
//public class AnyShadowResources extends ShadowResources {
//
//    @Implementation
//    @Override
//    public Drawable loadDrawable(TypedValue value, int id, Resources.Theme theme) throws Resources.NotFoundException {
//        if (id == R.mipmap.ic_launcher) {
//            return Mockito.mock(Drawable.class);
//        } else {
//            return super.loadDrawable(value, id, theme);
//        }
//    }
//}
