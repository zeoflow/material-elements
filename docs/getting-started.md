<!--docs:
title: "Getting Started"
layout: landing
section: docs
path: /docs/getting-started/
-->

### 1. Depend on our library

Material Elements for Android is available through Google's Maven Repository.
To use it:

1.  Open the `build.gradle` file for your application.
2.  Make sure that the `repositories` section includes Maven Repository
    `google()`. For example:

    ```groovy
      allprojects {
        repositories {
          google()
          jcenter()
        }
      }
    ```

3.  Add the library to the `dependencies` section:

    ```groovy
      dependencies {
        // ...
        implementation 'com.zeoflow:material-elements:x.y.z'
        // ...
      }
    ```

Visit [MVN Repository](https://mvnrepository.com/artifact/com.zeoflow/material-elements)
to find the latest version of the library.

##### New Namespace and AndroidX

If your app currently depends on the original Design Support Library, you can
make use of the
[`Refactor to AndroidX…`](https://developer.android.com/jetpack/androidx/migrate)
option provided by Android Studio. Doing so will update your app's dependencies
and code to use the newly packaged `androidx` and `com.zeoflow:material-elements`
libraries.

### 2. Compile your app with Android 10

In order to use Material Elements for Android, and the latest versions of the
Support Libraries, you will have to install Android Studio 3.5 or higher to
build with Android 10, and update your app's `compileSdkVersion` to `30`.

### 3. Ensure you are using `AppCompatActivity`

Using `AppCompatActivity` will ensure that all the elements work correctly. If
you are unable to extend from `AppCompatActivity`, update your activities to use
`AppCompatDelegate`. This will enable the `AppCompat` versions of elements to
be inflated among other important things.

### 4. Change your app theme to inherit from a Material Elements theme

Doing an app-wide migration by changing your app theme to inherit from a
Material Elements theme is the recommended approach. However, be sure to test
thoroughly afterwards, as elements in existing layouts may change their looks
and behavior.

Note: If you **can't** change your theme, you can do one of the following:

*   Inherit from one of our Material Elements **Bridge** themes. See the
    [**Bridge Themes**](#bridge-themes) section for more details.
*   Continue to inherit from an AppCompat theme and add some new theme
    attributes to your theme. See the
    [**App Compat Themes**](#app-compat-themes) section for more details.

#### **Material Elements themes**

The following is the list of Material Elements themes you can use to get the
latest component styles and theme-level attributes.

*   `Theme.MaterialElements`
*   `Theme.MaterialElements.NoActionBar`
*   `Theme.MaterialElements.Light`
*   `Theme.MaterialElements.Light.NoActionBar`
*   `Theme.MaterialElements.Light.DarkActionBar`
*   `Theme.MaterialElements.DayNight`
*   `Theme.MaterialElements.DayNight.NoActionBar`
*   `Theme.MaterialElements.DayNight.DarkActionBar`

Update your app theme to inherit from one of these themes, e.g.:

```xml
<style name="Theme.MyApp" parent="Theme.MaterialElements.DayNight">
    <!-- ... -->
</style>
```

For more information on how to set up theme-level attributes for your app, take
a look at our [Theming](theming.md) guide, as well as our
[Dark Theme](theming/Dark.md) guide for why it's important to inherit from the
`DayNight` theme.

Note: Using a Material Elements theme enables a custom view inflater which
replaces default elements with their Material counterparts. Currently, this
only replaces `<Button>` and `<AutoCompleteTextView>` XML elements with
[`<MaterialButton>`](elements/Button.md) and
[`<MaterialAutoCompleteTextView>`](https://github.com/material-elements/material-elements-android/blob/master/material-elements/java/com/zeoflow/material/elements/textfield/MaterialAutoCompleteTextView.java),
respectively.

#### **Bridge Themes** {#bridge-themes}

If you cannot change your theme to inherit from a Material Elements theme, you
can inherit from a Material Elements **Bridge** theme.

```xml
<style name="Theme.MyApp" parent="Theme.MaterialElements.Light.Bridge">
    <!-- ... -->
</style>
```

Both `Theme.MaterialElements` and `Theme.MaterialElements.Light` have
`.Bridge` themes:

*   `Theme.MaterialElements.Bridge`
*   `Theme.MaterialElements.Light.Bridge`
*   `Theme.MaterialElements.NoActionBar.Bridge`
*   `Theme.MaterialElements.Light.NoActionBar.Bridge`
*   `Theme.MaterialElements.Light.DarkActionBar.Bridge`

Bridge themes inherit from AppCompat themes, but also define the new Material
elements theme attributes for you. If you use a bridge theme, you can start
using Material Design elements without changing your app theme.

#### **AppCompat Themes** {#app-compat-themes}

You can also incrementally test new Material Elements without changing your
app theme. This allows you to keep your existing layouts looking and behaving
the same, while introducing new elements to your layout one at a time.

However, you must add the following new theme attributes to your existing app
theme, or you will encounter `ThemeEnforcement` errors:

```xml
<style name="Theme.MyApp" parent="Theme.AppCompat">

  <!-- Original AppCompat attributes. -->
  <item name="colorPrimary">@color/my_app_primary_color</item>
  <item name="colorSecondary">@color/my_app_secondary_color</item>
  <item name="android:colorBackground">@color/my_app_background_color</item>
  <item name="colorError">@color/my_app_error_color</item>

  <!-- New MaterialElements attributes. -->
  <item name="colorPrimaryVariant">@color/my_app_primary_variant_color</item>
  <item name="colorSecondaryVariant">@color/my_app_secondary_variant_color</item>
  <item name="colorSurface">@color/my_app_surface_color</item>
  <item name="colorOnPrimary">@color/my_app_color_on_primary</item>
  <item name="colorOnSecondary">@color/my_app_color_on_secondary</item>
  <item name="colorOnBackground">@color/my_app_color_on_background</item>
  <item name="colorOnError">@color/my_app_color_on_error</item>
  <item name="colorOnSurface">@color/my_app_color_on_surface</item>
  <item name="scrimBackground">@color/mtrl_scrim_color</item>
  <item name="textAppearanceHeadline1">@style/TextAppearance.MaterialElements.Headline1</item>
  <item name="textAppearanceHeadline2">@style/TextAppearance.MaterialElements.Headline2</item>
  <item name="textAppearanceHeadline3">@style/TextAppearance.MaterialElements.Headline3</item>
  <item name="textAppearanceHeadline4">@style/TextAppearance.MaterialElements.Headline4</item>
  <item name="textAppearanceHeadline5">@style/TextAppearance.MaterialElements.Headline5</item>
  <item name="textAppearanceHeadline6">@style/TextAppearance.MaterialElements.Headline6</item>
  <item name="textAppearanceSubtitle1">@style/TextAppearance.MaterialElements.Subtitle1</item>
  <item name="textAppearanceSubtitle2">@style/TextAppearance.MaterialElements.Subtitle2</item>
  <item name="textAppearanceBody1">@style/TextAppearance.MaterialElements.Body1</item>
  <item name="textAppearanceBody2">@style/TextAppearance.MaterialElements.Body2</item>
  <item name="textAppearanceCaption">@style/TextAppearance.MaterialElements.Caption</item>
  <item name="textAppearanceButton">@style/TextAppearance.MaterialElements.Button</item>
  <item name="textAppearanceOverline">@style/TextAppearance.MaterialElements.Overline</item>

</style>
```

### 5. Add a Material component to your app
#### **Implementing a text field via XML**

The default text field XML is defined as:

```xml
<com.zeoflow.material.elements.textfield.TextInputLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/textfield_label">

  <com.zeoflow.material.elements.textfield.TextInputEditText
      android:layout_width="match_parent"
      android:layout_height="wrap_content"/>
</com.zeoflow.material.elements.textfield.TextInputLayout>
```

Note: If you are **not** using a theme that inherits from a Material Elements
theme, you will have to specify the text field style as well, via
`style="@style/MaterialElements.TextInputLayout.FilledBox"`

Other text field styles are also provided. For example, if you want an
[outlined text field](https://material.io/go/design-text-fields#outlined-text-field)
in your layout, you can apply the Material Elements `outlined` style to the
text field in XML:

```xml
<com.zeoflow.material.elements.textfield.TextInputLayout
    style="@style/MaterialElements.TextInputLayout.OutlinedBox"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:hint="@string/textfield_label">

  <com.zeoflow.material.elements.textfield.TextInputEditText
      android:layout_width="match_parent"
      android:layout_height="wrap_content"/>
</com.zeoflow.material.elements.textfield.TextInputLayout>
```

## Contributors

Material Elements for Android welcomes contributions from the community. Check
out our [contributing guidelines](contributing.md) as well as an overview of the
[directory structure](directorystructure.md) before getting started.