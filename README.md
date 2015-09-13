# SBT Duplicated Checker #

This sbt plugin can be used to check duplicated files, especially for duplicated classes or configuration files, , which may lead to potential errors.

	[project-x]$ sbt
	[info] Loading project definition from /data/workspace/project-x/project
	[info] Updating {file:/data/workspace/project-x/project/}project-x-build...
	[info] Resolving org.fusesource.jansi#jansi;1.4 ...
	[info] Done updating.
	[info] Set current project to project-x (in build file:/data/workspace/project-x/)
	[project-x] 
	[project-x] deduplicate
	[info] deduplicating with filters: IgnoreWhen(**/META-INF/**), IgnoreWhen(META-INF/**), IgnoreWhen(**NOTICE*), IgnoreWhen(**LICENSE*), IgnoreWhen(reference.conf), IgnoreWhen(conf/reference.conf), WarnWhen(**.conf), ErrorWhen(**.class), WarnWhen(**)
	[warn]  duplicated: ABC, filtered by WarnWhen(**), found in: 
	[warn]     + .../project-x/target/scala-2.10/classes
	[warn]     + .../workspace/project-x/lib/sample.jar
	[error] duplicated: org/slf4j/Logger.class, filtered by ErrorWhen(**.class), found in: 
	[error]    + .../project-x/target/scala-2.10/classes
	[error]    + .../org.slf4j/slf4j-api/jars/slf4j-api-1.7.10.jar
	[info] duplicated found, 1 warnning, 1 error
	[trace] Stack trace suppressed: run last compile:deduplicate for the full output.
	[error] (compile:deduplicate) some duplicated files exist. see sbt's log for details.
	[error] Total time: 0 s, completed Jun 29, 2015 1:51:42 PM

## How To Use It ##
Add the following line to your project/plugins.sbt: 

    addSbtPlugin("woshilaiceshide" % "sbt-duplicated-checker" % "0.1-SNAPSHOT")

If the artifact is not located correctly, just download the source code, and then issue:
 
    cd sbt-duplicated-checker
	[sbt-duplicated-checker]$ sbt publishLocal

## Four Keys ##
1.
**deduplicateTarget**

deduplicate against these targets.

2.
**deduplicateFilter**

deduplicate using these filters written in globl patterns.
 
The default is List(IgnoreWhen(\*\*/META-INF/\*\*), IgnoreWhen(META-INF/\*\*), IgnoreWhen(\*\*NOTICE\*), IgnoreWhen(\*\*LICENSE\*), IgnoreWhen(reference.conf), IgnoreWhen(conf/reference.conf), WarnWhen(\*\*.conf), ErrorWhen(\*\*.class), WarnWhen(\*\*))

For every candidate file, checking will stop if a filter is satisfied from left to right.

3.
**deduplicate**

deduplicate using specified filters in specified targets.

4.
**deduplicateWhenQuerying_fullClasspath**

deduplicate when querying fullClasspath, or not? true by default.

So "**run**" or "**runMain**" or any tasks those depend on fullClasspath will check duplicated files automatically. It's just fine in general. To disable this feature, add the following line to build.sbt: 

    disablePlugins(DeduplicatePlugin)

or toggle it: 

    deduplicateWhenQuerying_fullClasspath := true

## Scopes ##
By default, all of the above four keys stand in **Compile**, **Runtime**, **Test**, and use the dependencies in the corresponding scope. For example, **test:deduplicate** depends on **test:deduplicateTarget**, which depends on **test:fullClasspath**.

## License ##
http://opensource.org/licenses/MIT

## Have fun! ##
Any feedback is expected.

