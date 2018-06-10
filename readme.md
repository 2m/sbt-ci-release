# sbt-ci-release

This is an sbt plugin to help automate publishing from Travis CI to Sonatype.

- tag pushes are published to Maven Central
- merge into master commits are published as -SNAPSHOT

Beware that publishing from Travis CI requires you to expose Sonatype
credentials as secret environment variables in Travis CI jobs.
However, note that secret environment variables are not accessible during pull requests.

Let's get started!

First, follow the instructions in https://central.sonatype.org/pages/ossrh-guide.html to
create a Sonatype account and make sure you have publishing rights for a domain name.

Next, install this plugin in `project/plugins.sbt`

```scala
addSbtPlugin("com.geirsson" % "sbt-ci-release" % "<STILL UNPUBLISHED>")
```

By installing `sbt-ci-release` the following sbt plugins are also brought in:
- [sbt-dynver](https://github.com/dwijnand/sbt-dynver): sets the version number based on your git history
- [sbt-pgp](https://github.com/sbt/sbt-pgp): to cryptographically sign the artifacts before publishing
- [sbt-sonatype](https://github.com/xerial/sbt-sonatype): to publish artifacts to Sonatype
- [sbt-git](https://github.com/sbt/sbt-git): to automatically populate `scmInfo`

Next, define publishing settings at the top of `build.sbt`

```scala
inThisBuild(List(
  organization := "com.geirsson",
  homepage := Some(url("https://github.com/scalameta/sbt-scalafmt")),
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  developers := List(
    Developer(
      "olafurpg",
      "Ólafur Páll Geirsson",
      "olafurpg@gmail.com",
      url("https://geirsson.com")
    )
  )
))
```

Next, copy-paste the following to the bottom of `build.sbt`

```scala
inScope(Global)(Seq(
  PgpKeys.pgpPassphrase := sys.env.get("PGP_PASSPHRASE").map(_.toCharArray())
))
```

Next, create a fresh gpg key that you will share with Travis CI and only use for this particular project.
```
gpg --gen-key
```
- For real name, use "$PROJECT_NAME bot". For example, in Scalafmt I use "Scalafmt bot"
- For email, use your own email address
- For passphrase, generate a random password with a password manager

At the end you'll see output like this
```
pub   rsa2048 2018-06-10 [SC] [expires: 2020-06-09]
      $LONG_ID
uid                      $PROJECT_NAME bot <a@a.is>
```

Take note of the id `$LONG_ID`, make sure to replace this ID from the code
examples below. The ID will look something like
`6E8ED79B03AD527F1B281169D28FC818985732D9`.

Next, post the public gpg signature to a keyserver

```
# macOS
gpg --armor --export $LONG_ID | pbcopy
# linux
gpg --armor --export $LONG_ID | xclip
```

Keyserver: https://pgp.mit.edu/


Open the "Settings" panel for your project on Travis CI,
for example https://travis-ci.org/scalameta/sbt-scalafmt/settings.

Define four secret variables

![](https://user-images.githubusercontent.com/1408093/41207402-bbb3970a-6d15-11e8-8772-000cc194ee92.png)

- `PGP_SECRET`: The base64 encoded secret of your private key that you can export from the command line like here below

```
# macOS
gpg --armor --export-secret-keys $LONG_ID | base64 | pbcopy
# linux
gpg --armor --export-secret-keys $LONG_ID | base64 | xclip
```
- `SONATYPE_PASSWORD`: The password you use to log into https://oss.sonatype.org/
- `SONATYPE_USERNAME`: The email you use to log into https://oss.sonatype.org/
- `PGP_PASSPHRASE`: The randomly generated password you used to create a fresh
  gpg key.

Next, update `.travis.yml` to trigger `ci-release` on successful merge into master and on tag push.

```yml
after_success:
- sbt ci-release
```

You're good to go!
- Merge a PR to your project and watch the CI release a -SNAPSHOT for your project.
- Push a tag and watch the CI release a -SNAPSHOT for your project.

It's is normal that something fails on the first attempt to publish from CI.
Even if it takes 10 attempts to get it right, it's still worth it because it's so nice to have automatic CI releases.