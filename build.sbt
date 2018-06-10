inThisBuild(
  List(
    version ~= { dynVer =>
      if (isTravisTag) dynVer
      else dynVer + "-SNAPSHOT"
    },
    resolvers += Resolver.sonatypeRepo("releases"),
    scalaVersion := "2.12.6",
    publishArtifact in packageDoc := sys.env.contains("CI"),
    publishArtifact in packageSrc := sys.env.contains("CI")
  )
)

lazy val plugin = project
  .settings(
    moduleName := "sbt-ci-release",
    sbtPlugin := true,
    scriptedBufferLog := false,
    scriptedLaunchOpts += s"-Dplugin.version=${version.value}",
    addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0"),
    addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3"),
    addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.0")
  )

// For some reason, it doesn't work if this is defined in globalSettings in PublishPlugin.
inScope(Global)(
  Seq(
    PgpKeys.pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toCharArray())
  )
)