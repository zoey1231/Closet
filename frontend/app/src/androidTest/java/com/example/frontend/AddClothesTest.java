package com.example.frontend;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static android.content.ContentValues.TAG;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.hasBackground;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddClothesTest {

    @Rule
    public ActivityTestRule<AddClothesActivity> activityRule = new ActivityTestRule<>(AddClothesActivity.class);

    @Rule public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE);

    private int count = 1;

    @Test
    public void addClothesTest() {

        CountingIdlingResource idlingResource_activity = AddClothesActivity.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry_activity = IdlingRegistry.getInstance();
        idlingRegistry_activity.register(idlingResource_activity);

        Uri uri = Uri.fromFile(new File("/storage/emulated/0/test.jpg"));
        Intent data = new Intent();
        data.setData(uri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, data);

        Intents.init();
        intending(toPackage("com.google.android.apps.photos")).respondWith(result);
        onView(withId(R.id.btn_image_add)).perform(click());
        countStep();
        intended(toPackage("com.google.android.apps.photos"));
        Intents.release();

        onView(withId(R.id.iv_add)).check(matches(isDisplayed()));
        ImageView image = activityRule.getActivity().findViewById(R.id.iv_add);
        assertTrue(image.getDrawable() != null);
        onView(withId(R.id.btn_image_add)).check(matches(not(isDisplayed())));
        onView(withId(R.id.tv_add)).check(matches(not(isDisplayed())));

        onView(withId(R.id.sp_category_add)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Shirts"))).perform(click());
        onView(withId(R.id.sp_category_add)).check(matches(withSpinnerText(containsString("Shirts"))));

        onView(withId(R.id.sp_color_add)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("White"))).perform(click());
        onView(withId(R.id.sp_color_add)).check(matches(withSpinnerText(containsString("White"))));

        onView(withId(R.id.cb_spring_add)).perform(click()).check(matches(isChecked()));

        onView(withId(R.id.sp_occasion_add)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Home"))).perform(click());
        onView(withId(R.id.sp_occasion_add)).check(matches(withSpinnerText(containsString("Home"))));

        onView(withId(R.id.et_name_add)).perform(replaceText("T-shirt"), closeSoftKeyboard());
        countStep();

//        onView(withId(R.id.btn_save_add)).perform(click());
        countStep();
//        onView(withText("Successfully added clothes!")).inRoot(withDecorView(not(is(activityRule.getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));

        idlingRegistry_activity.unregister(idlingResource_activity);

        Log.d(TAG, "Steps to add clothes: " + count);
    }

    private void countStep() {
        count++;
    }

}
