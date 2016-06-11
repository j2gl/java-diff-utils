A fork of [java-diff-utils](https://code.google.com/p/java-diff-utils/)

# Changelog

## 2.1.0

- Removes the dependency on Guava

## 2.0.0

- Change groupId and artifactId to prevent conflict with origin library: now 'com.github.java-diff-utils:java-diff-utils' instead of 'jp.skypencil.java-diff-utils:diffutils'
- Adds the ability to differentiate the inserted and deleted tags and class-names in inline-diff
- Default class-name is now `null` for deleted and inserted data, and "`change`" for change data
- Default tag for deleted data is `del`
- Default tag for inserted data is `ins`
- can now customize diff algorithm in `DiffRowGenerator.Builder`
- fix "equal" lines when lines isn't really equals (when Equalizer return equals on different strings)
- fix imbrication tag bug in lineDiff (when inline is on a multi-line chunk)
- Adds tha ability to skip data

## 1.5.0

- make Equalizer configurable. ([pull #1](https://github.com/eller86/java-diff-utils/pull/1))

## 1.4.1

- bugfix: parse method should be public

## 1.4.0

- switch from JDK5 to JDK7
- add Guava to dependency
- let user uses other string to represent line which does not exist
- implement event based parser like SAX (in difflib.event package)

