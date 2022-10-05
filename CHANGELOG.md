# SequenceDiagram Changelog

## [Unreleased]

## [2.2.3]
### Fixed
- Cannot create `ShowSequenceAction` when the Kotlin plugin is disabled.

### Added
- Support generate class initializer

## [2.2.2]
### Fixed
- Export image width issue.
- Change `Logger` to `com.intellij.openapi.diagnostic.Logger`
- NPE when `KtParameter.getTypeReference` is null

## [2.2.1]
### Added
- Add ShowSequenceActionGroup
- Extract openapi
- Add Shortcut `Alt S` (Windows OS), `option S` (macOS)
- Export SVG, PNG, JPEG, TIFF image
- Generate Kotlin `init`

## [2.2.0]
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

## [2.1.10]
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

## [2.1.9]
### Changed
- Build for 222.x
- Add parent CallStack parameter to IGenerator

## [2.1.8]
### Fixed
- Unable to export large images #122, #119

## [2.1.7]
### Fixed
- Support for Mermaid when export diagram.
- Support export PlantUML and Mermaid when unchecked `Show simple call name`.
- Fix some java.util.ConcurrentModificationException bugs.

## [2.1.6]
### Changed
- Build for 221.x
- Fix code which invoke removed api.

## [2.1.5]
### Fixed
- Can you raise this label “Remove Class XXXXX” to the top？#116

### Changed
- Only show implementation class menu list for interface in project scope.

## [2.1.4]
### Fixed
- V2.1.3 does not jump to the code #113

## [2.1.3]
### Added
- Kotlin support(experimental)
  - Support code navigation
  - Support Java code call kotlin

### Changed
- Global share sequence diagram options.
- Try to make loaded diagram navigable.
- Add Notification when finish load .sdt file

## [2.1.2]
### Changed
- Optimized performance issue
- Optimized java navigation
- Optimized Lambda generation

### Fixed
- SequencePlugin shows puzzling warnings in the Problems tab #99
- Navigation not jump to source #76

## [2.1.1]
### Added
- Kotlin support(experimental)
  - Support generate JavaCall deeply
- UI improvements
  - Add to Project view popup menu

### Changed
- Change `README.md` add some description in where to find section.

### Fixed
- http://vanco.github.io/SequencePlugin can not open #96
- NPE when generate some kotlin in Java.

## [2.1.0]
### Added
- Use `org.jetbrains.changelog` gradle plugin `1.0.0` to manage changelog file.
- Kotlin support(experimental)
  - Support generate topLevel function
  - Support generate PrimaryConstructor and SecondaryConstructor
  - Support generate No Constructor Class
  - Support generate JavaCall
  - Code navigation is Not Implement Yet
- UI improvements
  - Use vector icons.
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

## [2.0.6]
### Fixed
- bug：cannot save sequenceSettingsState #70
- Cannot add SequenceDiagram to Android Studio 4.1.1 #69

## [2.0.5]
### Added
- Export PlantUML file.

### Fixed
- [Feature Request] export puml file #56

## [2.0.4]
### Changed
- Rebuild for 203.*

## [2.0.3]
### Fixed
- Fix NPE issue when click the diagram load from .sdt file.

## [2.0.2]
### Added
- Add save to file feature, you can save diagram to `*.sdt` file.
- Load `.sdt` file to view the graph.

### Changed
- Merged pull request #58, and now you can add object color overlay.

## [2.0.1]
### Changed
- Filled arrows for synchronous messages. #47
- Refine Method Info Tips.

## [2.0.0]
### Changed
- New Plugin version support 2020.x

## [1.3.0]
### Fixed
- fix #38: Lambda Expression.

## [1.2.8]
### Fixed
- fix#34: Can we export the whole call stack to a text file.

## [1.2.7]
### Fixed
- fix#26: Dead loop when generate Wrapper Patten class. fix issue when disable smartInterface option.

## [1.2.6]
### Fixed
- fix#22: PNG export.

## [1.2.5]
### Fixed
- fix#20: Export PNG cropped method names.

## [1.2.4]
### Added
- Add sequenceSettingsState for method call name, It's now showing in simple/full format.

## [1.2.3]
### Added
- Add abstract class support.

## [1.2.2]
### Fixed
- hotfix: #10 NPE when visitNewException.

## [1.2.1]
### Fixed
- hotfix: NPE when process local variable's initializer. Show bird view icon in lower right corner.

## [1.2.0]
### Added
- new feature: Smart Interface, List implementation of interface in project, user can choose one to 
  show in sequence diagram. If only one implementation found, it will show automatically.

## [1.1.0]
### Fixed
- fix issue #3 support parameter is another method call, fix issue #4 support pipeline call.

## [1.0.9]
### Fixed
- fix issue #1 recognise more generic method definition, fix issue #2 show popup menu on Mac and linux.

## [1.0.8]
### Changed
- Refine not to call "@Deprecated" methods. Add Exclude Settings. Change the name to SequenceDiagram.

## [1.0.7]
### Changed
- Remove "com.intellij.diagram" dependency.

## [1.0.6]
### Fixed
- HotFix: add missing res file in version 1.0.5.

## [1.0.5]
### Changed
- Narrow the plugin support product list to java.

## [1.0.4]
### Fixed
- fix bug: go to source issue

## [1.0.3]
### Fixed
- fix bug: NPE when call psiMethod.getContainingClass() on enum class.

## [1.0.2]
### Added
- Add Sequence Diagram... menu under the Tools menu and Editor popup menu group with Diagram.

### Fixed
- fix bug: method name with generic type.