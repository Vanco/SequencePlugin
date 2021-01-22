# SequenceDiagram Changelog

## [Unreleased]
### Added
- Use `org.jetbrains.changelog` gradle plugin `1.0.0` to manage changelog file.

### Changed
- Move changelog from `README.md` to `CHANGELOG.md`  

## [2.0.6]
### Fixed
- bug：cannot save configuration #70
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
- Merged pull request #58 and now you can add object color overlay.

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
- Add configuration for method call name, It's now showing in simple/full format.

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
- fix issue #3 support parameter is anther method call, fix issue #4 support pipeline call.

## [1.0.9]
### Fixed
- fix issue #1 recognise more generic method definition, fix issue #2 show popup menu on mac and linux.

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