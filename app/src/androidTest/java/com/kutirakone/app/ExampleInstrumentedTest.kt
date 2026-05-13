package com.kutirakone.app

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
<<<<<<< HEAD

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
=======
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

>>>>>>> d94e41b (Initial project upload)
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
<<<<<<< HEAD
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
=======
        val appContext = InstrumentationRegistry
            .getInstrumentation().targetContext
>>>>>>> d94e41b (Initial project upload)
        assertEquals("com.kutirakone.app", appContext.packageName)
    }
}