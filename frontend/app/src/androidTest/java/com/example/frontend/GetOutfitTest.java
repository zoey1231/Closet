package com.example.frontend;


import android.os.IBinder;

import android.view.WindowManager;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.Root;

import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import androidx.test.runner.AndroidJUnit4;

import com.example.frontend.ui.home.HomeFragment;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class GetOutfitTest {

    @Rule
    public ActivityScenarioRule<RegisterActivity> activityRule
            = new ActivityScenarioRule<>(RegisterActivity.class);
    @Before
    public void setup(){
        //login into the main screen
        CountingIdlingResource idlingResourceLogin = LoginActivity.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry = IdlingRegistry.getInstance();
        idlingRegistry.register(idlingResourceLogin);


        onView(allOf(withId(R.id.linkToLogin), withText("Already have an account? Login here"))).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.etEmail_login)).check(matches(isDisplayed())).perform(replaceText("clothes@clothes.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword_login)).check(matches(isDisplayed())).perform(replaceText("123123"), closeSoftKeyboard());
        onView(allOf(withId(R.id.btn_login),withText("Login"))).check(matches(isDisplayed())).perform(click());

        idlingRegistry.unregister(idlingResourceLogin);

    }
    @Test
    public void outfitTest() {

        CountingIdlingResource idlingResourceHome = HomeFragment.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry2 = IdlingRegistry.getInstance();
        idlingRegistry2.register(idlingResourceHome);

        //On start, check the outfits’ views are not present and get outfit's button is present
        onView(withId(R.id.btn_get_outfit)).check(matches(isEnabled()));

        // click get outfit button and outfit linear layout should display
        onView(withId(R.id.btn_get_outfit)).check(matches(isDisplayed())).perform(click());

        onView(withId(R.id.gl_outfit)).check(matches(isDisplayed()));

        //check that the like and dislike buttons are both enabled

        onView(allOf(withText("DISLIKE"))).check(matches(isEnabled()));
        onView(allOf(withText("LIKE IT!"))).check(matches(isEnabled()));

        // click the dislike button, the undo view should be shown
        onView(allOf(withText("DISLIKE"))).check(matches(isDisplayed())).perform(scrollTo(), click());
        onView(allOf(withText("We will not suggest this outfit any more, OR "))).check(matches(isDisplayed()));
        onView(allOf(withText("Undo"))).check(matches(isClickable()));

        //check that like and dislike button now is not enabled
        onView(allOf(withText("DISLIKE"))).check(matches(not(isEnabled())));
        onView(allOf(withText("LIKE IT!"))).check(matches(not(isEnabled())));

        //click the undo button
        onView(allOf(withText("Undo"))).check(matches(isDisplayed())).perform(scrollTo(), click());

        //the undo view should be disappeared from the screen
        onView(allOf(withText("We will not suggest this outfit any more, OR "))).check(matches(not(isDisplayed())));
        onView(allOf(withText("Undo"))).check(matches(not(isDisplayed())));

        //check that like and dislike button now is enabled for select again
        onView(allOf(withText("DISLIKE"))).check(matches(isEnabled()));
        onView(allOf(withText("LIKE IT!"))).check(matches(isEnabled()));

        // click like button
        onView(allOf(withText("LIKE IT!"))).check(matches(isDisplayed())).perform(scrollTo(), click());

        //check that like and dislike button now is not enabled
        onView(allOf(withText("DISLIKE"))).check(matches(not(isEnabled())));
        onView(allOf(withText("LIKE IT!"))).check(matches(not(isEnabled())));

        idlingRegistry2.unregister(idlingResourceHome);

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
        public void describeTo(Description description) {

            description.appendText("ToastMatcher");
        }
    }
}
