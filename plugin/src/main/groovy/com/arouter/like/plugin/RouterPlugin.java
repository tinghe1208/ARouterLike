package com.arouter.like.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.AppPlugin;
import com.arouter.like.plugin.utils.Logger;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class RouterPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {

        boolean isApp = project.getPlugins().hasPlugin(AppPlugin.class);

        if (isApp) {
            Logger.make(project);
            AppExtension android = project.getExtensions().getByType(AppExtension.class);
            android.registerTransform(new RegisterTransform());
        }
    }
}
