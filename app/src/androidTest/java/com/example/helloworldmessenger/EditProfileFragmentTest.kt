package com.example.helloworldmessenger

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.helloworldmessenger.fragments.EditProfileFragment
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditProfileFragmentTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun tooLongNameTest() {
        launchFragmentInContainer<EditProfileFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid name
        onView(withId(R.id.nameEditText))
            .perform(typeText("12345678901234567890123451234567890123456789012345"))
        // Check if the error message is displayed
        onView(withId(R.id.textInputLayout))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.name_too_long))))
    }

    private fun hasTextInputLayoutErrorText(expectedErrorText: String): Matcher<View> {
        return object : TypeSafeMatcher<View>() {

            override fun matchesSafely(item: View?): Boolean {
                if (item !is TextInputLayout) {
                    return false
                }
                val error = item.error ?: return false
                val errorText = error.toString()
                return expectedErrorText == errorText
            }

            override fun describeTo(description: org.hamcrest.Description?) {
                description?.appendText("Error text: $expectedErrorText")
            }
        }
    }
}