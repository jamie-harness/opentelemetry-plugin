package io.jenkins.plugins.opentelemetry.embeded;


import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.ModelObject;
import hudson.util.FormValidation;
import io.jenkins.plugins.opentelemetry.MigrateHarnessUrlChildAction;
import jenkins.model.TransientActionFactory;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import javax.ws.rs.POST;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;


@Extension
public class MigrateHarnessMenu extends TransientActionFactory<ModelObject> {
    @Override
    public Class<ModelObject> type() {
        return ModelObject.class;
    }
    @NonNull
    @Override
    public Collection<? extends Action> createFor(@NonNull ModelObject target) {
        MigrateHarnessUrlChildAction migrateHarnessUrlChildAction = new MigrateHarnessUrlChildAction(target);
        return Collections.singleton(migrateHarnessUrlChildAction);
    }

    @Override
    public Class<? extends Action> actionType() {
        return MigrateHarnessUrlChildAction.class;
    }
}
