package com.example.frontend;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.IdlingRegistry;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.filters.LargeTest;

import androidx.test.runner.AndroidJUnit4;
import com.example.frontend.ui.home.HomeFragment;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
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
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

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
        onView(withId(R.id.btn_get_outfit)).check(matches(isEnabled()));

        // click get outfit button and outfit linear layout should display
        onView(withId(R.id.btn_get_outfit)).check(matches(isDisplayed())).perform(click());

        ViewInteraction gridLayout = onView(
                allOf(withId(R.id.gl_outfit),
                        withParent(allOf(withId(R.id.sv_outfit),
                                withParent(withId(R.id.cl_home_screen)))),
                        isDisplayed()));
        gridLayout.check(matches(isDisplayed()));


        //check that the like and dislike buttons are both enabled
        ViewInteraction button = onView(
                allOf(withText("LIKE IT!"),withId(4),
                        isDisplayed()));
        button.check(matches(isEnabled()));


        ViewInteraction button2 = onView(
                allOf(withText("DISLIKE"),withId(5),
                        isDisplayed()));
        button2.check(matches(isEnabled()));

        // click the dislike button, the undo view should be shown
        ViewInteraction button4 = onView(
                allOf(withText("DISLIKE"),withId(5),
                        isDisplayed()));
        button4.perform(click());

        ViewInteraction textView = onView(
                allOf(withText("We will not suggest this outfit any more, OR "),withId(8),
                        isDisplayed()));
        textView.check(matches(withText("We will not suggest this outfit any more, OR ")));


        ViewInteraction textView2 = onView(
                allOf(withText("Undo"),withId(10),
                        isDisplayed()));
        textView2.check(matches(isClickable()));

        //check that like and dislike button now is not enabled

        ViewInteraction button5 = onView(
                allOf(withText("LIKE IT!"),withId(4)));
        button5.check(matches(not(isEnabled())));

        ViewInteraction button6 = onView(
                allOf(withText("DISLIKE"),withId(5)));
        button6.check(matches(not(isEnabled())));

        //click the undo button
        ViewInteraction textView3 = onView(
                allOf(withId(10),
                        isDisplayed()));
        textView3.perform(scrollTo(), click());

        //the undo view should be disappeared from the screen
        ViewInteraction textView5 = onView(
                allOf(withText("We will not suggest this outfit any more, OR "),withId(8)));
        textView5.check(matches(not(isDisplayed())));

        ViewInteraction textView4 =onView(
                allOf(withText("Undo"),withId(10)));
        textView4.check(matches(not(isDisplayed())));

        //check that like and dislike button now is enabled for select again
        ViewInteraction button7 = onView(
                allOf(withText("LIKE IT!"),withId(4),
                        isDisplayed()));
        button7.check(matches(isEnabled()));

        ViewInteraction button8 = onView(
                allOf(withText("DISLIKE"),withId(5),
                        isDisplayed()));
        button8.check(matches(isEnabled()));

        // click like button
        ViewInteraction button9 = onView(
                allOf(withText("LIKE IT!"),withId(4),
                        isDisplayed()));
        button9.perform(scrollTo(),click());

        //check that like and dislike button now is not enabled
        ViewInteraction button10 = onView(
                allOf(withText("LIKE IT!"),withId(4)));
        button10.check(matches(not(isEnabled())));

        ViewInteraction button11 = onView(
                allOf(withText("DISLIKE"),withId(5)));
        button11.check(matches(not(isEnabled())));

        idlingRegistry2.unregister(idlingResourceHome);

    }
    private <T> Matcher<T> first(final Matcher<T> matcher) {
        return new BaseMatcher<T>() {
            boolean isFirst = true;

            @Override
            public boolean matches(final Object item) {
                if (isFirst && matcher.matches(item)) {
                    isFirst = false;
                    return true;
                }
                return false;
            }
            @Override
            public void describeTo(final Description description) {
                description.appendText("should return first matching item");
            }
        };
    }
    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

}
