# SequenceDiagram Changelog

## Unreleased
### Added
- RegexFilter: e.g. `.*Builder` to filter `my.package.MyBuilder`, `java.lang.StringBuilder`...

## 3.0.5 - 2023-05-24

### Added
- add FilterManager (Paid)

### Changed
- improve smart interface
- Change icons to default icons

### Fixed
- Exporting change dark/light style bug.
- Click empty other toolwindow (Terminal, Service...) create welcome page issue

### Removed
- Remove unused UI class
- Remove `Expend...` from class context menu.

## 3.0.4 - 2023-04-06

### Added
- Don't show `remove xxx` popup menu on `Actor` or bootstrap Class, Method, Call
- Generic Filter

### Fixed
- Fix welcome color support light/dark theme
- Fix smart interface only fellow first one found
- Fix export JPEG/PNG/TIFF null error (affect idea 2023.1)

### Removed
- Remove old Java and Kotlin generator (2.2.6)

## 3.0.3 - 2023-03-29

### Added
- Anonymous class support
- Cancel generate process

### Changed
- Rename ElementTypeFinder to SequenceLanguagePlugin
- improve navigation
- improve generate speed
- Make expend implementation a select list

### Fixed
- limited call deep overflow when interface -> impl call
- config issue.
- fix NPE when some psiElement to UMethod is null
- fix null in implementation search result
- fix color mapping remove issue

## 3.0.2 - 2023-03-09

### Changed
- display lambda real parameter
- delete lambda from diagram
- fixed `Display only project class`
- improve navigation

## 3.0.1 - 2023-03-06

### Added
- UAST Generator: Java, Kotlin, Scala(Beta), Groovy(limited)

### Changed
- Add `Smart Interface` configuration
- Add `isExternal` method
- Remove `showSequence(UMethod)` method
- Remove `UastSequnceNavigable.kt` kotlin class
- Remove `CallStack generate(UElement node, CallStack parent)` method

### fixed
- issue #139
- navigation issue.
- scala package object class issue.

## 2.2.6

### Change
- rollback to old java/kotlin generator because UAST api will throw `UnsupportedOperatonException`

## 2.2.5

### Added
- `UastSequenceGenerator` suppose to support JVM Language: Java, Kotlin, Scala, Groovy.

### Fixed
- Issue #139, #141 `UnsupportedOperatonException`

## 2.2.4

### Added
- UAST ActionFinder

### Change
- move to package `vanstudio`

### Fixed
- Issue #111 StackOverflowError when I try to use the plugin for non-trivial (bigger) classes

## 2.2.3

### Fixed
- Cannot create `ShowSequenceAction` when the Kotlin plugin is disabled.

### Added
- Support generate class initializer

## 2.2.2

### Fixed
- Export image width issue.
- Change `Logger` to `com.intellij.openapi.diagnostic.Logger`
- NPE when `KtParameter.getTypeReference` is null

## 2.2.1

### Added
- Add ShowSequenceActionGroup
- Extract openapi
- Add Shortcut `Alt S` (Windows OS), `option S` (macOS)
- Export SVG, PNG, JPEG, TIFF image
- Generate Kotlin `init`

## 2.2.0

### Changed
- Change sdt to line break schema
- Refactoring add formatter package for PlantUML, Mermaid format.

### Fixed
- Issue #126
- Issue #95
- fix isRecursive always false issue.
- SVG canvas size issue.
- SVG `λ→` show incorrectly.
- Issue #129
- Issue #130

## 2.1.10

### Added
- `Show lambda call` option
- Add Color Icon in setting Colors.
- Support generate function expression body in kotlin
- Support generate lambda_argument

### Removed
- Remove `Generate Sequence Diagram` intention

### Fixed
- Issue#127
- Issue#100 Method Declaration Becomes Grey

## 2.1.9

### Changed
- Build for 222.x
- Add parent CallStack parameter to IGenerator

## 2.1.8

### Fixed
- Unable to export large images #122, #119

## 2.1.7

### Fixed
- Support for Mermaid when export diagram.
- Support export PlantUML and Mermaid when unchecked `Show simple call name`.
- Fix some java.util.ConcurrentModificationException bugs.

## 2.1.6

### Changed
- Build for 221.x
- Fix code which invoke removed api.

## 2.1.5

### Fixed
- Can you raise this label “Remove Class XXXXX” to the top？#116

### Changed
- Only show implementation class menu list for interface in project scope.

## 2.1.4

### Fixed
- V2.1.3 does not jump to the code #113

## 2.1.3

### Added
- - Support code navigation
  - Support Java code call kotlin

### Changed
- Global share sequence diagram options.
- Try to make loaded diagram navigable.
- Add Notification when finish load .sdt file

## 2.1.2

### Changed
- Optimized performance issue
- Optimized java navigation
- Optimized Lambda generation

### Fixed
- SequencePlugin shows puzzling warnings in the Problems tab #99
- Navigation not jump to source #76

## 2.1.1

### Added
- - Support generate JavaCall deeply
- - Add to Project view popup menu

### Changed
- Change `README.md` add some description in where to find section.

### Fixed
- http://vanco.github.io/SequencePlugin can not open #96
- NPE when generate some kotlin in Java.

## 2.1.0

### Added
- Use `org.jetbrains.changelog` gradle plugin `1.0.0` to manage changelog file.
- - Support generate topLevel function
  - Support generate PrimaryConstructor and SecondaryConstructor
  - Support generate No Constructor Class
  - Support generate JavaCall
  - Code navigation is Not Implement Yet
- - Use vector icons.
  - Add Generate sequence diagram intention.
  - Add Icon to Toolbar.
  - Add Settings Icon.

### Changed
- Move changelog from `README.md` to `CHANGELOG.md`
- Add some missing `@Override` annotations in `ConfigurationOptions.java`(And it seems that SequenceDiagram support the 
  2021.1 IDEA platform now ?)
- Build for 2021.2
- Change to gradle kotlin script.
- Place inside method will generate sequence diagram for enclosed method.
- Place at class name or anywhere other than method will pop up method list to choose.

### Fixed
- Issue #81 IndexOutOfBoundsException
- Issue #82 Null pointer exception after double-click in the diagram
- Issue #88 Exported PlantUML is zero-length for reopened SDT files

## 2.0.6

### Fixed
- bug：cannot save sequenceSettingsState #70
- Cannot add SequenceDiagram to Android Studio 4.1.1 #69

## 2.0.5

### Added
- Export PlantUML file.

### Fixed
- [Feature Request] export puml file #56

## 2.0.4

### Changed
- Rebuild for 203.*

## 2.0.3

### Fixed
- Fix NPE issue when click the diagram load from .sdt file.

## 2.0.2

### Added
- Add save to file feature, you can save diagram to `*.sdt` file.
- Load `.sdt` file to view the graph.

### Changed
- Merged pull request #58, and now you can add object color overlay.

## 2.0.1

### Changed
- Filled arrows for synchronous messages. #47
- Refine Method Info Tips.

## 2.0.0

### Changed
- New Plugin version support 2020.x

## 1.3.0

### Fixed
- fix #38: Lambda Expression.

## 1.2.8

### Fixed
- fix#34: Can we export the whole call stack to a text file.

## 1.2.7

### Fixed
- fix#26: Dead loop when generate Wrapper Patten class. fix issue when disable smartInterface option.

## 1.2.6

### Fixed
- fix#22: PNG export.

## 1.2.5

### Fixed
- fix#20: Export PNG cropped method names.

## 1.2.4

### Added
- Add sequenceSettingsState for method call name, It's now showing in simple/full format.

## 1.2.3

### Added
- Add abstract class support.

## 1.2.2

### Fixed
- hotfix: #10 NPE when visitNewException.

## 1.2.1

### Fixed
- hotfix: NPE when process local variable's initializer. Show bird view icon in lower right corner.

## 1.2.0

### Added
- new feature: Smart Interface, List implementation of interface in project, user can choose one to 
  show in sequence diagram. If only one implementation found, it will show automatically.

## 1.1.0

### Fixed
- fix issue #3 support parameter is another method call, fix issue #4 support pipeline call.

## 1.0.9

### Fixed
- fix issue #1 recognise more generic method definition, fix issue #2 show popup menu on Mac and linux.

## 1.0.8

### Changed
- Refine not to call "@Deprecated" methods. Add Exclude Settings. Change the name to SequenceDiagram.

## 1.0.7

### Changed
- Remove "com.intellij.diagram" dependency.

## 1.0.6

### Fixed
- HotFix: add missing res file in version 1.0.5.

## 1.0.5

### Changed
- Narrow the plugin support product list to java.

## 1.0.4

### Fixed
- fix bug: go to source issue

## 1.0.3

### Fixed
- fix bug: NPE when call psiMethod.getContainingClass() on enum class.

## 1.0.2

### Added
- Add Sequence Diagram... menu under the Tools menu and Editor popup menu group with Diagram.

### Fixed
- fix bug: method name with generic type.
