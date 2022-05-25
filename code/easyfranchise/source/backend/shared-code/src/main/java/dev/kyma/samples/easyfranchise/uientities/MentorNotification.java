package dev.kyma.samples.easyfranchise.uientities;

import dev.kyma.samples.easyfranchise.dbentities.Mentor;

public class MentorNotification {
    public Mentor mentor;
    public UIFranchise franchise;
    
    @Override
    public String toString() {
        return "MentorNotification [mentor=" + mentor + ", franchise=" + franchise + "]";
    }
}
