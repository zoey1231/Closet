package com.example.frontend;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddClothesTest {

    @Rule
    public ActivityScenarioRule<AddClothesActivity> activityRule
            = new ActivityScenarioRule<>(AddClothesActivity.class);

    @Test
    public void AddClothesTest() {
        CountingIdlingResource componentIdlingResource = AddClothesActivity.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry = IdlingRegistry.getInstance();
        idlingRegistry.register(componentIdlingResource);

        onView(withId(R.id.btn_image_add)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_save_add)).check(matches(isDisplayed()));
        onView(withId(R.id.sp_category_add)).check(matches(isDisplayed()));
        onView(withId(R.id.sp_color_add)).check(matches(isDisplayed()));
        onView(withId(R.id.sp_occasion_add)).check(matches(isDisplayed()));
        onView(withId(R.id.cb_spring_add)).check(matches(isDisplayed()));
        onView(withId(R.id.cb_summer_add)).check(matches(isDisplayed()));
        onView(withId(R.id.cb_fall_add)).check(matches(isDisplayed()));
        onView(withId(R.id.cb_winter_add)).check(matches(isDisplayed()));
        onView(withId(R.id.cb_all_add)).check(matches(isDisplayed()));
        onView(withId(R.id.et_name_add)).check(matches(isDisplayed()));


    }
}
