<!--docs:
title: "Contributing"
layout: landing
section: docs
path: /docs/contributing/
-->

# General Contributing Guidelines

The Material Elements contributing policies and procedures can be found in the
main Material Elements documentation repository’s
[contributing page](https://github.com/zeoflow/materiale-elements/blob/CONTRIBUTING.md).

To make a contribution, you'll need to be able to build the library from source
and run our tests.

## Building From Source

Take a look at our [instructions](building-from-source.md) on how to build the
library from source.

## Running Tests

Material Components for Android has JVM tests as well as Emulator tests.

To run the JVM tests, do:

```sh
./gradlew test
```

To run the emulator tests, ensure you have
[a virtual device set up](https://developer.android.com/studio/run/managing-avds.html)
and do:

```sh
./gradlew connectedAndroidTest
```

## Code Conventions

Since we all want to spend more time coding and less time fiddling with
whitespace, Material Elements uses code conventions and styles to
encourage consistency. Code with a consistent style is easier (and less
error-prone!) to review, maintain, and understand.

#### Be consistent

If the style guide is not explicit about a particular situation, the cardinal
rule is to **be consistent**. For example, take a look at the surrounding code
and follow its lead, or look for similar cases elsewhere in the codebase.