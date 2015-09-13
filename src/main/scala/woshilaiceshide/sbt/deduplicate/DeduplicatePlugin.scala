package woshilaiceshide.sbt.deduplicate

import sbt._
import sbt.Keys._
import sbt.Def.{ Initialize, setting, task, taskDyn }
import FileUtility._
object DeduplicatePlugin extends AutoPlugin {

  object autoImport {

    import FileUtility._
    import java.io._

    val IgnoreWhen = FileUtility.IgnoreWhen
    val WarnWhen = FileUtility.WarnWhen
    val ErrorWhen = FileUtility.ErrorWhen

    val defaultDeduplicateFilters = Seq(IgnoreWhen("**/META-INF/**"), IgnoreWhen("META-INF/**"), IgnoreWhen("**NOTICE*"), IgnoreWhen("**LICENSE*"), IgnoreWhen("reference.conf"), IgnoreWhen("conf/reference.conf"), WarnWhen("**.conf"), ErrorWhen("**.class"), WarnWhen("**"))

    val deduplicateTarget = taskKey[Seq[File]]("deduplicate against these targets.")
    val deduplicateFilter = SettingKey[Seq[PathMatcherWithLevel]]("deduplicateFilter", s" deduplicate using these filters written in globl patterns, \n such as ${defaultDeduplicateFilters}")
    val deduplicate = taskKey[Unit]("deduplicate using specified filters in specified targets.")
    val deduplicateWhenQuerying_fullClasspath = settingKey[Boolean]("deduplicate when querying fullClasspath, or not? true by default.")

    type PathFilter = PathMatcherWithLevel
    type GeneralLogger = {
      def info(message: => String): Unit
      def warn(message: => String): Unit
      def error(message: => String): Unit
    }

    private def format(fileGroup: Seq[File]) = {
      val size = fileGroup.size
      fileGroup
        .map { x => s"${refine(x)}" }
        .zip(0 to size).map { x =>
          {
            x._2 match {
              case index if index == 0        => s"+ ${x._1}"
              case index if index == size - 1 => s"+ ${x._1}"
              case _                          => s"| ${x._1}"
            }
          }
        }
    }
    def deduplicate(targets: Seq[File], filters: Seq[PathFilter], logger: GeneralLogger): Unit = {

      logger.info(s"deduplicating with filters: ${filters.mkString(", ")}")
      val listOfCatched = filterDuplicated(targets, filters).toSeq
      listOfCatched.filter(!_.matcher.isInstanceOf[IgnoreWhen]) match {
        case Seq() => { logger.info(s"no duplicated found"); }
        case list => {
          list map {
            case Catched(path, containers, IgnoreWhen(pattern)) => {}
            case Catched(path, containers, filter @ WarnWhen(pattern)) => {
              logger.warn(s" duplicated: ${path}, filtered by ${filter}, found in: ")
              logger.warn(format(containers).map("    " + _).mkString("\n"))
            }
            case Catched(path, containers, filter @ ErrorWhen(pattern)) => {
              logger.error(s"duplicated: ${path}, filtered by ${filter}, found in: ")
              logger.error(format(containers).map("   " + _).mkString("\n"))
            }
          }
          val countOfWarnning = list.count { _.matcher.isInstanceOf[WarnWhen] }
          val countOfError = list.count { _.matcher.isInstanceOf[ErrorWhen] }
          logger.info(s"duplicated found, ${if (1 == countOfWarnning) "1 warnning" else s"${countOfWarnning} warnnings"}, ${if (1 == countOfError) "1 error" else s"${countOfError} errors"}")

          if (0 < countOfError) throw new RuntimeException("some duplicated files exist. see sbt's log for details.")
        }
      }

    }
  }

  import autoImport._

  override def requires = sbt.plugins.CorePlugin && sbt.plugins.JvmPlugin
  override def trigger = allRequirements

  private def deduplicateInConfig(config: Configuration) = {
    deduplicate.in(config) := {
      if (deduplicateWhenQuerying_fullClasspath.value) {
        fullClasspath.in(config).value
      } else {
        copyResources.in(config).value
        val targets = deduplicateTarget.in(config).value
        val filters = deduplicateFilter.value
        val logger = streams.value.log
        deduplicate(targets, filters, logger)
      }
    }
  }

  private def deduplicatedTargetInConfig(config: Configuration) = {
    deduplicateTarget in config := fullClasspath.in(config).value.map { _.data }
  }

  private def fullClasspathInConfig(config: Configuration) = {
    fullClasspath.in(config) := {
      if (deduplicateWhenQuerying_fullClasspath.value) {
        val value = fullClasspath.in(config).value
        copyResources.in(config).value
        val targets = value.map { _.data }
        val filters = deduplicateFilter.value
        val logger = streams.value.log
        deduplicate(targets, filters, logger)
        value
      } else {
        fullClasspath.in(config).value
      }
    }
  }

  //Compile, Runtime, Test, Provided, Optional
  private val configs = Seq(Compile, Runtime, Test) //sbt.Configurations.default

  override def projectSettings: Seq[Setting[_]] =
    Seq(deduplicateWhenQuerying_fullClasspath := true) ++
      configs.map(deduplicatedTargetInConfig) ++
      Seq(deduplicateFilter := defaultDeduplicateFilters) ++
      configs.map(deduplicateInConfig) ++
      configs.map(fullClasspathInConfig)
}
