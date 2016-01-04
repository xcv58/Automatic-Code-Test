package com.xcv58.automatic;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.test.ActivityInstrumentationTestCase2;

import com.xcv58.automatic.activities.MainActivity;
import com.xcv58.automatic.activities.TripFragment;
import com.xcv58.automatic.utils.Utils;

import org.junit.Before;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.registerIdlingResources;
import static android.support.test.espresso.Espresso.unregisterIdlingResources;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;

/**
 * Created by xcv58 on 1/1/16.
 */
public class EspressoTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private MainActivity mMainActivity;
    private TripFragment tripFragment;
    private ProgressIdlingResource resource;

    public EspressoTest() {
        super(MainActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        mMainActivity = getActivity();

        tripFragment = (TripFragment) mMainActivity.getSupportFragmentManager()
                .findFragmentById(R.id.fragment_list);
        resource = new ProgressIdlingResource(tripFragment);
        registerIdlingResources(resource);
    }

    public void testToken() {
        Utils.log("test token");
        onView(withClassName(endsWith("EditText")))
                .check(matches(withHint(R.string.first_time_hint)))
                .perform(replaceText(BuildConfig.TOKEN), closeSoftKeyboard());
        onView(allOf(withClassName(endsWith("MDButton")), withText("OK")))
                .perform(click());
        int size = tripFragment.getTripList().size();
        assertTrue("Not empty list", size > 0);

        Utils.log("test endless scroll");
        for (int n = 10; n > 0; n--) {
            size = tripFragment.getTripList().size();
            onView(withId(R.id.recycler_view)).perform(
                    RecyclerViewActions.actionOnItemAtPosition(size - 2, scrollTo()));
            onView(withId(R.id.recycler_view)).perform(swipeUp());
            int newSize = tripFragment.getTripList().size();
            assertTrue("Load more trips", newSize > size);
        }
        unregisterIdlingResources(resource);
    }

    public void testInvalidToken() {
        Utils.log("test invalid token");
    }
}
