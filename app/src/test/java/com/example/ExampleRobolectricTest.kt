package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ui.GameUiState
import com.example.ui.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("拼音字词精灵", appName)
  }

  @Test
  fun `game viewmodel initialization and state change`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = GameViewModel(app)
    
    // Check initial state
    assertEquals(GameUiState.Welcome, viewModel.uiState.value)
    assertEquals(0, viewModel.fruitsCount.value)
  }
}
