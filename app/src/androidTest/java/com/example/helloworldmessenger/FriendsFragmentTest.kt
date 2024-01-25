package com.example.helloworldmessenger

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.helloworldmessenger.fragments.FriendsFragment
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class FriendsFragmentTest {

    @Test
    fun emptySearch() {

        launchFragmentInContainer<FriendsFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        onView(withId(R.id.searchBar))
            .perform(click())
        typeText("")
        onView(withId(R.id.emptySearchResultsTextView))
            .check(matches(isDisplayed()))
    }
}