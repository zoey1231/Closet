package com.example.frontend;


import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.Root;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;

import com.example.frontend.ui.home.HomeFragment;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
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
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isClickable;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class OutfitTest {

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
        onView(withId(R.id.etEmail_login)).check(matches(isDisplayed())).perform(replaceText("m9test@m9test.com"), closeSoftKeyboard());
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
        onView(withId(R.id.rl_outfit)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_outfit)).check(matches(isEnabled()));

        // click get outfit button and outfit linear layout should display
        onView(withId(R.id.btn_outfit)).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.rl_outfit)).check(matches(isDisplayed()));

        //check that the like and dislike buttons are both enabled
        onView(withId(R.id.btn_dislike_outfit1)).check(matches(isEnabled()));
        onView(withId(R.id.btn_like_outfit1)).check(matches(isEnabled()));

        // click the dislike button, the undo view should be shown
        onView(withId(R.id.btn_dislike_outfit1)).check(matches(isDisplayed())).perform(scrollTo(), click());
        onView(withId(R.id.view_dislike)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_undo)).check(matches(isEnabled()));
        onView(withId(R.id.tv_undo)).check(matches(isClickable()));
        onView(withId(R.id.tv_will_not_display)).check(matches(withText("We will not suggest this outfit any more, OR ")));

        //check that like and dislike button now is not enabled
        onView(withId(R.id.btn_dislike_outfit1)).check(matches(not(isEnabled())));
        onView(withId(R.id.btn_like_outfit1)).check(matches(not(isEnabled())));

        //click the undo button
        onView(withId(R.id.btn_undo)).check(matches(isDisplayed())).perform(scrollTo(), click());

        //the undo view should be disappeared from the screen
        onView(withId(R.id.view_dislike)).check(matches(not(isDisplayed())));

        //check that like and dislike button now is enabled for select again
        onView(withId(R.id.btn_dislike_outfit1)).check(matches(isEnabled()));
        onView(withId(R.id.btn_like_outfit1)).check(matches(isEnabled()));
        //check the undo view now is gone
        onView(withId(R.id.view_dislike)).check(matches(not(isDisplayed())));

        // click like button
        onView(withId(R.id.btn_like_outfit1)).check(matches(isDisplayed())).perform(scrollTo(), click());

        //check the success toast message is displayed
        onView(withText("Your preference has been recorded")).inRoot(new ToastMatcher())
                .check(matches(withText("Your preference has been recorded")));

        //check that like and dislike button now is not enabled
        onView(withId(R.id.btn_dislike_outfit1)).check(matches(not(isEnabled())));
        onView(withId(R.id.btn_like_outfit1)).check(matches(not(isEnabled())));
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
