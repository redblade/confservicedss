package eu.pledgerproject.confservice.domain.enumeration;

/**
 * The ExecStatus enumeration.
 */
public enum ExecStatus {
    RUNNING, STOPPED, ERROR, STARTING, STOPPING, FORCE_STOP, SCALING_MORE_RESOURCES, SCALING_LESS_RESOURCES, OFFLOADING_BETTER_RANKING, OFFLOADING_WORSE_RANKING
}
