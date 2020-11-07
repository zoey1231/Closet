package com.example.frontend;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.android21buttons.fragmenttestrule.FragmentTestRule;
import com.example.frontend.ui.home.HomeFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class GetOutfitTest {

    @Rule
    public FragmentTestRule<?, HomeFragment> fragmentTestRule =
            FragmentTestRule.create(HomeFragment.class);

    @Test
    public void GetOutfitTest() {
        CountingIdlingResource idlingResource = HomeFragment.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry = IdlingRegistry.getInstance();
        idlingRegistry.register(idlingResource);

        // click get outfit button and outfit linear layout should display
        onView(withId(R.id.btn_outfit)).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.ll_outfit1)).check(matches(isDisplayed()));

        // click like button
        onView(withId(R.id.btn_like_outfit1)).check(matches(isDisplayed())).perform(click());
        //todo

        // click dislike button
        onView(withId(R.id.btn_dislike_outfit1)).check(matches(isDisplayed())).perform(click());
        //todo

        idlingRegistry.unregister(idlingResource);
    }

}
