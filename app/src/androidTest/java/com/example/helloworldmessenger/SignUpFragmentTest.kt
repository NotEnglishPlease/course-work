package com.example.helloworldmessenger

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.helloworldmessenger.fragments.SignUpFragment
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpFragmentTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun invalidEmailTest() {
        launchFragmentInContainer<SignUpFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid email
        onView(withId(R.id.inputEmailEditText))
            .perform(typeText("invalid_email"), closeSoftKeyboard())
        // Check if the error message is displayed
        onView(withId(R.id.inputEmailLayout))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.invalid_email))))
    }

    @Test
    fun invalidPasswordLengthLessTest() {
        launchFragmentInContainer<SignUpFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid password
        onView(withId(R.id.inputPasswordEditText))
            .perform(typeText("12345"), closeSoftKeyboard())
        // Check if the error message is displayed
        onView(withId(R.id.inputPasswordLayout))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.password_length_error))))
    }

    @Test
    fun invalidPasswordLengthMoreTest() {
        launchFragmentInContainer<SignUpFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid password
        onView(withId(R.id.inputPasswordEditText))
            .perform(typeText("1234567890123456789012345"), closeSoftKeyboard())
        // Check if the error message is displayed
        onView(withId(R.id.inputPasswordLayout))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.password_length_error))))
    }

    @Test
    fun invalidPasswordConfirmTest() {
        launchFragmentInContainer<SignUpFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid password
        onView(withId(R.id.inputPasswordEditText))
            .perform(
                typeText("123456"), closeSoftKeyboard()
            )
        // Type invalid password confirm
        onView(withId(R.id.inputConfirmPasswordEditText))
            .perform(typeText("1234567"), closeSoftKeyboard())
        // Check if the error message is displayed
        onView(withId(R.id.inputConfirmPasswordLayout))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.confirm_password_error))))
    }

    @Test
    fun invalidUsernameLengthMoreTest() {
        launchFragmentInContainer<SignUpFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid username
        onView(withId(R.id.inputNameEditText))
            .perform(
                typeText("12345813829178174987198247912749817284192749124"),
                closeSoftKeyboard()
            )
        // Check if the error message is displayed
        onView(withId(R.id.inputNameLayout))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.name_too_long))))
    }

    @Test
    fun invalidPasswordSymbolsTest() {
        launchFragmentInContainer<SignUpFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )

        // Type invalid password
        onView(withId(R.id.inputPasswordEditText))
            .perform(
                typeText("123456!@#$!#@#!"), closeSoftKeyboard()
            )
        // Check if the error message is displayed
        onView(withId(R.id.inputPasswordLayout))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.password_invalid_characters_error))))
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