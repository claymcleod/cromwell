package cromwell.backend.google.pipelines.v2beta

import akka.actor.ActorRef
import cromwell.backend.BackendConfigurationDescriptor
import cromwell.backend.google.pipelines.common.{PipelinesApiBackendLifecycleActorFactory, PipelinesApiBackendSingletonActor, PipelinesApiConfiguration}
import cromwell.backend.google.pipelines.v2beta.api.request.RequestHandler
import cromwell.backend.standard.StandardAsyncExecutionActor

class PipelinesApiLifecycleActorFactory(name: String, configurationDescriptor: BackendConfigurationDescriptor)
  extends PipelinesApiBackendLifecycleActorFactory(name, configurationDescriptor) {
  val genomicsFactory = LifeSciencesFactory(googleConfig.applicationName, papiAttributes.auths.genomics, papiAttributes.endpointUrl, papiAttributes.location)(papiAttributes.gcsTransferConfiguration)
  override protected val jesConfiguration = new PipelinesApiConfiguration(configurationDescriptor, genomicsFactory, googleConfig, papiAttributes)
  override def requiredBackendSingletonActor(serviceRegistryActor: ActorRef) = {
    implicit val batchHandler = new RequestHandler(googleConfig.applicationName, papiAttributes.endpointUrl, papiAttributes)
    PipelinesApiBackendSingletonActor.props(
      jesConfiguration.papiAttributes.qps,
      jesConfiguration.papiAttributes.requestWorkers,
      serviceRegistryActor
    )
  }
  override lazy val asyncExecutionActorClass: Class[_ <: StandardAsyncExecutionActor] =
    classOf[PipelinesApiAsyncBackendJobExecutionActor]
}
