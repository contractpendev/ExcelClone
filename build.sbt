name := "scala-excel"

version := "1.0"

scalaVersion := "2.12.3"

scalacOptions ++= Seq("-feature", "-deprecation", "-Xlint")
libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.6"
libraryDependencies += "io.reactivex" %% "rxscala" % "0.26.5"
libraryDependencies += "org.scalafx" % "scalafx_2.12" % "8.0.144-R12"

// http://www.lihaoyi.com/PPrint/
libraryDependencies += "com.lihaoyi" %% "pprint" % "0.5.3"

//resolvers += Resolver.sonatypeRepo("snapshots")

fork in run := true


