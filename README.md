# Plugins for Zettel Notes

Steps to create new plugin

1. Add `maven { url "https://jitpack.io"}` in root level build.gradle repositories
2. Add `implementation 'com.github.damionx7:Zettel-Notes-Plugin-Api:1.0.6'` in build.gradle
3. Add intent-filter in AndroidManifest.xml
```xml
<intent-filter>
    <action android:name="org.eu.thedoc.zettelnotes.intent.buttons" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```
4. Create `Button` class and make it extend `ButtonInterface`
`public class Button extends ButtonInterface`

