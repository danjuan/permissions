package com.plugin.permission.safe

import groovy.json.JsonSlurper
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.tasks.TaskState;


public class DzPermissionSafePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.extensions.create("permissionConfig", PermissionPluginConfig)
        project.afterEvaluate {
            println("==============start==============");
            File jsonFile = new File(project.permissionConfig.permissionPath)
            if (jsonFile.exists()) {
                List<String> list = new ArrayList<>();
                def jsonParser = new JsonSlurper()
                def jsonObj = jsonParser.parseText(jsonFile.text)
                jsonObj.permissions.each { permission ->
                    list.add(permission)
                }

                if (list.size() > 0) {
                    project.gradle.addListener(new TaskTimeListener(list))
                }
            }

        }
    }

    public class TaskTimeListener implements TaskExecutionListener {

        List<String> permissionList = new ArrayList<>();

        TaskTimeListener(List<String> list) {
            this.permissionList = list;
        }

        @Override
        void beforeExecute(Task task) {

        }

        @Override
        void afterExecute(Task task, TaskState taskState) {

            def path = task.path
            println("###########path=${path}")
            if (path.contains("Manifest") && path.contains("process")) {
                println("\n")
                def name = task.name.replace("Manifest", "").replace("process", "")
                def pp = task.project.getBuildDir().path + "/intermediates/merged_manifests/" + name + "/" + task.name + "/merged/AndroidManifest.xml"
                def file = new File(pp)
                if (file.exists()) {
                    println("==============================\n")
                    def manifest = new XmlSlurper().parse(file)
                    def childNodes = manifest.childNodes()
                    while (childNodes.hasNext()) {
                        def nextNode = childNodes.next()

                        nextNode.findAll { node ->
                            def attributes = node.attributes
                            Iterator iterator = attributes.keySet().iterator()
                            boolean illegal = false
                            boolean remove_permission = false
                            String illegal_permission = ""
                            while (iterator.hasNext()) {
                                Object key = iterator.next()
                                String permission = attributes.get(key)
                                if (permission.contains("permission")) {
                                    println("permission:" + permission)
                                    if (!permissionList.contains(permission)) {
                                        illegal = true
                                        illegal_permission = permission
                                    }
                                }
                                if (permission.contains("remove")) {
                                    remove_permission = true
                                }

                            }
                            if (illegal && !remove_permission) {
                                throw new GradleException("非法的权限：" + illegal_permission + "；==来自：" + task.project.name)
                            }
                        }
                    }

                    println("==============================\n")
                }
                println("\n")
            }
        }

    }

}