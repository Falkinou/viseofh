-keep class com.sun.mail.** { *; }
-keep class javax.mail.** { *; }
-keep class javax.activation.** { *; }
-keep class org.apache.commons.net.** { *; }

# DJI MSDK V5 references optional KML parsing classes used by mission import/export.
# Orange DroneKit does not call those KML APIs, so R8 can ignore the optional dom4j dependency.
-dontwarn org.dom4j.**
