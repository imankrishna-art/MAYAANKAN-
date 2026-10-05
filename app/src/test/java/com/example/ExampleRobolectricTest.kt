package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Maya Free", appName)
  }

  @Test
  fun `test local command engine time query`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val engine = com.example.commands.LocalCommandEngine(context)
    val result = engine.evaluate("এখন কয়টা বাজে?")
    assertEquals(true, result.isHandled)
    assertNotNull(result.spokenResponse)
  }
}
