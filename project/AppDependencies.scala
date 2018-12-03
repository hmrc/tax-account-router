import play.core.PlayVersion.current
import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  val compile = Seq(
    ws,
    "com.softwaremill.macmemo" %% "macros"                   % "0.4",
    "uk.gov.hmrc"              %% "bootstrap-play-26"        % "0.26.0"
  )

  val test = Seq(
    "com.typesafe.play"        %% "play-test"                % current    % "test",
    "org.mockito"              % "mockito-core"              % "2.23.0"   % "test",
    "org.pegdown"              %  "pegdown"                  % "1.6.0"    % "test, it",
    "org.scalatest"            %% "scalatest"                % "3.0.5"    % "test",
    "org.scalatestplus.play"   %% "scalatestplus-play"       % "3.1.2"    % "test, it",
    "uk.gov.hmrc"              %% "frontend-bootstrap"       % "8.24.0"   % "test",
    "uk.gov.hmrc"              %% "hmrctest"                 % "3.2.0"    % "test",
    "uk.gov.hmrc"              %% "service-integration-test" % "0.2.0"    % "test, it"
  )
}