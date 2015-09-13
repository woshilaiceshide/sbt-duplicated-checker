import sbt._
import sbt.Keys._

import bintray._
import bintray.BintrayKeys._

object Build extends Build {

  val projectName = "sbt-duplicated-checker"

  lazy val root = Project(
    projectName,
    file("."))
    //.settings(Defaults.defaultSettings: _*)
    //.enablePlugins(plugins.JvmPlugin, plugins.IvyPlugin, plugins.CorePlugin)
    .settings(sbtPlugin := true)
    .settings(organization := "woshilaiceshide")
    .settings(name := projectName)
    .settings(version := "0.1")
    .settings(description := "a sbt plugin used to checking duplicated files, especially dupliciated classes and configuration files.")
    //.settings(crossScalaVersions := Seq("2.10.5", "2.11.7"))
    .settings(shellPrompt in ThisBuild := { state => Project.extract(state).currentRef.project + "> " })
    .enablePlugins(BintrayPlugin)
    //.settings(licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")))
    .settings(licenses += ("MIT", url("http://opensource.org/licenses/MIT")))
    .settings(publishMavenStyle := false)
    .settings(bintrayRepository := "sbt-plugins")
    .settings(bintrayOrganization := None)
    //.settings(bintrayVcsUrl := None)
    .settings(bintrayVcsUrl := Some(s"git@github.com:woshilaiceshide/${projectName}.git"))
    .settings(bintrayReleaseOnPublish in ThisBuild := false) //use bintrayRelease
    .settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)

}