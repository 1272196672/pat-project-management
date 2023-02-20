package com.pat.task

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.cluster.ClusterEvent
import akka.cluster.ClusterEvent.{LeaderChanged, MemberEvent, MemberRemoved, MemberUp, RoleLeaderChanged, UnreachableMember}
import akka.cluster.typed.{Cluster, Subscribe, Unsubscribe}

object ClusterListener {
  def listen(): Behavior[ClusterEvent.ClusterDomainEvent] = {
    Behaviors.setup { context =>
      context.log.debug("starting up ChatClusterListener...")
      Cluster(context.system).subscriptions ! Subscribe(context.self, classOf[ClusterEvent.ClusterDomainEvent])

      Behaviors.receiveMessagePartial {
        case RoleLeaderChanged(role, leader) =>
          context.log.info(s"HELLO BROTHER: $role $leader")
          Behaviors.same
        case MemberUp(member) =>
          context.log.debug("Member is Up: {}", member.address)
          Behaviors.same
        case UnreachableMember(member) =>
          context.log.debug("Member detected as unreachable: {}", member)
          Behaviors.same
        case MemberRemoved(member, previousStatus) =>
          context.log.debug("Member is Removed: {} after {}", member.address: Any, previousStatus: Any)
          Behaviors.stopped { () =>
            Cluster(context.system).subscriptions ! Unsubscribe(context.self)
          }
          Behaviors.same
        case LeaderChanged(member) =>
          context.log.info("Leader changed: " + member)
          Behaviors.same
        case any: MemberEvent =>
          context.log.info("Member Event: " + any.toString)
          Behaviors.same
      }
    }
  }
}