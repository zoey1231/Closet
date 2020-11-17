package com.example.frontend;


import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginTest {
    private View decorView;
    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule
            = new ActivityScenarioRule<>(LoginActivity.class);
    @Before
    public void setUp() {
        activityRule.getScenario().onActivity(new ActivityScenario.ActivityAction<LoginActivity>() {
            @Override
            public void perform(LoginActivity activity) {
                decorView = activity.getWindow().getDecorView();
            }
        });
    }
    @Test
    public void loginWithInvalidAccountTest() {

        //we register and unregister idling resources with Espresso to validate asynchronous operations
        // such as send login data to server and wait for response before proceeding to next part of UI test
        CountingIdlingResource componentIdlingResource = LoginActivity.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry = IdlingRegistry.getInstance();
        idlingRegistry.register(componentIdlingResource);

        //check if all components on the Login UI are there
        onView(withId(R.id.etEmail_login)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword_login)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
        onView(withId(R.id.linkToRegister)).check(matches(withText("New here? Create an Account here")));

        //Try to login with invalid account
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.etEmail_login),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()));

        appCompatEditText.perform(replaceText("invalid@invalid.com"), closeSoftKeyboard());


        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.etPassword_login),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                1),
                        isDisplayed()));
        appCompatEditText2.perform(replaceText("123wrong"), closeSoftKeyboard());

        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.btn_login), withText("Login"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                0),
                        isDisplayed()));
        appCompatButton.perform(click());

        //we should not able to be logged in
        //check if a toast message“Invalid credentials, could not log you in.” is present
        onView(withText("Invalid credentials, could not log you in.")).inRoot(withDecorView(not(decorView))).check(matches(withText("Invalid credentials, could not log you in.")));

        //check if all components on the Login UI are there
        onView(withId(R.id.etEmail_login)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword_login)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
        onView(withId(R.id.linkToRegister)).check(matches(withText("New here? Create an Account here")));

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.linkToRegister), withText("New here? Create an Account here"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()));
        appCompatTextView.perform(click());

        //we should now in register activity
        //check if all components on the register UI are there
        onView(withId(R.id.etName)).check(matches(isDisplayed()));
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_register)).check(matches(isDisplayed()));
        onView(withId(R.id.linkToLogin)).check(matches(withText("Already have an account? Login here")));

        //unregister idling resources with Espresso
        idlingRegistry.unregister(componentIdlingResource);

    }

    @Test
    public void loginSuccessTest() {
        //we register and unregister idling resources with Espresso to validate asynchronous operations
        // such as send login data to server and wait for response before proceeding to next part of UI test
        CountingIdlingResource componentIdlingResource = LoginActivity.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry = IdlingRegistry.getInstance();
        idlingRegistry.register(componentIdlingResource);

        //check if all components on the Login UI are there
        onView(withId(R.id.etEmail_login)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword_login)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_login)).check(matches(isDisplayed()));
        onView(withId(R.id.linkToRegister)).check(matches(withText("New here? Create an Account here")));

        //Try to login with valid account
        onView(withId(R.id.etEmail_login)).perform(replaceText("test@test.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword_login)).perform(replaceText("123123"), closeSoftKeyboard());
        onView(withId(R.id.btn_login)).perform(click());

        //we should able to be logged in
        //check if a toast message“Login successfully” is present
        onView(withText("Login successfully")).inRoot(withDecorView(not(decorView))).check(matches(isDisplayed()));

        //check we are in the main screen
        onView((withId(R.id.cl_home_screen))).check((matches(isDisplayed())));

        //unregister idling resources with Espresso
        idlingRegistry.unregister(componentIdlingResource);

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
