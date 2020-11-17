package com.example.frontend;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddClothesStepTest {

    @Rule
    public ActivityTestRule<RegisterActivity> activityRule = new ActivityTestRule<>(RegisterActivity.class);

    @Before
    public void setUp(){

        CountingIdlingResource idlingResourceLogin = LoginActivity.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry = IdlingRegistry.getInstance();
        idlingRegistry.register(idlingResourceLogin);

        onView(allOf(withId(R.id.linkToLogin), withText("Already have an account? Login here"))).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.etEmail_login)).check(matches(isDisplayed())).perform(replaceText("m9test@m9test.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword_login)).check(matches(isDisplayed())).perform(replaceText("123123"), closeSoftKeyboard());
        onView(allOf(withId(R.id.btn_login),withText("Login"))).check(matches(isDisplayed())).perform(click());

        idlingRegistry.unregister(idlingResourceLogin);

//        onView(withId(R.id.mobile_navigation)).perform(NavigationViewActions.navigateTo(R.id.navigation_clothes));
    }

    @Test
    public void addClothesStepTest() {
        int stepcCount = 0;

        onView(withId(R.id.btn_clothes_add)).perform(click());
        stepcCount++;

        CountingIdlingResource idlingResourceAddClothes = AddClothesActivity.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry_activity = IdlingRegistry.getInstance();
        idlingRegistry_activity.register(idlingResourceAddClothes);

        Uri uri = Uri.fromFile(new File("/storage/emulated/0/test.jpg"));
        Intent data = new Intent();
        data.setData(uri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, data);
        Intents.init();
        intending(toPackage("com.google.android.apps.photos")).respondWith(result);
        onView(withId(R.id.btn_image_add)).perform(click());
        intended(toPackage("com.google.android.apps.photos"));
        Intents.release();
        stepcCount++;

        onView(withId(R.id.sp_category_add)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Shirts"))).perform(click());
        onView(withId(R.id.sp_color_add)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("White"))).perform(click());
        onView(withId(R.id.cb_spring_add)).perform(click()).check(matches(isChecked()));
        onView(withId(R.id.sp_occasion_add)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Home"))).perform(click());
        onView(withId(R.id.et_name_add)).perform(replaceText("T-shirt"), closeSoftKeyboard());
        stepcCount++;

        onView(withId(R.id.btn_save_add)).perform(click());
        stepcCount++;

        idlingRegistry_activity.unregister(idlingResourceAddClothes);

        assertTrue("The user should not need more than 5 steps to add a clothes item", stepcCount <= 5);
        System.out.println("The steps performed to add a clothes item is: " + stepcCount);
    }
}
