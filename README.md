# SequenceDiagram
SequenceDiagram for IntelliJ IDEA
http://vanco.github.io/SequencePlugin.

with this plugin, you can
+ generate Simple Sequence Diagram.
+ Navigate the code by click the diagram shape.
+ Delete Class from diagram.
+ Export the diagram as image.
+ Exclude classes from diagram by Settings > Other Settings > Sequence
+ Smart Interface(experimental)

#### Name history
+ **SequencePlugin** Maintained by Kentaur(Kesh Sibilev, ksibilve@yahoo.com) until 2011
+ **SequencePluginReload** Maintained by Vanhg(Evan Fan, fanhuagang@gmail.com) 2011 - 2015
+ **SequenceDiagram** Maintained by Vanco(Evan Fan, fanhuagang@gmail.com) since 2016

#### Why change name?
Since 2011, I found a solution of NPE of original **SequencePlugin**, so I write email to Kentaur with my solution,
He said he was not coding any more. Instead, he send me the code. I fix the NPE issue and publish to plugin
repository with new name **SequencePluginReload**.

But in 2015, the IntelliJ change the login system, and I lost my account, cannot continue publish new version to
the repository.

In 2016, I change the Name again to **SequenceDiagram** and host the source code on [github](https://github.com/Vanco/SequencePlugin).
Now it open source.

Thanks Kentaur for the great work on the original source.

## How to use
SequenceDiagram **ONLY** generate sequence diagram for the **CURRENT** method of **JAVA** file in the editor.
### When installed, where to find it?
![Tools Menu](imges/tools_menu.png "Tools Menu")

![Context Menu](imges/context_menu.png "Context Menu")

Place the cursor in side the method, trigger it from `Tools` menu or `context` menu > `Sequence Diagram...`

## Version History
<dl>
        <dt>1.5</dt>
        <dd>new feature: support scala.</dd>
        <dt>1.2</dt>
        <dd>new feature: Smart Interface, List implementation of interface in project, user can choose one to show in sequence diagram. If only one implementation found, it will show automatically. </dd>
        <dt>1.1</dt>
        <dd>fix issue #3 support parameter is anther method call, fix issue #4 support pipeline call.</dd>
        <dt>1.0.9</dt>
        <dd>fix issue #1 recognise more generic method definition, fix issue #2 show popup menu on mac and linux.</dd>
        <dt>1.0.8</dt>
        <dd>Refine not to call "Deprecated" methods. Add Exclude Settings. Change the name to SequenceDiagram. </dd>
        <dt>1.0.7</dt>
        <dd>Remove "com.intellij.diagram" dependency. </dd>
        <dt>1.0.6</dt>
        <dd>HotFix: add missing res file in version 1.0.5.</dd>
        <dt>1.0.5</dt>
        <dd>Narrow the plugin support product list to java.[ABANDONED]</dd>
        <dt>1.0.4</dt>
        <dd>fixbug: go to source</dd>
        <dt>1.0.3</dt>
        <dd>fixbug: NPE when call psiMethod.getContainingClass() on enum class.</dd>
        <dt>1.0.2</dt>
        <dd>Add Sequence Diagram... menu under the Tools menu and Editor popup menu group with Diagram.</dd>
        <dd>fixbug: method name with generic type.</dd>
</dl>