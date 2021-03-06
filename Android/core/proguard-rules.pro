-injars libs(!META-INF/MANIFEST.MF)

-keepattributes InnerClasses,Signature,Annotation,EnclosingMethod

-keep public interface com.zopim.android.sdk.** { *; }
-keep public class com.zopim.android.sdk.** { *; }
-keep public interface com.fasterxml.jackson.** { *; }
-keep public class com.fasterxml.jackson.** { *; }
-keep interface android.support.v7.** { *; }
-keep class android.support.v7.** { *; }
-keep public interface com.zopim.android.sdk.** { *; }
-keep public class com.zopim.android.sdk.** { *; }
-keep class org.apache.**
-keep class org.apache.** { *; }
-keep class com.google.android.gms.**
-keep class com.google.android.gms.** { *; }
-keep class com.ibuildapp.**
-keep class com.ibuildapp.** { *; }
-keep class com.appbuilder.**
-keep class com.appbuilder.** { *; }
-keep class com.squareup.**
-keep class com.squareup.** { *; }
-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep class com.restfb.**
-keep class com.restfb.** { *; }
-keep class java.nio.**
-keep class java.nio.** { *; }
-keep class java.net.**
-keep class java.net.** { *; }
-keep class org.codehaus.**
-keep class org.codehaus.** { *; }
-keep class android.support.v4.view.PagerAdapter
-keep class android.support.v4.view.ViewPager$OnAdapterChangeListener
-keep class android.support.v4.view.ViewPager$OnPageChangeListener
-keep class android.support.v4.widget.DrawerLayout$DrawerListener
-keep class android.support.v4.widget.SlidingPaneLayout$PanelSlideListener
-keep class android.support.v4.widget.SwipeRefreshLayout$OnRefreshListener
-keep class com.appbuilder.sdk.android.AppAdvView$OnAdClosedListener
-keep class com.appbuilder.sdk.android.OnSwipeInterface
-keep class com.millennialmedia.android.MMAdView$MMAdListener
-keep class com.smaato.soma.AdSettings
-keep class com.smaato.soma.VideoListener
-keep class io.card.payment.DetectionInfo
-keep class twitter4j.**
-keep class twitter4j.** { *; }
-keep class retrofit.**
-keep class retrofit.** { *; }
-keep class com.**
-keep class com.** { *; }

-dontnote com.paypal.**
-dontnote com.ibuildapp.**
-dontnote com.appbuilder.**

-dontwarn twitter4j.**
-dontwarn com.ibuildapp.**
-dontwarn java.nio.**
-dontwarn org.codehaus.**
-dontwarn com.squareup.**
-dontwarn javax.management.**
-dontwarn org.apache.http.**
-dontwarn org.apache.log4j.**
-dontwarn org.slf4j.**
-dontwarn io.vov.vitamio.**
-dontwarn android.webkit.WebSettings
-dontwarn java.lang.management.ManagementFactory
-dontwarn org.apache.commons.logging.LogFactory
-dontwarn com.fasterxml.jackson.databind.ext.DOMSerializer
-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry
 -ignorewarnings
 -keep class * {
     public private *; }