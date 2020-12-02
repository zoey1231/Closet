package com.example.frontend;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.view.WindowManager;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.Root;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import com.example.frontend.ui.clothes.AddClothesActivity;

import org.hamcrest.TypeSafeMatcher;
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

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddClothesTest {

    @Rule
    public ActivityTestRule<RegisterActivity> activityRule = new ActivityTestRule<>(RegisterActivity.class);

    @Rule public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.READ_EXTERNAL_STORAGE);

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
    }

    @Test
    public void addClothesTest() {

        onView(withId(R.id.navigation_clothes)).perform(click());
        onView(withId(R.id.btn_clothes_add)).perform(click());

        CountingIdlingResource idlingResourceAddClothes = AddClothesActivity.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry_activity = IdlingRegistry.getInstance();
        idlingRegistry_activity.register(idlingResourceAddClothes);

        Uri uri = Uri.fromFile(new File("/storage/emulated/0/Download/test.jpg"));
        Intent data = new Intent();
        data.setData(uri);
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, data);

        Intents.init();
        intending(toPackage("com.google.android.apps.photos")).respondWith(result);
        onView(withId(R.id.btn_image_add)).perform(click());
        intended(toPackage("com.google.android.apps.photos"));
        Intents.release();

        onView(withId(R.id.iv_add)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_image_add)).check(matches(not(isDisplayed())));
        onView(withId(R.id.tv_add)).check(matches(not(isDisplayed())));

        onView(withId(R.id.sp_category_add)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("shirts"))).perform(click());
        onView(withId(R.id.sp_category_add)).check(matches(withSpinnerText(containsString("shirts"))));

        onView(withId(R.id.sp_color_add)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("white"))).perform(click());
        onView(withId(R.id.sp_color_add)).check(matches(withSpinnerText(containsString("white"))));

        onView(withId(R.id.cb_spring_add)).perform(click()).check(matches(isChecked()));

        onView(withId(R.id.sp_occasion_add)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("home"))).perform(click());
        onView(withId(R.id.sp_occasion_add)).check(matches(withSpinnerText(containsString("home"))));

        onView(withId(R.id.et_name_add)).perform(replaceText("T-shirt"), closeSoftKeyboard());

        onView(withId(R.id.btn_save_add)).perform(click());
        onView(withText("Successfully added clothes!")).inRoot(new ToastMatcher()).check(matches(withText("Successfully added clothes!")));

        idlingRegistry_activity.unregister(idlingResourceAddClothes);
    }

    class ToastMatcher extends TypeSafeMatcher<Root> {

        @Override
        public boolean matchesSafely(Root root) {
            int type = root.getWindowLayoutParams().get().type;
            if ((type == WindowManager.LayoutParams.TYPE_TOAST)) {
                IBinder windowToken = root.getDecorView().getWindowToken();
                IBinder appToken = root.getDecorView().getApplicationWindowToken();
                if (windowToken == appToken) {
                    return true;
                    //means this window isn't contained by any other windows.
                }
            }
            return false;
        }

        @Override
        public void describeTo(org.hamcrest.Description description) {
            description.appendText("ToastMatcher");
        }
    }
}


