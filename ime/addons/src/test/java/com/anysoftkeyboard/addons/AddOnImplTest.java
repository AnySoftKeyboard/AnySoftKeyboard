package com.anysoftkeyboard.addons;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.anysoftkeyboard.AnySoftKeyboardRobolectricTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

@RunWith(AnySoftKeyboardRobolectricTestRunner.class)
public class AddOnImplTest {

  @Test
  public void testEquals() {
    TestableAddOn addOn1 = new TestableAddOn("id1", "name", 8);
    TestableAddOn addOn2 = new TestableAddOn("id2", "name", 8);
    TestableAddOn addOn11 = new TestableAddOn("id1", "name111", 8);
    TestableAddOn addOn1DifferentApiVersion = new TestableAddOn("id1", "name", 7);

    Assert.assertEquals(addOn1, addOn11);
    Assert.assertNotEquals(addOn1, addOn2);
    Assert.assertEquals(addOn1.hashCode(), addOn11.hashCode());
    Assert.assertNotEquals(addOn1.hashCode(), addOn2.hashCode());
    Assert.assertNotEquals(addOn1, addOn1DifferentApiVersion);

    Assert.assertNotEquals(new Object(), addOn1);
  }

  @Test
  public void testToString() {
    String toString = new TestableAddOn("id1", "name111", 8).toString();

    Assert.assertTrue(toString.contains("name111"));
    Assert.assertTrue(toString.contains("id1"));
    Assert.assertTrue(toString.contains("TestableAddOn"));
    Assert.assertTrue(toString.contains(getApplicationContext().getPackageName()));
    Assert.assertTrue(toString.contains("API-8"));
  }

  @Test
  public void testUsesLocalResourceMapper() {
    final AddOn.AddOnResourceMapping resourceMapping =
        new TestableAddOn("id1", "name111", 8).getResourceMapping();
    Assert.assertEquals(8, resourceMapping.getApiVersion());
    // always returns the same thing
    Assert.assertSame(
        R.styleable.KeyboardLayout_Key,
        resourceMapping.getRemoteStyleableArrayFromLocal(R.styleable.KeyboardLayout_Key));
    Assert.assertEquals(3355, resourceMapping.getLocalAttrId(3355));
    Assert.assertEquals(
        R.attr.keyDynamicEmblem, resourceMapping.getLocalAttrId(R.attr.keyDynamicEmblem));
  }

  @Test
  public void testUsesRemoteResourceMapper() {
    final Context remote = Mockito.spy(getApplicationContext());
    Mockito.doReturn("com.example.else").when(remote).getPackageName();
    final Resources remoteRes = Mockito.spy(remote.getResources());
    Mockito.doAnswer(
            invocation ->
                getApplicationContext()
                    .getResources()
                    .getIdentifier(
                        (String) invocation.getArguments()[0],
                        (String) invocation.getArguments()[1],
                        getApplicationContext().getPackageName()))
        .when(remoteRes)
        .getIdentifier(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    Mockito.doReturn(remoteRes).when(remote).getResources();

    final AddOn.AddOnResourceMapping resourceMapping =
        new TestableAddOn(remote, "id1", "name111", 9).getResourceMapping();
    Assert.assertEquals(9, resourceMapping.getApiVersion());

    Assert.assertArrayEquals(
        R.styleable.KeyboardLayout_Key,
        resourceMapping.getRemoteStyleableArrayFromLocal(R.styleable.KeyboardLayout_Key));
    Assert.assertNotSame(
        R.styleable.KeyboardLayout_Key,
        resourceMapping.getRemoteStyleableArrayFromLocal(R.styleable.KeyboardLayout_Key));
    Assert.assertEquals(0, resourceMapping.getLocalAttrId(3355));
    Assert.assertEquals(
        R.attr.keyDynamicEmblem, resourceMapping.getLocalAttrId(R.attr.keyDynamicEmblem));
  }

  @Test
  public void testSetNewConfiguration() {
    var withLocal = new TestableAddOn("id1", "name111", 8);
    Assert.assertSame(getApplicationContext(), withLocal.getPackageContext());
    Assert.assertEquals(
        1, withLocal.getPackageContext().getResources().getConfiguration().orientation);

    var newConfig = getApplicationContext().getResources().getConfiguration();
    newConfig.orientation = 2;
    withLocal.setNewConfiguration(newConfig);
    Assert.assertNotSame(getApplicationContext(), withLocal.getPackageContext());
    Assert.assertEquals(
        getApplicationContext().getPackageName(), withLocal.getPackageContext().getPackageName());
    Assert.assertEquals(
        2, withLocal.getPackageContext().getResources().getConfiguration().orientation);
  }

  @Test
  public void testSetNewConfigurationOnRemote() throws Exception {
    final Context local = Mockito.spy(getApplicationContext());
    Mockito.doAnswer(
            invocation -> {
              final Context remote = Mockito.spy(getApplicationContext());
              Mockito.doReturn("com.example.else").when(remote).getPackageName();

              Mockito.doAnswer(
                      remoteInvocation -> {
                        final Resources remoteRes = Mockito.spy(remote.getResources());
                        Mockito.doReturn(remoteInvocation.getArguments()[0])
                            .when(remoteRes)
                            .getConfiguration();

                        return remote;
                      })
                  .when(remote)
                  .createConfigurationContext(Mockito.any());

              return remote;
            })
        .when(local)
        .createPackageContext(Mockito.any(), Mockito.anyInt());

    Mockito.doAnswer(
            invocation -> {
              final Resources res = Mockito.spy(local.getResources());
              Mockito.doReturn(invocation.getArguments()[0]).when(res).getConfiguration();

              return local;
            })
        .when(local)
        .createConfigurationContext(Mockito.any());

    final Context remote = Mockito.spy(getApplicationContext());
    Mockito.doReturn("com.example.else").when(remote).getPackageName();

    var addOnRemote = new TestableAddOn(local, remote, "id1", "name111", 8);
    Assert.assertNotSame(getApplicationContext(), addOnRemote.getPackageContext());
    Assert.assertSame(remote, addOnRemote.getPackageContext());

    Assert.assertEquals(
        1, addOnRemote.getPackageContext().getResources().getConfiguration().orientation);

    var newConfig = getApplicationContext().getResources().getConfiguration();
    newConfig.orientation = 2;
    addOnRemote.setNewConfiguration(newConfig);
    Assert.assertNotSame(remote, addOnRemote.getPackageContext());
    Assert.assertEquals(remote.getPackageName(), addOnRemote.getPackageContext().getPackageName());
    Assert.assertEquals(
        2, addOnRemote.getPackageContext().getResources().getConfiguration().orientation);
  }

  @Test
  public void testReturnsNullWhenNoSuchPackageAndDoesNotCrash() {
    final Context remote = Mockito.spy(getApplicationContext());
    Mockito.doReturn("com.example.else").when(remote).getPackageName();

    var addOnRemote = new TestableAddOn(remote, "id1", "name111", 8);
    Assert.assertSame(remote, addOnRemote.getPackageContext());

    addOnRemote.setNewConfiguration(
        new Configuration(getApplicationContext().getResources().getConfiguration()));
    Assert.assertNull(addOnRemote.getPackageContext());
  }

  private static class TestableAddOn extends AddOnImpl {

    TestableAddOn(
        Context localContext,
        Context remoteContext,
        CharSequence id,
        CharSequence name,
        int apiVersion) {
      super(
          localContext,
          remoteContext,
          apiVersion,
          id,
          name,
          name.toString() + id.toString(),
          false,
          1);
    }

    TestableAddOn(Context remoteContext, CharSequence id, CharSequence name, int apiVersion) {
      this(getApplicationContext(), remoteContext, id, name, apiVersion);
    }

    TestableAddOn(CharSequence id, CharSequence name, int apiVersion) {
      this(getApplicationContext(), id, name, apiVersion);
    }
  }
}
