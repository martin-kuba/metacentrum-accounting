package cz.cesnet.meta.pbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Class representing a queue as reported by a PBS server.
 *
 * @author Martin Kuba makub@ics.muni.cz
 */
public class Queue extends PbsInfoObject {

    final static Logger log = LoggerFactory.getLogger(Queue.class);

    //used attribute names
    public static final String ATTRIBUTE_QUEUE_TYPE = "queue_type";
    public static final String ATTRIBUTE_ROUTE_DESTINATIONS = "route_destinations";
    public static final String ATTRIBUTE_WALLTIME_MIN = "resources_min.walltime";
    public static final String ATTRIBUTE_WALLTIME_MAX = "resources_max.walltime";
    public static final String ATTRIBUTE_PRIORITY = "Priority";
    public static final String ATTRIBUTE_REQUIRED_PROPERTY = "required_property";
    public static final String ATTRIBUTE_ACL_USERS = "acl_users";
    public static final String ATTRIBUTE_ACL_GROUPS = "acl_groups";
    public static final String ATTRIBUTE_ACL_HOSTS = "acl_hosts";
    public static final String ATTRIBUTE_ACL_USERS_ENABLED = "acl_user_enable";
    public static final String ATTRIBUTE_ACL_GROUPS_ENABLED = "acl_group_enable";
    public static final String ATTRIBUTE_ACL_HOSTS_ENABLED = "acl_host_enable";
    public static final String ATTRIBUTE_MAX_RUNNING_JOBS = "max_running";
    public static final String ATTRIBUTE_MAX_USER_RUN = "max_user_run";
    public static final String ATTRIBUTE_MAX_USER_CPU = "max_user_proc";
    public static final String ATTRIBUTE_FAIRSHARE_TREE = "fairshare_tree";
    public static final String ATTRIBUTE_DESCRIPTION_CZECH = "description_cs";
    public static final String ATTRIBUTE_DESCRIPTION_ENGLISH = "description_en";

    public Queue() {
        super();
    }

    public Queue(String name) {
        super(name);
    }

    //primary data are inherited from PBSInfoObject

    private int running = 0;
    private int completed = 0;
    private int queued = 0;
    private int total = 0;
    private int priority = -1;
    private String[] aclUsers = null;
    private String[] aclGroups = null;
    private String[] aclHosts = null;

    @Override
    public void clear() {
        super.clear();
        aclUsers = null;
        aclGroups = null;
        aclHosts = null;
    }

    @Override
    public String getName() {
        return pbs == null ? name : name + pbs.getSuffix();
    }

    public String getShortName() {
        return name;
    }

    public boolean isMaintenance() {
        return name.equals(PbsUtils.MAINTENANCE);
    }

    public boolean isReserved() {
        return name.equals(PbsUtils.RESERVED);
    }

    public boolean isRouting() {
        return "Route".equals(attrs.get(ATTRIBUTE_QUEUE_TYPE));
    }

    public List<String> getDestQueueNames() {
        String route_destinations = attrs.get(ATTRIBUTE_ROUTE_DESTINATIONS);
        if (route_destinations == null) return Collections.emptyList();
        String[] shortNames = route_destinations.split(",");
        List<String> dsts = new ArrayList<>(shortNames.length);
        for (String shortName : shortNames) {
            dsts.add(pbs == null ? shortName : shortName + pbs.getSuffix());
        }
        return dsts;
    }

    public String getWalltimeMin() {
        return attrs.get(ATTRIBUTE_WALLTIME_MIN);
    }

    public String getWalltimeMax() {
        return attrs.get(ATTRIBUTE_WALLTIME_MAX);
    }

    public long getWalltimeMinSeconds() {
        String t = getWalltimeMin();
        if (t == null) return 0;
        String[] sa = t.split(":");
        if (sa.length != 3) {
            log.error("cannot parsec queue {} mintime {}", getName(), t);
            return 0;
        }
        return Long.parseLong(sa[0]) * 3600L + Long.parseLong(sa[1]) * 60L + Long.parseLong(sa[2]);
    }

    public long getWalltimeMaxSeconds() {
        String t = getWalltimeMax();
        if (t == null) return Long.MAX_VALUE;
        String[] sa = t.split(":");
        if (sa.length != 3) {
            log.error("cannot parsec queue {} maxtime {}", getName(), t);
            return Long.MAX_VALUE;
        }
        return Long.parseLong(sa[0]) * 3600L + Long.parseLong(sa[1]) * 60L + Long.parseLong(sa[2]);
    }


    public int getPriority() {
        if (priority == -1) {
            String p = attrs.get(ATTRIBUTE_PRIORITY);
            if (p == null) {
                this.priority = 0;
            } else {
                this.priority = Integer.parseInt(p);
            }
        }
        return priority;
    }

    /**
     * Returns a property that nodes must have to be used by this queue.
     *
     * @return value of required_property attribute
     */
    public String getRequiredProperty() {
        return attrs.get(ATTRIBUTE_REQUIRED_PROPERTY);
    }

    /**
     * Array of user names in ACL list.
     *
     * @return value of acl_users attribute split at commas
     */
    public String[] getAclUsersArray() {
        if (this.aclUsers == null) {
            String propatr = getAclUsers();
            if (propatr != null) {
                this.aclUsers = propatr.split(",");
            } else {
                this.aclUsers = new String[0];
            }
        }
        return this.aclUsers;
    }

    public String[] getAclGroupsArray() {
        if (this.aclGroups == null) {
            String propatr = getAclGroups();
            if (propatr != null) {
                this.aclGroups = propatr.split(",");
            } else {
                this.aclGroups = new String[0];
            }
        }
        return this.aclGroups;
    }

    public String[] getAclHostsArray() {
        if (this.aclHosts == null) {
            String propatr = getAclHosts();
            if (propatr != null) {
                this.aclHosts = propatr.split(",");
            } else {
                this.aclHosts = new String[0];
            }
        }
        return this.aclHosts;
    }

    public String getAclUsers() {
        return attrs.get(ATTRIBUTE_ACL_USERS);
    }

    public String getAclGroups() {
        return attrs.get(ATTRIBUTE_ACL_GROUPS);
    }

    public String getAclHosts() {
        return attrs.get(ATTRIBUTE_ACL_HOSTS);
    }

    public boolean isAclUsersEnabled() {
        return "True".equals(attrs.get(ATTRIBUTE_ACL_USERS_ENABLED));
    }

    public boolean isAclGroupsEnabled() {
        return "True".equals(attrs.get(ATTRIBUTE_ACL_GROUPS_ENABLED));
    }

    public boolean isAclHostsEnabled() {
        return "True".equals(attrs.get(ATTRIBUTE_ACL_HOSTS_ENABLED));
    }

    public String getLockedForKey() {
        if (isAclUsersEnabled()) return "users";
        if (isAclGroupsEnabled()) return "groups";
        if (isAclHostsEnabled()) return "hosts";
        return null;
    }

    public String getLockedFor() {
        if (isAclUsersEnabled()) return getAclUsers().replace(',', ' ');
        if (isAclGroupsEnabled()) return getAclGroups().replace(',', ' ');
        if (isAclHostsEnabled()) return getAclHosts().replace(',', ' ');
        return "";
    }


    public boolean isLocked() {
        return this.isAclUsersEnabled() || this.isAclGroupsEnabled() || this.isAclHostsEnabled();
    }

    public String getMaxRunningJobs() {
        return attrs.get(ATTRIBUTE_MAX_RUNNING_JOBS);
    }

    public String getMaxUserRun() {
        return attrs.get(ATTRIBUTE_MAX_USER_RUN);
    }

    public String getMaxUserCPU() {
        return attrs.get(ATTRIBUTE_MAX_USER_CPU);
    }

    public String getFairshareTree() {
        String ft = attrs.get(ATTRIBUTE_FAIRSHARE_TREE);
        return ft != null ? ft : "default";
    }

    public int getJobsRunning() {
        return running;
    }

    public int getJobsCompleted() {
        return completed;
    }

    public int getJobsQueued() {
        return queued;
    }

    public int getJobsTotal() {
        return total;
    }

    public void setJobNums(int running, int completed, int queued, int total) {
        this.running = running;
        this.completed = completed;
        this.queued = queued;
        this.total = total;
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[" + name + ",R=" + running + ",C=" + completed + ",Q=" + queued + ",T=" + total + "]";
    }

    public String getOrigToString() {
        return super.toString();
    }

    public List<Node> getNodes() {
        return getPbs().getQueueToNodesMap().get(this.getName());
    }

    public List<Job> getJobs() {
        return this.getPbs().getQueueToJobsMap().get(this.getName());
    }

    public boolean isExecutionQueue() {
        return "Execution".equals(attrs.get(ATTRIBUTE_QUEUE_TYPE));
    }


    private synchronized void prepareDescriptions() {
        descriptionsMap = new HashMap<>();
        String description_cs = attrs.get(ATTRIBUTE_DESCRIPTION_CZECH);
        if (description_cs != null) {
            descriptionsMap.put(new Locale("cs"), description_cs);
        }
        String description_en = attrs.get(ATTRIBUTE_DESCRIPTION_ENGLISH);
        if (description_en != null) {
            descriptionsMap.put(new Locale("en"), description_en);
        }
        descriptionAvailable = (description_cs != null && description_en != null);
        descriptionsPrepared = true;
    }

    private Map<Locale, String> descriptionsMap;
    private boolean descriptionAvailable;
    private boolean descriptionsPrepared = false;

    public Map<Locale, String> getDescriptionMap() {
        if (!descriptionsPrepared) prepareDescriptions();
        return descriptionsMap;
    }

    public boolean isDescriptionAvailable() {
        if (!descriptionsPrepared) prepareDescriptions();
        return descriptionAvailable;
    }
}
