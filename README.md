# Plugins for Zettel Notes

[![](https://jitpack.io/v/damionx7/Zettel-Notes-Plugin-Api.svg)](https://jitpack.io/#damionx7/Zettel-Notes-Plugin-Api)

Steps to create new plugin

1. Add `maven { url "https://jitpack.io"}` in build.gradle repositories (read more at https://jitpack.io/)
2. Add `implementation 'com.github.damionx7:Zettel-Notes-Plugin-Api:1.0.21'` in build.gradle
3. Add intent-filter in AndroidManifest.xml

```xml
<intent-filter>
    <action android:name="org.eu.thedoc.zettelnotes.intent.buttons" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

4. Create `Button` class and make it extend `ButtonInterface`

```
public class Button
    extends ButtonInterface {

  private final Listener mListener = new Listener() {
    @Override
    public void onClick() {
      if (mCallback != null) {
        mCallback.insertText("\uD83D\uDE04");
      }
    }

    @Override
    public boolean onLongClick() {
      if (mCallback != null) {
        String selectedText = mCallback.getTextSelected(false);
        if (!selectedText.isEmpty()) {
          mCallback.replaceTextSelected("\uD83D\uDE04");
          return true;
        }
      }
      return false;
    }

  };

  @Override
  public String getName() {
    return "Plugin";
  }

  @Override
  public Listener getListener() {
    return mListener;
  }
}
```

