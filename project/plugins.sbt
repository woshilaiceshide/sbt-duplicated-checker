shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " }

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "4.0.0")
//libraryDependencies += ("com.typesafe.sbteclipse" %% "sbteclipse-plugin" % "4.0.0").extra("sbtVersion" -> sbtBinaryVersion.value, "scalaVersion" -> scalaBinaryVersion.value).copy(crossVersion = CrossVersion.Disabled)

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.5") 

//addSbtPlugin("woshilaiceshide" % "sbt-duplicated-checker" % "0.1-SNAPSHOT")