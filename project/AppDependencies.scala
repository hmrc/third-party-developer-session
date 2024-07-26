import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {

  private val bootstrapVersion = "9.1.0"
  private val hmrcMongoVersion = "2.2.0"
  private val tpdDomainVersion = "0.6.0-SNAPSHOT"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"    % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"           % hmrcMongoVersion,
    "uk.gov.hmrc"             %% "api-platform-tpd-domain"      % tpdDomainVersion
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"       % bootstrapVersion            % Test,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"      % hmrcMongoVersion            % Test,
    "uk.gov.hmrc"             %% "api-platform-test-tpd-domain" % tpdDomainVersion            % Test,
  )

  val it = Seq.empty
}
