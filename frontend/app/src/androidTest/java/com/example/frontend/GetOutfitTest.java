package com.example.frontend;

import android.view.View;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.android21buttons.fragmenttestrule.FragmentTestRule;
import com.example.frontend.ui.home.HomeFragment;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
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
        CountingIdlingResource componentIdlingResource = HomeFragment.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry = IdlingRegistry.getInstance();
        idlingRegistry.register(componentIdlingResource);

        onView(withId(R.id.btn_outfit)).check(matches(isDisplayed()));
        onView(withId(R.id.ll_outfit)).check(matches((Matcher<? super View>) doesNotExist()));

    }
}
