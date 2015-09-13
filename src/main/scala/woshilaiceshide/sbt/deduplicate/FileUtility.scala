package woshilaiceshide.sbt.deduplicate

object FileUtility {

  import java.io._
  import java.util.zip._
  import java.nio.file._

  private val fs = FileSystems.getDefault

  private[deduplicate] def refine(file: File, depth: Int = 4) = {

    import scala.collection.JavaConverters._
    val nodes = fs.getPath(file.getPath).iterator().asScala.toSeq
    if (depth >= nodes.size) file.getPath
    else {
      ".../" + nodes.drop(nodes.size - depth).mkString(File.separator)
    }
  }

  //not tail-recursive
  @inline private def relativeChildren(ancestor: File, parent: File): Seq[String] = {

    //import java.nio.file._
    //val p1 = Paths.get("x/y/z")
    //val p0 = Paths.get("x/y/z/a/b/c")
    //val p = p0.relativize(p1)

    val ancestor0 = if (ancestor.isAbsolute()) ancestor else ancestor.getAbsoluteFile()
    val parent0 = if (parent.isAbsolute()) parent else parent.getAbsoluteFile()
    val children = parent0.listFiles() match {
      case null => Seq();
      case x    => x.toSeq
    }
    children.flatMap { child =>
      child match {
        case x if x.isFile()      => Seq(ancestor0.toURI.relativize(x.toURI).getPath())
        case x if x.isDirectory() => relativeChildren(ancestor, x)
        case x                    => Seq()
      }
    }
  }

  private def zipChildren(zipFile: ZipFile) = {
    import scala.collection.JavaConverters._
    zipFile.entries().asScala.filter { !_.isDirectory }.map { _.getName }.toSeq
  }

  private def entriesOf(paths: Seq[File]) = {
    paths.map { path =>
      path match {
        case x if x.isFile => (x, zipChildren(new ZipFile(x)))
        case x             => (x, relativeChildren(x, x))
      }
    }
  }

  sealed abstract class PathMatcherWithLevel(matcher: PathMatcher) extends PathMatcher {
    def matches(path: Path) = { matcher.matches(path) }
  }

  //DO NOT use WildcardFileFilter in commons-io/commons-io
  case class IgnoreWhen(pattern: String) extends PathMatcherWithLevel(fs.getPathMatcher(s"glob:${pattern}"))
  case class WarnWhen(pattern: String) extends PathMatcherWithLevel(fs.getPathMatcher(s"glob:${pattern}"))
  case class ErrorWhen(pattern: String) extends PathMatcherWithLevel(fs.getPathMatcher(s"glob:${pattern}"))

  case class Catched[T <: PathMatcher](path: String, containers: Seq[File], matcher: T)
  private[deduplicate] def filterDuplicated[T <: PathMatcher](paths: Seq[File], matchers: Seq[T]) = {

    val reversed = entriesOf(paths).flatMap {
      case (file, children) => {
        children.map { child => (child, file) }
      }
    }

    reversed
      .groupBy(_._1).filter(_._2.size > 1)
      .map { x =>
        (x._1, x._2.map(_._2), matchers.find { _.matches(fs.getPath(x._1)) })
      }
      .filter(_._3.isDefined)
      .map(x => Catched(x._1, x._2, x._3.get))

  }

}