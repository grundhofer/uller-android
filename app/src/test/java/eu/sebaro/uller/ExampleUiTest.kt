package eu.sebaro.uller

import io.appium.java_client.MobileBy
import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.remote.AndroidMobileCapabilityType
import io.appium.java_client.remote.MobileCapabilityType
import org.junit.After
import org.junit.Test
import org.junit.Before
import org.openqa.selenium.remote.DesiredCapabilities
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUiTest {

    private var LOCAL_URL = "http://127.0.0.1:4723/wd/hub"
    private var ANDROID_PACKAGE = "eu.sebaro.uller"
    private var ANDROID_ACTIVITY = "eu.sebaro.uller.MainActivity"
    var APK_PATH = "/Users/s.grundhoefer/AndroidStudioProjects/UllerApp/app/build/outputs/apk/debug/app-debug.apk"
    var DEVICE_ANDROID_UDID_9T = "7f76563c"
    var DEVICE_ANDROID_NAME_9T = "Mi 9T Pro"
    lateinit var androidDriver: AndroidDriver<MobileElement>

    fun createAndroidDriver(): AndroidDriver<MobileElement> {
    val caps = DesiredCapabilities()

    caps.setCapability(MobileCapabilityType.PLATFORM_NAME, "Android")
    caps.setCapability(MobileCapabilityType.UDID, DEVICE_ANDROID_UDID_9T)
    caps.setCapability(MobileCapabilityType.DEVICE_NAME, DEVICE_ANDROID_NAME_9T)
    caps.setCapability(AndroidMobileCapabilityType.AUTO_GRANT_PERMISSIONS, true)
    caps.setCapability(AndroidMobileCapabilityType.APP_PACKAGE, ANDROID_PACKAGE)
    caps.setCapability(AndroidMobileCapabilityType.APP_ACTIVITY, ANDROID_ACTIVITY)
    caps.setCapability(MobileCapabilityType.APP, APK_PATH)
    var appiumUrl: String = LOCAL_URL
    return AndroidDriver(URL(appiumUrl), caps)
}
    @Before
    fun setupTest() {
        androidDriver = createAndroidDriver()
        androidDriver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS)
    }

    @Test
    fun openWebview() {
        Thread.sleep(1000)
        androidDriver.findElement(MobileBy.AccessibilityId("shop Playstation 5")).click()
        println("PS5 clicked")
        Thread.sleep(1000)
        androidDriver.findElement(MobileBy.AccessibilityId("Back")).click()
        println("Back button to main menu clicked")
        Thread.sleep(1000)
        androidDriver.findElement(MobileBy.AccessibilityId("subscribe Playstation 5")).click()
        println("Subscribe to PS5 notification")
        Thread.sleep(1000)
    }

    @After
    fun endTest() {
        androidDriver.quit()
    }
}