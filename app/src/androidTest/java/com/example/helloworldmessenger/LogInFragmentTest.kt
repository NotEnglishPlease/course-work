package com.example.helloworldmessenger

import android.view.View
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.helloworldmessenger.fragments.LogInFragment
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LogInFragmentTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun invalidEmailTest() {
        launchFragmentInContainer<LogInFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid email
        onView(withId(R.id.loginInputEmailEditText))
            .perform(typeText("invalid_email"))
        // Check if the error message is displayed
        onView(withId(R.id.loginInputEmail))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.invalid_email))))
    }


    @Test
    fun invalidPasswordLengthLessTest() {
        launchFragmentInContainer<LogInFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid password
        onView(withId(R.id.loginInputPasswordEditText))
            .perform(typeText("12345"))
        // Check if the error message is displayed
        onView(withId(R.id.loginInputPassword))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.password_length_error))))
    }

    @Test
    fun invalidPasswordLengthMoreTest() {
        launchFragmentInContainer<LogInFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid password
        onView(withId(R.id.loginInputPasswordEditText))
            .perform(typeText("1234567890123456789012345"))
        // Check if the error message is displayed
        onView(withId(R.id.loginInputPassword))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.password_length_error))))
    }

    @Test
    fun invalidPasswordSymbolsTest() {
        launchFragmentInContainer<LogInFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid password
        onView(withId(R.id.loginInputPasswordEditText))
            .perform(typeText("1234567890!@#$%^&*()"))
        // Check if the error message is displayed
        onView(withId(R.id.loginInputPassword))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.password_invalid_characters_error))))
    }

    @Test
    fun invalidPasswordEmptyTest() {
        launchFragmentInContainer<LogInFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type valid email
        onView(withId(R.id.loginInputEmailEditText))
            .perform(typeText("valid@email.com"))
        // Type invalid password
        onView(withId(R.id.loginInputPasswordEditText))
            .perform(typeText(""))

        onView(withId(R.id.logInButton))
            .perform(click())
        // Check if the error message is displayed
        onView(withId(R.id.loginInputPassword))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.password_error))))
    }

    @Test
    fun invalidEmailEmptyTest() {
        launchFragmentInContainer<LogInFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid email
        onView(withId(R.id.loginInputEmailEditText))
            .perform(typeText(""))
        onView(withId(R.id.logInButton))
            .perform(click())
        // Check if the error message is displayed
        onView(withId(R.id.loginInputEmail))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.email_error))))
    }

    @Test
    fun invalidEmailAndPasswordEmptyTest() {
        launchFragmentInContainer<LogInFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )
        // Type invalid email
        onView(withId(R.id.loginInputEmailEditText))
            .perform(typeText(""))
        // Type invalid password
        onView(withId(R.id.loginInputPasswordEditText))
            .perform(typeText(""))
        onView(withId(R.id.logInButton))
            .perform(click())
        // Check if the error message is displayed
        onView(withId(R.id.loginInputEmail))
            .check(matches(hasTextInputLayoutErrorText(context.getString(R.string.email_error))))
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