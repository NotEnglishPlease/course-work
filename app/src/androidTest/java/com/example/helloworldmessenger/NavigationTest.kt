package com.example.helloworldmessenger

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.Navigation
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.helloworldmessenger.fragments.LogInFragment
import com.example.helloworldmessenger.fragments.SignUpFragment
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @Test
    fun navigationToSignUpScreen() {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        val loginScenario = launchFragmentInContainer<LogInFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )

        loginScenario.onFragment { fragment ->
            navController.setGraph(R.navigation.entrance_nav_graph)
            navController.setCurrentDestination(R.id.logInFragment)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.registerButton))
            .perform(click())

        assertThat(navController.currentDestination?.id, equalTo(R.id.signUpFragment))
    }

    @Test
    fun navigationToLogInScreen() {
        val navController = TestNavHostController(
            ApplicationProvider.getApplicationContext()
        )
        val sigUpScenario = launchFragmentInContainer<SignUpFragment>(
            themeResId = R.style.Theme_HelloWorldMessenger
        )

        sigUpScenario.onFragment { fragment ->
            navController.setGraph(R.navigation.entrance_nav_graph)
            navController.setCurrentDestination(R.id.signUpFragment)
            Navigation.setViewNavController(fragment.requireView(), navController)
        }

        onView(withId(R.id.loginButton))
            .perform(click())

        assertThat(navController.currentDestination?.id, equalTo(R.id.logInFragment))
    }
}