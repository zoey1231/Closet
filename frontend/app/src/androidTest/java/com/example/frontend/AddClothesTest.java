package com.example.frontend;

import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.idling.CountingIdlingResource;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.android21buttons.fragmenttestrule.FragmentTestRule;
import com.example.frontend.ui.clothes.ClothesFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.fail;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddClothesTest {

    @Rule
    public FragmentTestRule<?, ClothesFragment> fragmentRule =
            FragmentTestRule.create(ClothesFragment.class);
    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule
            = new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void addClothesTest() {
        CountingIdlingResource idlingResource_fragment = ClothesFragment.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry_fragment = IdlingRegistry.getInstance();
        idlingRegistry_fragment.register(idlingResource_fragment);

        // click add clothes button and we should be navigate to add clothes activity
        onView(withId(R.id.btn_clothes_add)).check(matches(isDisplayed())).perform(click());

        CountingIdlingResource idlingResource_activity = ClothesFragment.getRegisterIdlingResourceInTest();
        IdlingRegistry idlingRegistry_activity = IdlingRegistry.getInstance();
        idlingRegistry_activity.register(idlingResource_activity);

        // click add image button and when we come back from gallery, image should display and add image button should be invisible
        onView(withId(R.id.btn_image_add)).check(matches(isDisplayed())).perform(click());
        onView(withId(R.id.iv_add)).check(matches(isDisplayed()));
//        onView(withId(R.id.btn_image_add)).check(matches((Matcher<? super View>) doesNotExist()));

        // select "Shirts" in category spinner
        onView(withId(R.id.sp_category_add)).check(matches(isDisplayed())).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Shirts"))).perform(click());
        onView(withId(R.id.sp_category_add)).check(matches(withSpinnerText(containsString("Shirts"))));

        // select "White" in color spinner
        onView(withId(R.id.sp_color_add)).check(matches(isDisplayed())).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("White"))).perform(click());
        onView(withId(R.id.sp_color_add)).check(matches(withSpinnerText(containsString("White"))));

        // click save clothes button and we should get "Missing clothes values" message
        onView(withId(R.id.btn_save_add)).check(matches(isDisplayed())).perform(click());
//        onView(withText("Missing clothes values")).inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));

        onView(withId(R.id.cb_spring_add)).check(matches(isDisplayed())).check(matches(isNotChecked())).perform(click());
        onView(withId(R.id.cb_summer_add)).check(matches(isDisplayed())).check(matches(isNotChecked())).perform(click());

        onView(withId(R.id.sp_category_add)).check(matches(isDisplayed())).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Shirts"))).perform(click());
        onView(withId(R.id.sp_category_add)).check(matches(withSpinnerText(containsString("Shirts"))));

        // added for codacy issue
        fail();

    }

}
