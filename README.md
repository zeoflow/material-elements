# Material Elements for Android
<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[![All Contributors](https://img.shields.io/badge/all_contributors-1-orange.svg?style=flat-square)](#contributors-)
<!-- ALL-CONTRIBUTORS-BADGE:END -->

##  Intro
Material Elements help developers execute
Material Elements. Developed by a core team of
engineers and UX designers, these elements enable a reliable
development workflow to build beautiful and functional Android apps.

## Getting Started
For information on how to get started with Material Elements,
take a look at our [Getting Started](docs/getting-started.md) guide.

## Submitting Bugs or Feature Requests
Bugs or feature requests should be submitted at our [GitHub Issues section](https://github.com/zeoflow/material-elements/issues).

## How does it work?
### 1. Depend on our library

Material Elements for Android is available through Google's Maven Repository.
To use it:

1.  Open the `build.gradle` file for your application.
2.  Make sure that the `repositories` section includes Google's Maven Repository
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
        implementation 'com.zeoflow:material-elements:<version>'
        // ...
      }
    ```

### 2. Update your app theme to inherit from one of these themes
`styles.xml`

```xml
<style name="Theme.MyApp" parent="Theme.MaterialElements.DayNight">
    <!-- ... -->
</style>
```

#### **Bridge Themes** {#bridge-themes}
If you cannot change your theme to inherit from a Material Elements theme, you
can inherit from a Material Elements **Bridge** theme.

```xml
<style name="Theme.MyApp" parent="Theme.MaterialElements.Light.Bridge">
    <!-- ... -->
</style>
```

### 3. Add a Material component to your app
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

## License
    Copyright 2020 ZeoFlow
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
      http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

## Contributors ✨

Thanks goes to these wonderful people ([emoji key](https://allcontributors.org/docs/en/emoji-key)):

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tr>
    <td align="center"><a href="http://team.zeoflow.com/teodor_g"><img src="https://avatars0.githubusercontent.com/u/22307006?v=4" width="100px;" alt=""/><br /><sub><b>Teodor G.</b></sub></a><br /><a href="#infra-TeodorHMX1" title="Infrastructure (Hosting, Build-Tools, etc)">🚇</a> <a href="https://github.com/zeoflow/material-elements/commits?author=TeodorHMX1" title="Tests">⚠️</a> <a href="https://github.com/zeoflow/material-elements/commits?author=TeodorHMX1" title="Code">💻</a></td>
  </tr>
</table>

<!-- markdownlint-enable -->
<!-- prettier-ignore-end -->
<!-- ALL-CONTRIBUTORS-LIST:END -->

This project follows the [all-contributors](https://github.com/all-contributors/all-contributors) specification. Contributions of any kind welcome!