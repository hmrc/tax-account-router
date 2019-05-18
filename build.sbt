import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings
import uk.gov.hmrc.SbtArtifactory
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "tax-account-router"

lazy val scoverageSettings: Seq[Def.Setting[_ >: String with Double with Boolean]] = {

  import scoverage.ScoverageKeys

  Seq(
    ScoverageKeys.coverageEnabled := true,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*filters.*;.*handlers.*;.*components.*;.*models.*;.*repositories.*;" +
      ".*BuildInfo.*;.*javascript.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;.*DataCacheConnector;" +
      ".*ControllerConfiguration;.*LanguageSwitchController;.*FormErrorHelper;.*FrontendAppConfig;.*Constraints;" +
      ".*Formatters;.*CheckYourAnswersHelper;.*FormHelpers;.*error_template.template;.*main_template.template",
    ScoverageKeys.coverageMinimum := 84,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    parallelExecution in Test := false
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin, SbtArtifactory)
  .settings(
    majorVersion                     := 0,
    libraryDependencies              ++= AppDependencies.compile ++ AppDependencies.test
  )
  .settings(publishingSettings: _*)
  .settings(scoverageSettings: _*)
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(resolvers += Resolver.jcenterRepo)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
